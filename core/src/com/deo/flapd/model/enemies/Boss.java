package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.deo.flapd.control.EnemyAi;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Drops;
import com.deo.flapd.model.Entity;
import com.deo.flapd.model.Player;
import com.deo.flapd.model.UraniumCell;
import com.deo.flapd.model.bullets.BulletData;
import com.deo.flapd.model.bullets.EnemyBullet;
import com.deo.flapd.utils.JsonEntry;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.badlogic.gdx.math.MathUtils.random;
import static com.deo.flapd.utils.DUtils.LogLevel.ERROR;
import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.LogLevel.WARNING;
import static com.deo.flapd.utils.DUtils.constructFilledImageWithColor;
import static com.deo.flapd.utils.DUtils.drawParticleEffectBounds;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getDistanceBetweenTwoPoints;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static java.lang.StrictMath.abs;

public class Boss {
    
    public JsonEntry bossConfig;
    private final Array<BasePart> parts;
    private final Array<Phase> phases;
    private final int[] spawnAt;
    boolean visible;
    Player player;
    Array<Movement> animations;
    public boolean hasAlreadySpawned;
    private final int spawnScore;
    public BasePart body;
    
    boolean hasAi;
    EnemyAi bossAi;
    
    public String bossName;
    
    Boss(String bossName, AssetManager assetManager) {
        log("------------------------------------------------------\n", INFO);
        log("---------loading boss config, name: " + bossName, INFO);
        
        this.bossName = bossName;
        
        bossConfig = new JsonEntry(new JsonReader().parse(Gdx.files.internal("enemies/bosses/" + bossName + "/config.json")));
        hasAi = bossConfig.getBoolean(false, false, "hasAi");
        TextureAtlas bossAtlas = assetManager.get("enemies/bosses/" + bossName + "/" + bossConfig.getString("noTextures", "textures"));
        bossAtlas.addRegion("noTexture", new TextureRegion(bossAtlas.getRegions().get(0), 0, 0, 0, 0));
        parts = new Array<>();
        animations = new Array<>();
        hasAlreadySpawned = getBoolean("boss_spawned_" + bossName);
        spawnScore = bossConfig.getInt(-1, "spawnConditions", "score") + getRandomInRange(-bossConfig.getInt(0, "spawnConditions", "randomness"), bossConfig.getInt(0, "spawnConditions", "randomness"));
        spawnAt = bossConfig.getIntArray(new int[]{500, 500}, "spawnAt");
        for (int i = 0; i < bossConfig.get("parts").size; i++) {
            String type = bossConfig.get("parts", i).getString("part", "type");
            switch (type) {
                case ("basePart"):
                    if (body == null) {
                        body = new BasePart(bossConfig.get("parts", i), bossAtlas, assetManager);
                        parts.add(body);
                    } else {
                        log("can't have two or more base parts per boss, ignoring " + bossConfig.get("parts", i).name, ERROR);
                    }
                    break;
                case ("part"):
                    parts.add(new Part(bossConfig.get("parts", i), bossAtlas, parts, body, assetManager));
                    break;
                case ("cannon"):
                    parts.add(new Cannon(bossConfig.get("parts", i), bossAtlas, parts, body, assetManager));
                    break;
                case ("shield"):
                    parts.add(new Shield(bossConfig.get("parts", i), bossAtlas, parts, body, assetManager));
                    break;
                case ("clone"):
                    String copyFrom = bossConfig.get("parts", i).getString(parts.get(0).name, "copyFrom");
                    String cloneType = bossConfig.getString("part", "parts", copyFrom, "type");
                    switch (cloneType) {
                        case ("part"):
                            parts.add(new Part(bossConfig.get("parts", i), bossAtlas, parts, body, assetManager));
                            break;
                        case ("cannon"):
                            parts.add(new Cannon(bossConfig.get("parts", i), bossAtlas, parts, body, assetManager));
                            break;
                        default:
                            log("Can't copy base part", WARNING);
                            break;
                    }
                    break;
                default:
                    log("Unknown path type: " + type, WARNING);
                    break;
            }
        }
        
        if (hasAi) {
            bossAi = new EnemyAi();
        }
        
        Array<Array<BasePart>> sortedParts = new Array<>();
        
        int maxLayer = 0;
        
        for (int i = 0; i < parts.size; i++) {
            maxLayer = Math.max(maxLayer, parts.get(i).layer);
        }
        
        maxLayer++;
        
        sortedParts.setSize(maxLayer);
        
        for (int i = 0; i < maxLayer; i++) {
            sortedParts.set(i, new Array<>());
        }
        
        for (int i = 0; i < parts.size; i++) {
            sortedParts.get(parts.get(i).layer).add(parts.get(i));
        }
        
        parts.clear();
        
        for (int i = 0; i < maxLayer; i++) {
            for (int i2 = 0; i2 < sortedParts.get(i).size; i2++) {
                parts.add(sortedParts.get(i).get(i2));
            }
        }
        
        phases = new Array<>();
        
        if (bossConfig.get(false, "groups").isNull()) {
            bossConfig.jsonValue.addChild("groups", new JsonValue(JsonValue.ValueType.object));
        }
        StringBuilder allParts = new StringBuilder(parts.get(0).name);
        for (int i = 1; i < parts.size; i++) {
            allParts.append(",").append(parts.get(i).name);
        }
        bossConfig.get("groups").jsonValue.addChild("all", new JsonValue(allParts.toString()));
        for (int i = 0; i < bossConfig.get("phases").size; i++) {
            Phase phase = new Phase(bossConfig.get("groups"), bossConfig.get("phases", i), parts, animations, this);
            phases.add(phase);
        }
    }
    
    void draw(SpriteBatch batch, float delta) {
        if (visible) {
            for (int i = 0; i < parts.size; i++) {
                parts.get(i).draw(batch, delta);
            }
        }
    }
    
    void drawDebug(ShapeRenderer shapeRenderer) {
        if (visible) {
            for (int i = 0; i < parts.size; i++) {
                parts.get(i).drawDebug(shapeRenderer);
            }
        }
    }
    
    void update(float delta) {
        if (GameLogic.score >= spawnScore && !hasAlreadySpawned) {
            spawn();
        }
        if (visible) {
            
            body.x += body.movementOffsetX;
            body.y += body.movementOffsetY;
            body.movementOffsetX = 0;
            body.movementOffsetY = 0;
            
            for (int i = 0; i < parts.size; i++) {
                if (!parts.get(i).equals(body)) {
                    parts.get(i).x = body.x + parts.get(i).offsetX;
                    parts.get(i).y = body.y + parts.get(i).offsetY;
                }
                parts.get(i).update(delta);
            }
            for (int i = 0; i < animations.size; i++) {
                animations.get(i).update(delta);
            }
            for (int i = 0; i < phases.size; i++) {
                phases.get(i).update();
            }
            if (hasAi) {
                bossAi.update(delta);
            }
        }
    }
    
    void spawn() {
        body.x = spawnAt[0];
        body.y = spawnAt[1];
        visible = true;
        hasAlreadySpawned = true;
        putBoolean("boss_spawned_" + bossName, true);
        GameLogic.bossWave = true;
        phases.get(0).activate();
    }
    
    void setTargetPlayer(Player player) {
        this.player = player;
        if (hasAi) {
            bossAi.initialize(player, body);
        }
        for (int i = 0; i < parts.size; i++) {
            parts.get(i).setTargetPlayer(player);
        }
    }
    
    void dispose() {
        for (int i = 0; i < parts.size; i++) {
            parts.get(i).dispose();
        }
    }
    
    void reset() {
        visible = false;
        body.x = 1500;
        body.y = 1500;
        GameLogic.bossWave = false;
        
        for (int i = 0; i < animations.size; i++) {
            animations.get(i).reset();
        }
        for (int i = 0; i < parts.size; i++) {
            parts.get(i).reset();
        }
        for (int i = 0; i < phases.size; i++) {
            phases.get(i).reset();
        }
    }
}

class BasePart extends Entity {
    
    Animation<TextureRegion> enemyAnimation;
    private float animationPosition;
    
    AssetManager assetManager;
    
    float movementOffsetX = 0;
    float movementOffsetY = 0;
    float movementRotation = 0;
    String name;
    String type;
    JsonEntry currentConfig;
    ProgressBar healthBar;
    Player player;
    boolean hasAnimation;
    boolean collisionEnabled;
    boolean hasCollision;
    boolean visible = true;
    boolean showHealthBar;
    
    int layer;
    
    ParticleEffect explosionEffect;
    boolean exploded;
    
    TextureAtlas textures;
    
    Array<BasePart> links;
    
    int[] bonusType, itemRarity, itemCount, moneyCount;
    float itemTimer, moneyTimer;
    int bonusChance;
    
    Sound explosionSound;
    float soundVolume;
    
    BasePart(JsonEntry newConfig, TextureAtlas textures, AssetManager assetManager) {
        
        log("loading boss part, name: " + newConfig.name, INFO);
        
        active = false;
        this.assetManager = assetManager;
        
        exploded = false;
        name = newConfig.name;
        currentConfig = newConfig;
        this.textures = textures;
        links = new Array<>();
        if (newConfig.getString("part", "type").equals("clone")) {
            currentConfig = newConfig.parent().get(newConfig.getString("noCopyFromTarget", "copyFrom"));
            currentConfig.name = name;
        }
        if (currentConfig.isNull()) {
            log("Invalid copy from target while trying to load " + name, ERROR);
        }
        type = currentConfig.getString("part", "type");
        
        hasCollision = currentConfig.getBoolean(false, false, "hasCollision");
        if (hasCollision) {
            health = currentConfig.getFloat(1, "health");
            regeneration = currentConfig.getFloat(false, 0, "regeneration");
        } else {
            health = 1;
            regeneration = 0;
        }
        maxHealth = health;
        width = currentConfig.getFloat(1, "width");
        height = currentConfig.getFloat(1, "height");
        layer = currentConfig.getInt(0, "layer");
        
        if (hasCollision && !type.equals("shield")) {
            itemRarity = currentConfig.getIntArray(new int[]{1, 2}, "drops", "items", "rarity");
            itemCount = currentConfig.getIntArray(new int[]{1, 2}, "drops", "items", "count");
            itemTimer = currentConfig.getFloat(1, "drops", "items", "timer");
            
            bonusChance = currentConfig.getInt(50, "drops", "bonuses", "chance");
            bonusType = currentConfig.getIntArray(new int[]{1, 2}, "drops", "bonuses", "type");
            
            moneyCount = currentConfig.getIntArray(new int[]{3, 5}, "drops", "money", "count");
            moneyTimer = currentConfig.getFloat(1, "drops", "items", "timer");
            
            explosionSound = assetManager.get(currentConfig.getString("sfx/explosion.ogg", "explosionSound"));
            explosionEffect = new ParticleEffect();
            explosionEffect.load(Gdx.files.internal(currentConfig.getString("particles/explosion.p", "explosionEffect")), Gdx.files.internal("particles"));
            explosionEffect.scaleEffect(currentConfig.getFloat(1, "explosionScale"));
            log("creating explosion effect for part: " + newConfig.name, INFO);
        }
        
        soundVolume = getFloat("soundVolume");
        String texture = currentConfig.getString("noTexture", "texture");
        hasAnimation = currentConfig.getBoolean(false, false, "hasAnimation");
        
        if (hasAnimation) {
            entitySprite = new Sprite();
            enemyAnimation = new Animation<>(
                    currentConfig.getFloat(1, "frameDuration"),
                    textures.findRegions(name + "_" + texture),
                    Animation.PlayMode.LOOP);
        } else {
            entitySprite = new Sprite(textures.findRegion(texture));
        }
        
        originX = width / 2f;
        originY = height / 2f;
        if (!currentConfig.getString(false, "standard", "originX").equals("standard")) {
            originX = currentConfig.getFloat(0, "originX");
        }
        if (!currentConfig.getString(false, "standard", "originY").equals("standard")) {
            originY = currentConfig.getFloat(0, "originY");
        }
        collisionEnabled = false;
        super.init();
        
        TextureRegionDrawable BarForeground1 = constructFilledImageWithColor(0, 6, Color.RED);
        TextureRegionDrawable BarForeground2 = constructFilledImageWithColor(100, 6, Color.RED);
        TextureRegionDrawable BarBackground = constructFilledImageWithColor(100, 6, Color.WHITE);
        
        ProgressBar.ProgressBarStyle healthBarStyle = new ProgressBar.ProgressBarStyle();
        
        healthBarStyle.knob = BarForeground1;
        healthBarStyle.knobBefore = BarForeground2;
        healthBarStyle.background = BarBackground;
        
        healthBar = new ProgressBar(0, health, 0.01f, false, healthBarStyle);
        healthBar.setAnimateDuration(0.25f);
        healthBar.setSize(25, 6);
        healthBar.setPosition(width / 2 + x + movementOffsetX - 12.5f, y + movementOffsetY - 7);
    }
    
    void draw(SpriteBatch batch, float delta) {
        if (visible) {
            if (hasAnimation) {
                entitySprite.setRegion(enemyAnimation.getKeyFrame(animationPosition));
            }
            entitySprite.draw(batch);
            if (showHealthBar) {
                healthBar.draw(batch, 1);
            }
        }
        if (exploded) {
            explosionEffect.draw(batch);
        }
    }
    
    @Override
    public void drawDebug(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(entityHitBox.x, entityHitBox.y, entityHitBox.width, entityHitBox.height);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(x + originX + movementOffsetX, y + originY + movementOffsetY, 5);
    }
    
    @Override
    protected void updateEntity(float delta) {
        entitySprite.setPosition(x + movementOffsetX, y + movementOffsetY);
        entitySprite.setRotation(rotation + movementRotation);
        entitySprite.setColor(color);
        if (health > 0) {
            entityHitBox.setPosition(entitySprite.getX(), entitySprite.getY());
            if (regeneration > 0 && maxHealth > 0) {
                health = clamp(health + regeneration * delta, 0, maxHealth);
            }
        } else {
            entityHitBox.setPosition(-1000, -1000).setSize(0, 0);
        }
    }
    
    void update(float delta) {
        updateEntity(delta);
        
        animationPosition += delta;
        
        if (showHealthBar) {
            healthBar.setValue(health);
            healthBar.setPosition(width / 2 + x + movementOffsetX - 12.5f, y + movementOffsetY - 7);
            healthBar.act(delta);
        }
        if (hasCollision && collisionEnabled && health > 0) {
            health -= player.bullet.overlaps(entityHitBox, true);
        }
        if (health <= 0 && !exploded) {
            explode();
            for (int i = 0; i < links.size; i++) {
                if (!links.get(i).exploded) {
                    links.get(i).explode();
                }
            }
        }
        if (exploded) {
            explosionEffect.update(delta);
        }
    }
    
    void setTargetPlayer(Player player) {
        this.player = player;
    }
    
    void dispose() {
        if (hasCollision && !type.equals("shield")) {
            explosionEffect.dispose();
        }
    }
    
    void addLinkedPart(BasePart part) {
        links.add(part);
    }
    
    void explode() {
        if (hasCollision && !type.equals("shield")) {
            UraniumCell.Spawn(entityHitBox, getRandomInRange(moneyCount[0], moneyCount[1]), 1, moneyTimer);
            
            if (getRandomInRange(0, 100) <= bonusChance) {
                Bonus.Spawn(getRandomInRange(bonusType[0], bonusType[1]), entityHitBox);
            }
            
            Drops.drop(entityHitBox, getRandomInRange(itemCount[0], itemCount[1]), itemTimer, getRandomInRange(itemRarity[0], itemRarity[1]));
            
            explosionEffect.setPosition(x + movementOffsetX + width / 2, y + movementOffsetY + height / 2);
            explosionEffect.start();
            
            if (soundVolume > 0) {
                explosionSound.play(soundVolume);
            }
            
            exploded = true;
        }
        active = false;
    }
    
    void reset() {
        movementOffsetX = 0;
        movementOffsetY = 0;
        movementRotation = 0;
        health = maxHealth;
        showHealthBar = false;
        visible = true;
        active = false;
        collisionEnabled = false;
        exploded = false;
        rotation = 0;
    }
}

class Part extends BasePart {
    
    private final Array<BasePart> parts;
    private BasePart link;
    private final BasePart body;
    
    Part(JsonEntry newConfig, TextureAtlas textures, Array<BasePart> parts, BasePart body, AssetManager assetManager) {
        super(newConfig, textures, assetManager);
        this.parts = parts;
        link = body;
        this.body = body;
        String relativeTo = currentConfig.getString(false, body.name, "offset", "relativeTo");
        offsetX = currentConfig.getFloat(false, 0, "offset", "X");
        offsetY = currentConfig.getFloat(false, 0, "offset", "Y");
        if (newConfig.getString("part", "type").equals("clone")) {
            for (int i = 0; i < newConfig.get(false, "override").size; i++) {
                if (newConfig.get("override", i).name.equals("offset")) {
                    offsetX = newConfig.get("override", i).getFloat(0, "X");
                    offsetY = newConfig.get("override", i).getFloat(0, "Y");
                    relativeTo = newConfig.get("override", i).getString(parts.get(0).name, "relativeTo");
                }
                if (newConfig.get("override", i).name.equals("originX")) {
                    originX = newConfig.getFloat(0, "override", i);
                    entitySprite.setOrigin(originX, originY);
                }
                if (newConfig.get("override", i).name.equals("originY")) {
                    originY = newConfig.getFloat(0, "override", i);
                    entitySprite.setOrigin(originX, originY);
                }
                if (newConfig.get("override", i).name.equals("fireRate")) {
                    if (this instanceof Cannon) {
                        currentConfig.get("fireRate", "randomness").jsonValue.set(newConfig.get("override", i).getFloat(0, "randomness"), "randomness");
                        currentConfig.get("fireRate", "baseRate").jsonValue.set(newConfig.get("override", i).getFloat(1, "baseRate"), "baseRate");
                        currentConfig.get("fireRate", "initialDelay").jsonValue.set(newConfig.get("override", i).getFloat(0, "initialDelay"), "initialDelay");
                    }
                }
                if (newConfig.get("override", i).name.equals("aimAngleLimit")) {
                    if (this instanceof Cannon) {
                        ((Cannon) this).aimAngleLimit = newConfig.get("override", i).asIntArray();
                    }
                }
            }
        }
        
        if (currentConfig.get(false, "linked").isString()) {
            for (int i = 0; i < parts.size; i++) {
                if (currentConfig.getString("no link target", "linked").equals(parts.get(i).name)) {
                    link = parts.get(i);
                    break;
                }
            }
        }
        
        link.addLinkedPart(this);
        
        getOffset(relativeTo);
    }
    
    private void getOffset(String relativeTo) {
        for (int i = 0; i < parts.size; i++) {
            if (parts.get(i).name.equals(relativeTo)) {
                offsetX += parts.get(i).offsetX;
                offsetY += parts.get(i).offsetY;
                if (!parts.get(i).type.equals("basePart")) {
                    String relativeToP = parts.get(i).currentConfig.getString("noRelativeToTarget", "offset", "relativeTo");
                    getOffset(relativeToP);
                }
                break;
            }
        }
    }
    
    @Override
    void draw(SpriteBatch batch, float delta) {
        if (visible && health > 0 && link.health > 0 && body.health > 0) {
            entitySprite.draw(batch);
            if (showHealthBar) {
                healthBar.draw(batch, 1);
            }
        }
        if (exploded) {
            explosionEffect.draw(batch);
        }
    }
}

class Shield extends Part {
    
    Shield(JsonEntry newConfig, TextureAtlas textures, Array<BasePart> parts, BasePart body, AssetManager assetManager) {
        super(newConfig, textures, parts, body, assetManager);
    }
    
    @Override
    protected void update(float delta) {
        updateEntity(delta);
        entitySprite.setAlpha(health / maxHealth);
        if (showHealthBar) {
            healthBar.setValue(health);
            healthBar.setPosition(width / 2 + x + movementOffsetX - 12.5f, y + movementOffsetY - 7);
            healthBar.act(delta);
        }
        if (hasCollision && collisionEnabled && health / maxHealth > 0.1f) {
            health = clamp(health - player.bullet.overlaps(entityHitBox, true), 1, maxHealth);
        }
    }
}

class Cannon extends Part {
    
    boolean canAim;
    String aimAnimationType;
    String[] aimTextures;
    int[] aimAngleLimit;
    
    float[] bulletOffset;
    float bulletOffsetAngle;
    float bulletOffsetDistance;
    
    int bulletsPerShot;
    float bulletSpread;
    
    float fireRate;
    float fireTimer;
    
    boolean drawBulletsOnTop;
    
    BulletData bulletData;
    Array<EnemyBullet> bullets;
    TextureAtlas textures;
    
    Sound shootingSound;
    
    boolean hasPowerUpEffect;
    ParticleEffect powerUpEffect;
    float powerUpEffectShootDelay;
    boolean powerUpActive;
    float powerUpScale;
    float powerUpOffsetAngle;
    float powerUpOffsetDistance;
    
    boolean hasPowerDownEffect;
    ParticleEffect powerDownEffect;
    float powerDownScale;
    float powerDownOffsetAngle;
    float powerDownOffsetDistance;
    
    float recoil;
    float currentRecoilOffset;
    float recoilReturnSpeed;
    
    Cannon(JsonEntry partConfig, TextureAtlas textures, Array<BasePart> parts, BasePart body, AssetManager assetManager) {
        super(partConfig, textures, parts, body, assetManager);
        
        this.textures = textures;
        
        bullets = new Array<>();
        
        drawBulletsOnTop = currentConfig.getBoolean(false, false, "drawBulletsOnTop");
        
        canAim = currentConfig.getBoolean(false, false, "canAim");
        if (canAim) {
            if (aimAngleLimit == null) {
                aimAngleLimit = currentConfig.getIntArray(new int[]{-360, 360}, "aimAngleLimit");
            }
            
            aimAnimationType = currentConfig.getString("noAnimation", "aimAnimation");
            
            if (aimAnimationType.equals("textureChange")) {
                aimTextures = new String[8];
                JsonEntry aimTexturesJsonValue = currentConfig.get("aimAnimationTextures");
                aimTextures[0] = aimTexturesJsonValue.getString("right", "right");
                aimTextures[1] = aimTexturesJsonValue.getString("left", "left");
                aimTextures[2] = aimTexturesJsonValue.getString("up", "up");
                aimTextures[3] = aimTexturesJsonValue.getString("down", "down");
                
                aimTextures[4] = aimTexturesJsonValue.getString("up_right", "up_right");
                aimTextures[5] = aimTexturesJsonValue.getString("up_left", "up_left");
                aimTextures[6] = aimTexturesJsonValue.getString("down_right", "down_right");
                aimTextures[7] = aimTexturesJsonValue.getString("down_left", "down_left");
            }
        }
        
        float fireRateRandomness = currentConfig.getFloat(0, "fireRate", "randomness");
        fireRate = currentConfig.getFloat(1, "fireRate", "baseRate") + getRandomInRange((int) (-fireRateRandomness * 10), (int) (fireRateRandomness * 10)) / 10f;
        fireTimer = -currentConfig.getFloat(0, "fireRate", "initialDelay");
        
        bulletData = new BulletData(currentConfig.get("bullet"));
        
        shootingSound = assetManager.get(currentConfig.getString("sfx/gun1.ogg", "shootSound"));
        
        bulletOffset = currentConfig.getFloatArray(new float[]{0, 0}, "bulletOffset");
        bulletOffsetAngle = MathUtils.atan2(bulletOffset[1], bulletOffset[0]) * MathUtils.radiansToDegrees;
        bulletOffsetDistance = getDistanceBetweenTwoPoints(0, 0, bulletOffset[0], bulletOffset[1]);
        bulletsPerShot = currentConfig.getInt(1, "bulletsPerShot");
        bulletSpread = currentConfig.getFloat(0, "bulletSpread");
        
        hasPowerUpEffect = currentConfig.getBoolean(false, false, "powerUpEffect");
        if (hasPowerUpEffect) {
            powerUpScale = currentConfig.getFloat(1, "powerUpEffectScale");
            powerUpEffect = new ParticleEffect();
            powerUpEffect.load(Gdx.files.internal(currentConfig.getString("particles/laser_powerup_red.p", "powerUpEffect")), Gdx.files.internal("particles"));
            powerUpEffectShootDelay = currentConfig.getFloat(1, "powerUpShootDelay");
            powerUpEffect.scaleEffect(powerUpScale);
            float[] powerUpOffset = currentConfig.getFloatArray(new float[]{0, 0}, "powerUpEffectOffset");
            powerUpOffset[0] += bulletOffset[0];
            powerUpOffset[1] += bulletOffset[1];
            powerUpOffsetAngle = MathUtils.atan2(powerUpOffset[1], powerUpOffset[0]) * MathUtils.radiansToDegrees;
            powerUpOffsetDistance = getDistanceBetweenTwoPoints(0, 0, powerUpOffset[0], powerUpOffset[1]);
        }
        
        hasPowerDownEffect = currentConfig.getBoolean(false, false, "powerDownEffect");
        if (hasPowerDownEffect) {
            powerDownScale = currentConfig.getFloat(1, "powerDownEffectScale");
            powerDownEffect = new ParticleEffect();
            powerDownEffect.load(Gdx.files.internal(currentConfig.getString("particles/smoke.p", "powerDownEffect")), Gdx.files.internal("particles"));
            powerDownEffect.scaleEffect(powerDownScale);
            float[] powerDownOffset = currentConfig.getFloatArray(new float[]{0, 0}, "powerDownEffectOffset");
            powerDownOffset[0] += bulletOffset[0];
            powerDownOffset[1] += bulletOffset[1];
            powerDownOffsetAngle = MathUtils.atan2(powerDownOffset[1], powerDownOffset[0]) * MathUtils.radiansToDegrees;
            powerDownOffsetDistance = getDistanceBetweenTwoPoints(0, 0, powerDownOffset[0], powerDownOffset[1]);
        }
        
        if (currentConfig.get(false, "recoil").isNumber()) {
            recoil = currentConfig.getFloat(5, "recoil");
            recoilReturnSpeed = currentConfig.getFloat(10, "recoilReturnSpeed");
        }
        
    }
    
    @Override
    protected void updateEntity(float delta) {
        entitySprite.setPosition(
                x + movementOffsetX + MathUtils.cosDeg(rotation + movementRotation) * currentRecoilOffset,
                y + movementOffsetY + MathUtils.sinDeg(rotation + movementRotation) * currentRecoilOffset);
        entitySprite.setRotation(rotation + movementRotation);
        entitySprite.setColor(color);
        if (health > 0) {
            entityHitBox.setPosition(entitySprite.getX(), entitySprite.getY());
            if (regeneration > 0 && maxHealth > 0) {
                health = clamp(health + regeneration * delta, 0, maxHealth);
            }
        } else {
            entityHitBox.setPosition(-1000, -1000).setSize(0, 0);
        }
    }
    
    @Override
    protected void update(float delta) {
        super.update(delta);
        if (active) {
            if (canAim) {
                float angleToThePlayer = clamp(MathUtils.radiansToDegrees * MathUtils.atan2(y - (player.bounds.getY() + player.bounds.getHeight() / 2), x - (player.bounds.getX() + player.bounds.getWidth() / 2)), aimAngleLimit[0], aimAngleLimit[1]);
                switch (aimAnimationType) {
                    case ("textureChange"):
                        
                        String texture;
                        if (angleToThePlayer >= -22.5 && angleToThePlayer <= 22.5) {
                            texture = aimTextures[1];
                        } else if (angleToThePlayer >= 22.5 && angleToThePlayer <= 67.5) {
                            texture = aimTextures[7];
                        } else if (angleToThePlayer >= 67.5 && angleToThePlayer <= 112.5) {
                            texture = aimTextures[3];
                        } else if (angleToThePlayer >= 112.5 && angleToThePlayer <= 157.5) {
                            texture = aimTextures[6];
                        } else if ((angleToThePlayer >= 157.5 && angleToThePlayer <= 180) || (angleToThePlayer >= -180 && angleToThePlayer <= -157.5)) {
                            texture = aimTextures[0];
                        } else if (angleToThePlayer >= -157.5 && angleToThePlayer <= -112.5) {
                            texture = aimTextures[4];
                        } else if (angleToThePlayer >= -112.5 && angleToThePlayer <= -67.5) {
                            texture = aimTextures[2];
                        } else {
                            texture = aimTextures[5];
                        }
                        if (hasAnimation) {
                            String atlas = texture.replace(" ", "").split(",")[0];
                            float frameDuration = Float.parseFloat(texture.replace(" ", "").split(",")[1]);
                            enemyAnimation = new Animation<>(
                                    frameDuration,
                                    textures.findRegions(name + "_" + atlas),
                                    Animation.PlayMode.LOOP);
                        } else {
                            entitySprite.setRegion(textures.findRegion(texture));
                        }
                        break;
                    case ("rotate"):
                        rotation = angleToThePlayer;
                        break;
                }
            }
            if (hasPowerUpEffect) {
                float newX = x + width / 2f - bulletData.width / 2f + MathUtils.cosDeg(rotation + movementRotation + powerUpOffsetAngle) * powerUpOffsetDistance;
                float newY = y + height / 2f - bulletData.height / 2f + MathUtils.sinDeg(rotation + movementRotation + powerUpOffsetAngle) * powerUpOffsetDistance;
                powerUpEffect.setPosition(newX, newY);
                powerUpEffect.update(delta);
            }
            if (hasPowerDownEffect) {
                float newX = x + width / 2f - bulletData.width / 2f + MathUtils.cosDeg(rotation + movementRotation + powerDownOffsetAngle) * powerDownOffsetDistance;
                float newY = y + height / 2f - bulletData.height / 2f + MathUtils.sinDeg(rotation + movementRotation + powerDownOffsetAngle) * powerDownOffsetDistance;
                powerDownEffect.setPosition(newX, newY);
                powerDownEffect.update(delta);
            }
            if (fireTimer >= 1 + powerUpEffectShootDelay && health > 0) {
                shoot();
                powerUpActive = false;
                fireTimer = 0;
            } else {
                if (fireTimer < 1) {
                    fireTimer += delta * fireRate;
                } else {
                    if (!powerUpActive) {
                        powerUpEffect.start();
                        powerUpActive = true;
                    }
                    fireTimer += delta;
                }
            }
            if (currentRecoilOffset > 0) {
                currentRecoilOffset = clamp(currentRecoilOffset - delta * recoilReturnSpeed, 0, recoil);
            }
        }
    }
    
    @Override
    void draw(SpriteBatch batch, float delta) {
        if (drawBulletsOnTop) {
            super.draw(batch, delta);
        }
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).update(delta);
            bullets.get(i).draw(batch, delta);
            if (bullets.get(i).queuedForDeletion) {
                bullets.get(i).dispose();
                bullets.removeIndex(i);
            }
        }
        if (hasPowerDownEffect && active) {
            powerDownEffect.draw(batch);
        }
        if (!drawBulletsOnTop) {
            super.draw(batch, delta);
        }
        if (hasPowerUpEffect && active) {
            powerUpEffect.draw(batch);
        }
    }
    
    @Override
    public void drawDebug(ShapeRenderer shapeRenderer) {
        super.drawDebug(shapeRenderer);
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).drawDebug(shapeRenderer);
        }
        if (hasPowerUpEffect) {
            shapeRenderer.setColor(Color.YELLOW);
            drawParticleEffectBounds(shapeRenderer, powerUpEffect);
        }
        if (hasPowerDownEffect) {
            shapeRenderer.setColor(Color.RED);
            drawParticleEffectBounds(shapeRenderer, powerDownEffect);
        }
    }
    
    void shoot() {
        for (int i = 0; i < bulletsPerShot; i++) {
            BulletData newBulletData = new BulletData(currentConfig.get("bullet"));
            
            float newX = x + movementOffsetX + originX - bulletData.width / 2f + MathUtils.cosDeg(rotation + movementRotation + bulletOffsetAngle) * bulletOffsetDistance;
            float newY = y + movementOffsetY + originY - bulletData.height / 2f + MathUtils.sinDeg(rotation + movementRotation + bulletOffsetAngle) * bulletOffsetDistance;
            float newRot = 0;
            
            if (canAim) {
                newRot = clamp(MathUtils.radiansToDegrees * MathUtils.atan2(y + movementOffsetY + height / 2f - (player.bounds.getY() + player.bounds.getHeight() / 2), x + movementOffsetY + width / 2f - (player.bounds.getX() + player.bounds.getWidth() / 2)), aimAngleLimit[0], aimAngleLimit[1]);
            }
            
            newRot += getRandomInRange(-10, 10) * bulletSpread;
            newRot += movementRotation;
            
            bullets.add(new EnemyBullet(assetManager, newBulletData, player, newX, newY, newRot, bulletData.hasCollisionWithPlayerBullets));
        }
        
        if (hasPowerDownEffect) {
            powerDownEffect.start();
        }
        
        currentRecoilOffset = recoil;
        
        if (soundVolume > 0) {
            shootingSound.play();
        }
        
    }
    
    @Override
    void reset() {
        super.reset();
        if (hasPowerUpEffect) {
            powerUpEffect.reset();
        }
        if (hasPowerDownEffect) {
            powerDownEffect.reset();
        }
    }
    
    @Override
    void dispose() {
        super.dispose();
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).dispose();
        }
        if (hasPowerUpEffect) {
            powerUpEffect.dispose();
        }
        if (hasPowerDownEffect) {
            powerDownEffect.dispose();
        }
    }
}

class Movement {
    
    private float moveBy;
    
    private float lastShakeXPos;
    private float lastShakeYPos;
    private float shakePeriod;
    private float shakeIntensityX;
    private float shakeIntensityY;
    
    private float currentAddPosition;
    private float speed;
    private float progress;
    private boolean active;
    private BasePart target;
    
    private BasePart relativeTarget;
    private float radiusExpansionRate;
    private float currentRadius;
    private float targetRadius;
    private float angleOffset;
    
    private final String type;
    private byte directionModifier;
    private final boolean stopPreviousAnimations;
    
    private final Array<Movement> animations;
    
    private final BasePart body;
    
    Movement(JsonEntry movementConfig, String target, Array<BasePart> parts, Array<Movement> animations, BasePart body) {
        
        log("loading boss movement, name: " + movementConfig.name, INFO);
        
        this.body = body;
        
        this.animations = animations;
        for (int i = 0; i < parts.size; i++) {
            if (parts.get(i).name.equals(target)) {
                this.target = parts.get(i);
                break;
            }
        }
        
        stopPreviousAnimations = movementConfig.getBoolean(false, false, "stopPreviousAnimations");
        
        type = movementConfig.name;
        if (type.equals("rotateRelativeTo")) {
            for (int i = 0; i < parts.size; i++) {
                if (parts.get(i).name.equals(movementConfig.getString(parts.get(0).name, "relativeTo"))) {
                    relativeTarget = parts.get(i);
                    break;
                }
            }
            radiusExpansionRate = movementConfig.getFloat(5, "radiusExpansionRate");
            targetRadius = movementConfig.getFloat(100, "targetRadius");
            angleOffset = movementConfig.getFloat(0, "angleOffset");
        }
        if (type.equals("shake")) {
            shakeIntensityX = movementConfig.getFloat(10, "shakeIntensityX");
            shakeIntensityY = movementConfig.getFloat(10, "shakeIntensityY");
            shakePeriod = movementConfig.getFloat(0.3f, "shakePeriod");
        } else {
            speed = movementConfig.getFloat(100, "speed");
            if (movementConfig.getString("inf", "moveBy").equals("inf")) {
                moveBy = Integer.MAX_VALUE;
            } else {
                moveBy = movementConfig.getFloat(Integer.MAX_VALUE, "moveBy");
            }
            directionModifier = 0 < moveBy ? (byte) 1 : (byte) -1;
        }
    }
    
    void start() {
        if (stopPreviousAnimations) {
            for (int i = 0; i < animations.size; i++) {
                animations.get(i).stop();
            }
        }
        active = true;
    }
    
    void stop() {
        active = false;
    }
    
    void update(float delta) {
        if (active) {
            
            if ((type.equals("moveLinearX") || type.equals("moveLinearY") || type.equals("rotate"))) {
                if (abs(currentAddPosition) < abs(moveBy)) {
                    currentAddPosition += speed * delta * 10 * directionModifier;
                } else {
                    active = false;
                }
            }
            
            switch (type) {
                case ("moveLinearX"):
                    target.movementOffsetX += speed * delta * 10 * directionModifier;
                    break;
                case ("moveLinearY"):
                    target.movementOffsetY += speed * delta * 10 * directionModifier;
                    break;
                case ("rotate"):
                    target.movementRotation += speed * delta * 10 * directionModifier;
                    break;
                case ("moveSinX"): {
                    target.movementOffsetX += MathUtils.sinDeg(progress) * moveBy * directionModifier * delta;
                    progress += speed * delta * 10;
                    break;
                }
                case ("moveSinY"): {
                    target.movementOffsetY += MathUtils.sinDeg(progress) * moveBy * directionModifier * delta;
                    progress += speed * delta * 10;
                    break;
                }
                case ("rotateSin"): {
                    target.movementRotation += MathUtils.sinDeg(progress) * moveBy * directionModifier * delta;
                    progress += speed * delta * 10;
                    break;
                }
                case ("rotateRelativeTo"): {
                    // TODO: 19/8/2021 fix this
                    if (relativeTarget.equals(body)) {
                        target.movementOffsetX = body.originX - target.originX + MathUtils.cosDeg(progress + angleOffset) * currentRadius;
                        target.movementOffsetY = body.originY - target.originY + MathUtils.sinDeg(progress + angleOffset) * currentRadius;
                    } else {
                        target.movementOffsetX = relativeTarget.offsetX + MathUtils.cosDeg(progress + angleOffset) * currentRadius;
                        target.movementOffsetY = relativeTarget.offsetY + MathUtils.sinDeg(progress + angleOffset) * currentRadius;
                    }
                    if (progress < moveBy) {
                        progress = clamp(progress + speed * delta * 10, 0, moveBy);
                    }
                    if (currentRadius < targetRadius) {
                        currentRadius = clamp(currentRadius + radiusExpansionRate * delta * 10, 0, targetRadius);
                    }
                    if (progress >= moveBy && currentRadius >= targetRadius) {
                        active = false;
                    }
                    break;
                }
                case ("shake"): {
                    progress += delta * 10;
                    if (progress >= shakePeriod) {
                        progress = 0;
                        float nextShakeXPos = (random() - 0.5f) * 2 * shakeIntensityX;
                        float nextShakeYPos = (random() - 0.5f) * 2 * shakeIntensityY;
                        target.movementOffsetX = target.movementOffsetX - lastShakeXPos + nextShakeXPos;
                        target.movementOffsetY = target.movementOffsetY - lastShakeYPos + nextShakeYPos;
                        lastShakeXPos = nextShakeXPos;
                        lastShakeYPos = nextShakeYPos;
                    }
                    break;
                }
            }
        }
    }
    
    void reset() {
        stop();
        progress = 0;
        currentAddPosition = 0;
        currentRadius = 0;
    }
}

class Phase {
    
    Array<Action> actions;
    boolean activated;
    JsonEntry config;
    Array<PhaseTrigger> phaseTriggers;
    
    Boss boss;
    
    Phase(JsonEntry partGroups, JsonEntry phaseData, Array<BasePart> parts, Array<Movement> animations, Boss boss) {
        
        log("loading boss phase, name: " + phaseData.name, INFO);
        
        this.boss = boss;
        
        actions = new Array<>();
        phaseTriggers = new Array<>();
        activated = false;
        config = phaseData;
        for (int i = 0; i < phaseData.size; i++) {
            actions.add(new Action(partGroups, phaseData.get(i), parts, animations, boss, "", actions));
        }
        JsonEntry triggers = phaseData.parent().parent().get(false, "phaseTriggers", config.name, "triggers");
        if (triggers.isNull()) {
            log("no triggers for " + phaseData.name, WARNING);
        } else {
            for (int i = 0; i < triggers.size; i++) {
                PhaseTrigger trigger = new PhaseTrigger(triggers.get(i), parts);
                phaseTriggers.add(trigger);
            }
        }
    }
    
    void activate() {
        for (int i = 0; i < actions.size; i++) {
            actions.get(i).activate();
        }
        activated = true;
    }
    
    void update() {
        for (int i = 0; i < phaseTriggers.size; i++) {
            phaseTriggers.get(i).update();
        }
        if (!activated) {
            boolean activate = phaseTriggers.size > 0;
            boolean reset = false;
            for (int i = 0; i < phaseTriggers.size; i++) {
                activate = activate && phaseTriggers.get(i).conditionsMet;
                reset = reset || (phaseTriggers.get(i).conditionsMet && phaseTriggers.get(i).isResetPhase);
            }
            if (activate) {
                activate();
            }
            if (reset) {
                boss.reset();
            }
        }
    }
    
    void reset() {
        activated = false;
        for (int i = 0; i < phaseTriggers.size; i++) {
            phaseTriggers.get(i).reset();
        }
    }
}

class Action {
    
    boolean enableCollisions;
    boolean showHealthBar;
    boolean visible;
    boolean active;
    String changeTexture;
    float frameDuration;
    BasePart target;
    Array<Movement> movements;
    boolean hasMovement;
    JsonEntry config;
    
    private final Boss boss;
    boolean hasAiSettingsChange;
    boolean dodgeBullets;
    float bulletDodgeSpeed;
    int[] XMovementBounds;
    int[] YMovementBounds;
    boolean followPlayer;
    float playerFollowSpeed;
    float playerNotDamagedMaxTime;
    float playerInsideEntityMaxTime;
    float[] basePosition;
    
    Action(JsonEntry partGroups, JsonEntry actionValue, Array<BasePart> baseParts, Array<Movement> animations, Boss boss, String predeterminedTarget, Array<Action> actions) {
        
        this.boss = boss;
        config = actionValue;
        movements = new Array<>();
        String target;
        if (predeterminedTarget.equals("")) {
            Array<String> targets = new Array<>();
            targets.addAll(actionValue.getString(baseParts.get(0).name, "target").replace(" ", "").split(","));
            for (int i = 0; i < targets.size; i++) {
                if (targets.get(i).startsWith("group:")) {
                    targets.addAll(partGroups.getString("", targets.get(i).replace("group:", "")).replace(" ", "").split(","));
                    targets.removeIndex(i);
                }
            }
            if (targets.isEmpty()) {
                log("Invalid action target, empty list", ERROR);
            }
            target = targets.get(0);
            if (targets.size > 1) {
                for (int i = 1; i < targets.size; i++) {
                    actions.add(new Action(partGroups, actionValue, baseParts, animations, boss, targets.get(i), actions));
                }
            }
        } else {
            target = predeterminedTarget;
        }
        log("loading boss action, name: " + actionValue.name + ", target: " + target, INFO);
        for (int i = 0; i < baseParts.size; i++) {
            if (baseParts.get(i).name.equals(target)) {
                this.target = baseParts.get(i);
                break;
            }
        }
        if (this.target == null) {
            log("Invalid action target, target " + target + " not found", ERROR);
        }
        showHealthBar = actionValue.getBoolean(false, false, "showHealthBar");
        enableCollisions = actionValue.getBoolean(false, false, "enableCollisions");
        visible = actionValue.getBoolean(false, false, "visible");
        active = actionValue.getBoolean(false, false, "active");
        changeTexture = actionValue.getString(false, "false", "changeTexture");
        if (this.target.hasAnimation && !changeTexture.equals("false")) {
            frameDuration = actionValue.getFloat(this.target.enemyAnimation.getFrameDuration(), "frameDuration");
        }
        int movementCount;
        
        if (boss.hasAi && this.target.equals(boss.body)) {
            hasAiSettingsChange = actionValue.get(false, "ai").isObject();
            if (hasAiSettingsChange) {
                dodgeBullets = actionValue.get("ai").getBoolean(false, "dodgeBullets");
                if (dodgeBullets) {
                    bulletDodgeSpeed = actionValue.get("ai").getFloat(90, "bulletDodgeSpeed");
                }
                XMovementBounds = actionValue.get("ai").getIntArray(new int[]{400, 800}, "xBounds");
                YMovementBounds = actionValue.get("ai").getIntArray(new int[]{0, 480}, "yBounds");
                followPlayer = actionValue.get("ai").getBoolean(false, "followPlayer");
                if (followPlayer) {
                    playerFollowSpeed = actionValue.get("ai").getFloat(50, "playerFollowSpeed");
                    playerNotDamagedMaxTime = actionValue.get("ai").getFloat(10, "playerNotDamagedMaxTime");
                    playerInsideEntityMaxTime = actionValue.get("ai").getFloat(3, "playerInsideEntityMaxTime");
                }
                basePosition = actionValue.get("ai").getFloatArray(new float[]{400, 170}, "basePosition");
            } else {
                log("no ai change for " + actionValue.name, INFO);
            }
        }
        if (actionValue.get(false, "move").isObject()) {
            movementCount = actionValue.get("move").size;
            hasMovement = true;
            for (int i = 0; i < movementCount; i++) {
                Movement movement = new Movement(actionValue.get("move", i), target, baseParts, animations, boss.body);
                animations.add(movement);
                movements.add(movement);
            }
        } else {
            log("no movement for " + actionValue.name + ", target: " + target, INFO);
            hasMovement = false;
        }
    }
    
    void activate() {
        if (hasMovement) {
            for (int i = 0; i < movements.size; i++) {
                movements.get(i).start();
            }
        }
        if (target.hasCollision) {
            target.collisionEnabled = config.get(false, "enableCollisions").isNull() ? target.collisionEnabled : enableCollisions;
        }
        if (!changeTexture.equals("false")) {
            if (target.hasAnimation) {
                target.enemyAnimation = new Animation<>(
                        frameDuration,
                        target.textures.findRegions(target.name + "_" + changeTexture),
                        Animation.PlayMode.LOOP);
            } else {
                target.entitySprite.setRegion(target.textures.findRegion(changeTexture));
            }
        }
        target.visible = config.get(false, "visible").isNull() ? target.visible : visible;
        target.active = config.get(false, "active").isNull() ? target.active : active;
        target.showHealthBar = config.get(false, "showHealthBar").isNull() ? target.showHealthBar : showHealthBar;
        if (boss.hasAi && target.equals(boss.body)) {
            if (hasAiSettingsChange) {
                boss.bossAi.setSettings(dodgeBullets, bulletDodgeSpeed,
                        XMovementBounds, YMovementBounds, followPlayer, playerFollowSpeed, playerNotDamagedMaxTime, playerInsideEntityMaxTime, new Vector2(basePosition[0], basePosition[1]));
            }
        }
    }
}

class PhaseTrigger {
    
    boolean conditionsMet;
    boolean isResetPhase;
    float value;
    String triggerType;
    String triggerModifier;
    BasePart triggerTarget;
    
    PhaseTrigger(JsonEntry triggerData, Array<BasePart> parts) {
        
        log("loading boss phase trigger, trigger name: " + triggerData.name + ", phase name: " + triggerData.parent().parent().name, INFO);
        
        isResetPhase = triggerData.parent().parent().name.equals("RESET");
        
        conditionsMet = false;
        triggerType = triggerData.getString("health", "triggerType");
        value = triggerData.getFloat(1, "value");
        String targetPart = triggerData.getString(parts.get(0).name, "target");
        for (int i2 = 0; i2 < parts.size; i2++) {
            if (parts.get(i2).name.equals(targetPart)) {
                triggerTarget = parts.get(i2);
                break;
            }
        }
        if (triggerTarget == null) {
            log("error setting up trigger for part " + targetPart + ", no valid trigger target", ERROR);
        }
        triggerModifier = triggerData.getString("<=", "triggerModifier");
    }
    
    void reset() {
        conditionsMet = false;
    }
    
    void update() {
        float partValue = 0;
        switch (triggerType) {
            case ("positionX"):
                partValue = triggerTarget.x + triggerTarget.movementOffsetX;
                break;
            case ("positionY"):
                partValue = triggerTarget.y + triggerTarget.movementOffsetY;
                break;
            case ("rotation"):
                partValue = triggerTarget.rotation + triggerTarget.movementRotation;
                break;
            case ("health"):
                partValue = triggerTarget.health;
                break;
        }
        switch (triggerModifier) {
            case ("<"):
                if (partValue < value) {
                    conditionsMet = true;
                }
                break;
            case (">"):
                if (partValue > value) {
                    conditionsMet = true;
                }
                break;
            case ("<="):
                if (partValue <= value) {
                    conditionsMet = true;
                }
                break;
            case (">="):
                if (partValue >= value) {
                    conditionsMet = true;
                }
                break;
        }
    }
}
