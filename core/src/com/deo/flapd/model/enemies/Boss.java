package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Drops;
import com.deo.flapd.model.ShipObject;
import com.deo.flapd.model.UraniumCell;
import com.deo.flapd.model.bullets.BulletData;
import com.deo.flapd.model.bullets.EnemyBullet;
import com.deo.flapd.utils.JsonEntry;

import static com.deo.flapd.utils.DUtils.constructFilledImageWithColor;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.putBoolean;

public class Boss {
    
    public JsonEntry bossConfig;
    private final Array<BasePart> parts;
    private final Array<Phase> phases;
    private final int[] spawnAt;
    boolean visible;
    ShipObject player;
    Array<Movement> animations;
    public boolean hasAlreadySpawned;
    private final int spawnScore;
    public BasePart body;
    
    public String bossName;
    
    Boss(String bossName, AssetManager assetManager) {
        
        log("\n\n loading boss config, name: " + bossName);
        
        this.bossName = bossName;
        
        bossConfig = new JsonEntry(new JsonReader().parse(Gdx.files.internal("enemies/bosses/" + bossName + "/config.json")));
        TextureAtlas bossAtlas = assetManager.get("enemies/bosses/" + bossName + "/" + bossConfig.getString("noTextures", "textures"));
        parts = new Array<>();
        animations = new Array<>();
        hasAlreadySpawned = getBoolean("boss_spawned_" + bossName);
        spawnScore = bossConfig.getInt(-1, "spawnConditions", "score") + getRandomInRange(-bossConfig.getInt(0, "spawnConditions", "randomness"), bossConfig.getInt(0, "spawnConditions", "randomness"));
        spawnAt = bossConfig.getIntArray(new int[]{500, 500}, "spawnAt");
        for (int i = 0; i < bossConfig.get("parts").size; i++) {
            String type = bossConfig.get("parts", i).getString("part", "type");
            switch (type) {
                case ("basePart"):
                    body = new BasePart(bossConfig.get("parts", i), bossAtlas);
                    parts.add(body);
                    break;
                case ("part"):
                    parts.add(new Part(bossConfig.get("parts", i), bossAtlas, parts, body));
                    break;
                case ("cannon"):
                    parts.add(new Cannon(bossConfig.get("parts", i), bossAtlas, parts, body, assetManager));
                    break;
                case ("clone"):
                    String copyFrom = bossConfig.get("parts", i).getString(parts.get(0).name, "copyFrom");
                    String cloneType = bossConfig.getString("part", "parts", copyFrom, "type");
                    switch (cloneType) {
                        case ("part"):
                            parts.add(new Part(bossConfig.get("parts", i), bossAtlas, parts, body));
                            break;
                        case ("cannon"):
                            parts.add(new Cannon(bossConfig.get("parts", i), bossAtlas, parts, body, assetManager));
                            break;
                        default:
                            log("\n Can't copy base part");
                            break;
                    }
                    break;
                default:
                    log("\n Unknown path type: " + type);
                    break;
            }
        }
        
        Array<Array<BasePart>> sortedParts = new Array<>();
        
        int maxLayer = 0;
        
        for (int i = 0; i < parts.size; i++) {
            maxLayer = Math.max(maxLayer, parts.get(i).layer);
        }
        
        maxLayer += 1;
        
        sortedParts.setSize(maxLayer);
        
        for (int i = 0; i < maxLayer; i++) {
            sortedParts.set(i, new Array<BasePart>());
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
        
        for (int i = 0; i < bossConfig.get("phases").size; i++) {
            Phase phase = new Phase(bossConfig.get("phases", i), parts, animations, this);
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
    
    void update(float delta) {
        if (GameLogic.Score >= spawnScore && !hasAlreadySpawned) {
            spawn();
        }
        if (visible) {
            for (int i = 0; i < parts.size; i++) {
                parts.get(i).x = body.x + parts.get(i).offsetX;
                parts.get(i).y = body.y + parts.get(i).offsetY;
                parts.get(i).update(delta);
            }
            for (int i = 0; i < animations.size; i++) {
                animations.get(i).update(delta);
            }
            for (int i = 0; i < phases.size; i++) {
                phases.get(i).update();
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
    
    void setTargetPlayer(ShipObject player) {
        this.player = player;
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
    
    float originalHealth;
    float originalOffsetX = 0;
    float originalOffsetY = 0;
    String name;
    String type;
    JsonEntry currentConfig;
    ProgressBar healthBar;
    ShipObject player;
    boolean collisionEnabled = true;
    boolean hasCollision;
    boolean visible;
    boolean active;
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
    
    BasePart(JsonEntry newConfig, TextureAtlas textures) {
        
        log("\n loading boss part, name: " + newConfig.name);
        
        exploded = false;
        name = newConfig.name;
        currentConfig = newConfig;
        this.textures = textures;
        links = new Array<>();
        if (newConfig.getString("part", "type").equals("clone")) {
            currentConfig = newConfig.parent().get(newConfig.getString("noCopyFromTarget", "copyFrom"));
        }
        type = currentConfig.getString("part", "type");
        health = currentConfig.getInt(1, "health");
        originalHealth = health;
        width = currentConfig.getFloat(1, "width");
        height = currentConfig.getFloat(1, "height");
        layer = currentConfig.getInt(0, "layer");
        hasCollision = currentConfig.getBoolean(false, "hasCollision");
        
        if (hasCollision) {
            itemRarity = currentConfig.getIntArray(new int[]{1, 2}, "drops", "items", "rarity");
            itemCount = currentConfig.getIntArray(new int[]{1, 2}, "drops", "items", "count");
            itemTimer = currentConfig.getFloat(1, "drops", "items", "timer");
            
            bonusChance = currentConfig.getInt(50, "drops", "bonuses", "chance");
            bonusType = currentConfig.getIntArray(new int[]{1, 2}, "drops", "bonuses", "type");
            
            moneyCount = currentConfig.getIntArray(new int[]{3, 5}, "drops", "money", "count");
            moneyTimer = currentConfig.getFloat(1, "drops", "items", "timer");
        }
        
        if (hasCollision) {
            explosionSound = Gdx.audio.newSound(Gdx.files.internal(currentConfig.getString("sfx/explosion.ogg", "explosionSound")));
        }
        soundVolume = getFloat("soundVolume");
        
        entitySprite = new Sprite(textures.findRegion(currentConfig.getString("noTexture", "texture")));
        
        originX = width / 2f;
        originY = height / 2f;
        if (!currentConfig.getString("standard", "originX").equals("standard")) {
            originX = currentConfig.getFloat(0, "originX");
        }
        if (!currentConfig.getString("standard", "originY").equals("standard")) {
            originY = currentConfig.getFloat(0, "originY");
        }
        if (!hasCollision) {
            collisionEnabled = false;
        }
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
        healthBar.setValue(health);
        
        if (hasCollision) {
            explosionEffect = new ParticleEffect();
            explosionEffect.load(Gdx.files.internal(currentConfig.getString("particles/explosion.p", "explosionEffect")), Gdx.files.internal("particles"));
            log("\n creating explosion effect for part: " + newConfig.name);
        }
        
    }
    
    void draw(SpriteBatch batch, float delta) {
        if (visible) {
            entitySprite.draw(batch);
            if (showHealthBar) {
                healthBar.draw(batch, 1);
            }
        }
        if (exploded) {
            explosionEffect.draw(batch);
        }
    }
    
    void update(float delta) {
        super.update();
        if (showHealthBar) {
            healthBar.setValue(health);
            healthBar.setPosition(width / 2 + x - 12.5f, y - 7);
            healthBar.act(delta);
        }
        if (hasCollision && collisionEnabled && health > 0) {
            for (int i = 0; i < player.bullet.bullets.size; i++) {
                if (player.bullet.bullets.get(i).overlaps(entitySprite.getBoundingRectangle())) {
                    health -= player.bullet.damages.get(i);
                    player.bullet.removeBullet(i, true);
                }
            }
        }
        if (health <= 0 && !exploded) {
            explode();
            for (int i = 0; i < links.size; i++) {
                if (!links.get(i).exploded && links.get(i).explosionEffect != null) {
                    links.get(i).explode();
                } else {
                    links.get(i).active = false;
                }
            }
        }
        if (exploded) {
            explosionEffect.update(delta);
        }
    }
    
    void setTargetPlayer(ShipObject player) {
        this.player = player;
    }
    
    void dispose() {
        if (hasCollision) {
            explosionEffect.dispose();
            explosionSound.dispose();
        }
    }
    
    void addLinkedPart(BasePart part) {
        links.add(part);
    }
    
    void explode() {
        if (hasCollision) {
            UraniumCell.Spawn(entityHitBox, getRandomInRange(moneyCount[0], moneyCount[1]), 1, moneyTimer);
            
            if (getRandomInRange(0, 100) <= bonusChance) {
                Bonus.Spawn(getRandomInRange(bonusType[0], bonusType[1]), entityHitBox);
            }
            
            Drops.drop(entityHitBox, getRandomInRange(itemCount[0], itemCount[1]), itemTimer, getRandomInRange(itemRarity[0], itemRarity[1]));
        }
        
        explosionEffect.setPosition(x + width / 2, y + height / 2);
        explosionEffect.start();
        if (soundVolume > 0) {
            explosionSound.play(soundVolume);
        }
        exploded = true;
        active = false;
        
    }
    
    void reset() {
        offsetX = originalOffsetX;
        offsetY = originalOffsetY;
        health = originalHealth;
        showHealthBar = false;
        visible = false;
        active = false;
        collisionEnabled = false;
        exploded = false;
        rotation = 0;
    }
}

class Part extends BasePart {
    
    private final Array<BasePart> parts;
    private float relativeX, relativeY;
    private BasePart link;
    private final BasePart body;
    
    Part(JsonEntry newConfig, TextureAtlas textures, Array<BasePart> parts, BasePart body) {
        super(newConfig, textures);
        this.parts = parts;
        link = body;
        this.body = body;
        String relativeTo = currentConfig.getString(parts.get(0).name, "offset", "relativeTo");
        relativeX = currentConfig.getFloat(0, "offset", "X");
        relativeY = currentConfig.getFloat(0, "offset", "Y");
        if (newConfig.getString("part", "type").equals("clone") && newConfig.get("override") != null) {
            for (int i = 0; i < newConfig.get("override").size; i++) {
                if (newConfig.get("override", i).name.equals("offset")) {
                    relativeX = newConfig.get("override", i).getFloat(0, "X");
                    relativeY = newConfig.get("override", i).getFloat(0, "Y");
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
            }
            
        }
        
        boolean customLink = false;
        
        if (!currentConfig.get("linked").isBoolean()) {
            for (int i = 0; i < parts.size; i++) {
                if (currentConfig.getString("false", "linked").equals(parts.get(i).name)) {
                    link = parts.get(i);
                    link.addLinkedPart(this);
                    customLink = true;
                    break;
                }
            }
        }
        
        if (!customLink) {
            link.addLinkedPart(this);
        }
        
        getOffset(relativeTo);
        offsetX = relativeX;
        offsetY = relativeY;
        
        originalOffsetX = relativeX;
        originalOffsetY = relativeY;
        
    }
    
    private void getOffset(String relativeTo) {
        for (int i = 0; i < parts.size; i++) {
            if (parts.get(i).name.equals(relativeTo)) {
                relativeX += parts.get(i).offsetX;
                relativeY += parts.get(i).offsetY;
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

class Cannon extends Part {
    
    boolean canAim;
    boolean isLaser;
    float maxLaserDistance;
    float aimingSpeed;
    float laserLifetime;
    boolean lockOntoTarget;
    int[] aimAngleLimit;
    float fireRate;
    float timer;
    
    boolean drawBulletsOnTop = false;
    
    BulletData bulletData;
    Array<EnemyBullet> bullets;
    AssetManager assetManager;
    TextureAtlas textures;
    
    Sound shootingSound;
    
    Cannon(JsonEntry partConfig, TextureAtlas textures, Array<BasePart> parts, BasePart body, AssetManager assetManager) {
        super(partConfig, textures, parts, body);
        
        this.assetManager = assetManager;
        this.textures = textures;
        
        bullets = new Array<>();
        
        isLaser = currentConfig.getBoolean(false, "laser");
        
        drawBulletsOnTop = currentConfig.getBoolean(false, "drawBulletsOnTop");
        
        canAim = currentConfig.getBoolean(false, "canAim");
        aimAngleLimit = currentConfig.getIntArray(new int[]{-360, 360}, "aimAngleLimit");
        
        float fireRateRandomness = currentConfig.getFloat(0, "fireRate", "randomness");
        fireRate = currentConfig.getFloat(1, "fireRate", "baseRate") + getRandomInRange((int) (-fireRateRandomness * 10), (int) (fireRateRandomness * 10)) / 10f;
        
        bulletData = new BulletData(currentConfig.get("bullet"));
        
        shootingSound = Gdx.audio.newSound(Gdx.files.internal(bulletData.shootSound));
        
    }
    
    @Override
    void update(float delta) {
        super.update(delta);
        if (active) {
            if (canAim) {
                rotation = MathUtils.clamp(MathUtils.radiansToDegrees * MathUtils.atan2(y - (player.bounds.getY() + player.bounds.getHeight() / 2), x - (player.bounds.getX() + player.bounds.getWidth() / 2)), aimAngleLimit[0], aimAngleLimit[1]);
            }
            timer += delta * fireRate;
            if (timer > 1 && health > 0 && visible) {
                shoot();
                timer = 0;
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
        if (!drawBulletsOnTop) {
            super.draw(batch, delta);
        }
    }
    
    void shoot() {
        for (int i = 0; i < bulletData.bulletsPerShot; i++) {
            BulletData newBulletData = new BulletData(currentConfig.get("bullet"));
            
            float newX = x + width / 2f - bulletData.width / 2f + MathUtils.cosDeg(rotation + bulletData.bulletAngle) * newBulletData.bulletDistance;
            float newY = y + height / 2f - bulletData.height / 2f + MathUtils.sinDeg(rotation + bulletData.bulletAngle) * newBulletData.bulletDistance;
            float newRot = 0;
            if (canAim) {
                newRot = MathUtils.clamp(MathUtils.radiansToDegrees * MathUtils.atan2(y - (player.bounds.getY() + player.bounds.getHeight() / 2), x - (player.bounds.getX() + player.bounds.getWidth() / 2)), aimAngleLimit[0], aimAngleLimit[1]);
            }
            
            newRot += getRandomInRange(-10, 10) * bulletData.spread;
            
            EnemyBullet bullet = new EnemyBullet(textures, newBulletData, player);
            bullet.x = newX;
            bullet.y = newY;
            bullet.rotation = newRot;
            
            bullets.add(bullet);
            
        }
        
        if (soundVolume > 0) {
            shootingSound.play();
        }
        
    }
    
    @Override
    void dispose() {
        super.dispose();
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).dispose();
        }
        shootingSound.dispose();
    }
}

class Movement {
    
    private final byte NEGATIVE = -1;
    private final byte POSITIVE = 1;
    private final float moveBy;
    private float currentAddPosition;
    private final float speed;
    private float progress;
    private boolean active;
    private BasePart target;
    
    private BasePart relativeTarget;
    private float radiusExpansionRate;
    private float currentRadius;
    private float targetRadius;
    private float angleOffset;
    
    private final String type;
    private final byte typeModifier;
    private final boolean stopPrevAnim;
    
    private final Array<Movement> animations;
    
    private final BasePart body;
    
    Movement(JsonEntry movementConfig, String target, Array<BasePart> parts, Array<Movement> animations, BasePart body) {
        
        log("\n loading boss movement, name: " + movementConfig.name);
        
        this.body = body;
        
        active = false;
        this.animations = animations;
        for (int i = 0; i < parts.size; i++) {
            if (parts.get(i).name.equals(target)) {
                this.target = parts.get(i);
                break;
            }
        }
        speed = movementConfig.getFloat(100, "speed");
        
        stopPrevAnim = movementConfig.getBoolean(false, "stopPreviousAnimations");
        
        if (movementConfig.getString("inf", "moveBy").equals("inf")) {
            moveBy = 999779927;
        } else {
            moveBy = movementConfig.getFloat(999779927, "moveBy");
        }
        
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
            angleOffset = movementConfig.getFloat(60, "angleOffset");
        }
        
        if (currentAddPosition < moveBy) {
            typeModifier = POSITIVE;
        } else {
            typeModifier = NEGATIVE;
        }
    }
    
    void start() {
        if (stopPrevAnim) {
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
            if (currentAddPosition < moveBy && typeModifier == POSITIVE) {
                currentAddPosition += speed * delta * 10;
                if (currentAddPosition >= moveBy) {
                    active = false;
                }
            }
            if (currentAddPosition > moveBy && typeModifier == NEGATIVE) {
                currentAddPosition -= speed * delta * 10;
                if (currentAddPosition <= moveBy) {
                    active = false;
                }
            }
            
            switch (type) {
                case ("moveLinearX"):
                    if (typeModifier == POSITIVE) {
                        target.x += speed * delta * 10;
                        if (!target.getClass().equals(BasePart.class)) {
                            target.offsetX += speed * delta * 10;
                        }
                    } else if (typeModifier == NEGATIVE) {
                        target.x -= speed * delta * 10;
                        if (!target.getClass().equals(BasePart.class)) {
                            target.offsetX -= speed * delta * 10;
                        }
                    }
                    break;
                case ("moveLinearY"):
                    if (typeModifier == POSITIVE) {
                        target.y += speed * delta * 10;
                        if (!target.getClass().equals(BasePart.class)) {
                            target.offsetY += speed * delta * 10;
                        }
                    } else if (typeModifier == NEGATIVE) {
                        target.y -= speed * delta * 10;
                        if (!target.getClass().equals(BasePart.class)) {
                            target.offsetY -= speed * delta * 10;
                        }
                    }
                    break;
                case ("rotate"):
                    if (typeModifier == POSITIVE) {
                        target.rotation += speed * delta * 10;
                    } else if (typeModifier == NEGATIVE) {
                        target.rotation -= speed * delta * 10;
                    }
                    break;
                case ("moveSinX"): {
                    target.x += MathUtils.sinDeg(progress) * moveBy * delta * 60;
                    if (!target.getClass().equals(BasePart.class)) {
                        target.offsetX -= MathUtils.sinDeg(progress) * moveBy * delta * 60;
                    }
                    progress += speed * delta * 10;
                    break;
                }
                case ("moveSinY"): {
                    target.y += MathUtils.sinDeg(progress) * moveBy * delta * 60;
                    if (!target.getClass().equals(BasePart.class)) {
                        target.offsetY -= MathUtils.sinDeg(progress) * moveBy * delta * 60;
                    }
                    progress += speed * delta * 10;
                    break;
                }
                case ("rotateSin"): {
                    target.rotation = MathUtils.sinDeg(progress) * moveBy * delta * 60;
                    progress += speed * delta * 10;
                    break;
                }
                case ("rotateRelativeTo"): {
                    if (relativeTarget.equals(body)) {
                        target.offsetX = body.originX - target.originX + MathUtils.cosDeg(progress + angleOffset) * currentRadius;
                        target.offsetY = body.originY - target.originY + MathUtils.sinDeg(progress + angleOffset) * currentRadius;
                    } else {
                        target.offsetX = relativeTarget.offsetX + MathUtils.cosDeg(progress + angleOffset) * currentRadius;
                        target.offsetY = relativeTarget.offsetY + MathUtils.sinDeg(progress + angleOffset) * currentRadius;
                    }
                    if (progress < moveBy) {
                        progress = MathUtils.clamp(progress + speed * delta * 10, 0, moveBy);
                    }
                    if (currentRadius < targetRadius) {
                        currentRadius = MathUtils.clamp(currentRadius + radiusExpansionRate * delta * 10, 0, targetRadius);
                    }
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
    
    Phase(JsonEntry phaseData, Array<BasePart> parts, Array<Movement> animations, Boss boss) {
        
        log("\n loading boss phase, name: " + phaseData.name);
        
        this.boss = boss;
        
        actions = new Array<>();
        phaseTriggers = new Array<>();
        activated = false;
        config = phaseData;
        for (int i = 0; i < phaseData.size; i++) {
            Action action = new Action(phaseData.get(i), parts, animations, boss.body);
            actions.add(action);
        }
        JsonEntry triggers = null;
        try {
            triggers = phaseData.parent().parent().get("phaseTriggers", config.name, "triggers");
        } catch (Exception e) {
            log("\n no triggers for " + phaseData.name);
        }
        if (triggers != null) {
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
    boolean enabled;
    String changeTexture;
    BasePart target;
    Array<Movement> movements;
    boolean hasMovement;
    JsonEntry config;
    
    Action(JsonEntry actionValue, Array<BasePart> baseParts, Array<Movement> animations, BasePart body) {
        
        log("\n loading boss action, name: " + actionValue.name);
        
        config = actionValue;
        movements = new Array<>();
        String target = actionValue.getString(baseParts.get(0).name, "target");
        for (int i = 0; i < baseParts.size; i++) {
            if (baseParts.get(i).name.equals(target)) {
                this.target = baseParts.get(i);
                break;
            }
        }
        showHealthBar = actionValue.getBoolean(false, "showHealthBar");
        enableCollisions = actionValue.getBoolean(false, "enableCollisions");
        visible = actionValue.getBoolean(true, "visible");
        enabled = actionValue.getBoolean(false, "active");
        changeTexture = actionValue.getString("false", "changeTexture");
        
        int movementCount;
        
        if (actionValue.get("move").isBoolean()) {
            log("\n no movement for " + actionValue.name);
            hasMovement = false;
        } else {
            movementCount = actionValue.get("move").size;
            hasMovement = true;
            for (int i = 0; i < movementCount; i++) {
                Movement movement = new Movement(actionValue.get("move", i), target, baseParts, animations, body);
                animations.add(movement);
                movements.add(movement);
            }
        }
    }
    
    void activate() {
        if (hasMovement) {
            for (int i = 0; i < movements.size; i++) {
                movements.get(i).start();
            }
        }
        if (target.hasCollision) {
            target.collisionEnabled = enableCollisions;
        }
        if (!changeTexture.equals("false")) {
            target.entitySprite.setRegion(target.textures.findRegion(changeTexture));
        }
        target.visible = visible;
        target.active = enabled;
        target.showHealthBar = showHealthBar;
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
        
        log("\n loading boss phase trigger, trigger name: " + triggerData.name + ", phase name: " + triggerData.parent().parent().name);
        
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
            log("\n error setting up trigger for part " + targetPart);
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
                partValue = triggerTarget.x;
                break;
            case ("positionY"):
                partValue = triggerTarget.y;
                break;
            case ("rotation"):
                partValue = triggerTarget.rotation;
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
