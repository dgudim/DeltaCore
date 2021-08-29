package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
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
import com.badlogic.gdx.utils.TimeUtils;
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
import com.deo.flapd.utils.MusicManager;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.badlogic.gdx.math.MathUtils.random;
import static com.deo.flapd.model.enemies.Bosses.secondThread;
import static com.deo.flapd.model.enemies.Bosses.stopThread;
import static com.deo.flapd.utils.DUtils.LogLevel.DEBUG;
import static com.deo.flapd.utils.DUtils.LogLevel.ERROR;
import static com.deo.flapd.utils.DUtils.LogLevel.WARNING;
import static com.deo.flapd.utils.DUtils.constructFilledImageWithColor;
import static com.deo.flapd.utils.DUtils.convertPercentsToAbsoluteValue;
import static com.deo.flapd.utils.DUtils.drawParticleEffectBounds;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getDistanceBetweenTwoPoints;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.getTargetsFromGroup;
import static com.deo.flapd.utils.DUtils.lerpAngleWithConstantSpeed;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.view.GameScreen.is_paused;
import static com.deo.flapd.view.LoadingScreen.particleEffectPoolLoader;
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
    
    String bossMusic;
    MusicManager musicManager;
    
    Boss(String bossName, AssetManager assetManager, MusicManager musicManager) {
        log("------------------------------------------------------\n", DEBUG);
        log("---------loading " + bossName, DEBUG);
        long genTime = TimeUtils.millis();
        
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
        bossMusic = bossConfig.getString("", "music");
        this.musicManager = musicManager;
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
                    
                    JsonEntry config = bossConfig.get("parts", i);
                    
                    JsonEntry barrelsJsonEntry = config.get(false, "barrels");
                    if (barrelsJsonEntry.isNull()) {
                        JsonValue toAdd = new JsonValue(JsonValue.ValueType.object);
                        toAdd.setName("barrels");
                        JsonValue toAdd_mainBarrel = new JsonValue(JsonValue.ValueType.object);
                        toAdd_mainBarrel.setName(config.name);
                        toAdd_mainBarrel.addChild("texture", new JsonValue(config.getString("noTexture", "texture")));
                        float width = config.getFloat(false, 1, "width");
                        float height = config.getFloat(false, 1, "height");
                        float originX = width / 2f;
                        float originY = height / 2f;
                        if (!config.getString(false, "standard", "originX").equals("standard")) {
                            originX = config.getFloat(0.5f, "originX");
                        }
                        if (!config.getString(false, "standard", "originY").equals("standard")) {
                            originY = config.getFloat(0.5f, "originX");
                        }
                        toAdd_mainBarrel.addChild("width", new JsonValue(width));
                        toAdd_mainBarrel.addChild("height", new JsonValue(height));
                        JsonValue toAdd_mainBarrel_offset = new JsonValue(JsonValue.ValueType.array);
                        toAdd_mainBarrel_offset.addChild(new JsonValue(width / 2f - originX));
                        toAdd_mainBarrel_offset.addChild(new JsonValue(height / 2f - originY));
                        toAdd_mainBarrel.addChild("offset", toAdd_mainBarrel_offset);
                        toAdd.addChild(toAdd_mainBarrel);
                        if (!config.getString("noTexture", "texture").equals("noTexture")) {
                            config.jsonValue.get("texture").set("noTexture");
                        }
                        
                        config.addValue(new JsonEntry(toAdd));
                    }
                    
                    parts.add(new Cannon(config, bossAtlas, parts, body, assetManager));
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
                        case ("shield"):
                            parts.add(new Shield(bossConfig.get("parts", i), bossAtlas, parts, body, assetManager));
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
        
        parts.sort((basePart, basePart2) -> basePart.layer - basePart2.layer);
        
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
        
        log("------------------------------------------------------", DEBUG);
        log("loaded " + bossName + " in " + TimeUtils.timeSinceMillis(genTime) + "ms", DEBUG);
        log(parts.size + " parts", DEBUG);
        log(phases.size + " phases", DEBUG);
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
        if(!bossMusic.equals("")){
            musicManager.setNewMusicSource(bossMusic, 1);
        }
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
    
        this.musicManager.setNewMusicSource("music/main", 1, 5, 5);
        
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
    boolean hasAnimation;
    
    AssetManager assetManager;
    
    float movementOffsetX = 0;
    float movementOffsetY = 0;
    float movementRotation = 0;
    String name;
    String type;
    JsonEntry currentConfig;
    ProgressBar healthBar;
    Player player;
    boolean collisionEnabled;
    boolean hasCollision;
    boolean visible = true;
    boolean showHealthBar;
    
    int layer;
    
    ParticleEffectPool.PooledEffect explosionEffect;
    boolean exploded;
    
    TextureAtlas textures;
    
    Array<BasePart> links;
    
    int[] bonusType, itemRarity, itemCount, moneyCount;
    float itemTimer, moneyTimer;
    int bonusChance;
    
    Sound explosionSound;
    float soundVolume;
    
    BasePart(JsonEntry newConfig, TextureAtlas textures, AssetManager assetManager) {
        
        log("loading " + newConfig.name, DEBUG);
        
        active = false;
        this.assetManager = assetManager;
        
        exploded = false;
        name = newConfig.name;
        currentConfig = newConfig;
        this.textures = textures;
        links = new Array<>();
        if (newConfig.getString("part", "type").equals("clone")) {
            JsonEntry copyFrom = newConfig.parent().get(newConfig.getString("noCopyFromTarget", "copyFrom"));
            if (!copyFrom.getString("", "type").equals("")) {
                newConfig.removeValue("type");
            }
            for (int i = 0; i < copyFrom.size; i++) {
                newConfig.addValue(copyFrom.get(i));
            }
            for (int i = 0; i < newConfig.get(false, "override").size; i++) {
                newConfig.replaceValue(newConfig.get(false, "override").get(i));
            }
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
        layer = currentConfig.getInt(false, 0, "layer");
        
        if (hasCollision && !type.equals("shield")) {
            itemRarity = currentConfig.getIntArray(new int[]{1, 2}, "drops", "items", "rarity");
            itemCount = currentConfig.getIntArray(new int[]{1, 2}, "drops", "items", "count");
            itemTimer = currentConfig.getFloat(1, "drops", "items", "timer");
            
            bonusChance = currentConfig.getInt(50, "drops", "bonuses", "chance");
            bonusType = currentConfig.getIntArray(new int[]{1, 2}, "drops", "bonuses", "type");
            
            moneyCount = currentConfig.getIntArray(new int[]{3, 5}, "drops", "money", "count");
            moneyTimer = currentConfig.getFloat(1, "drops", "items", "timer");
            
            explosionSound = assetManager.get(currentConfig.getString("sfx/explosion.ogg", "explosionSound"));
            explosionEffect = particleEffectPoolLoader.getParticleEffectByPath(currentConfig.getString("particles/explosion.p", "explosionEffect"));
            explosionEffect.scaleEffect(currentConfig.getFloat(1, "explosionScale"));
            log("creating explosion effect for " + newConfig.name, DEBUG);
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
            explosionEffect.free();
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
    BasePart link;
    final BasePart body;
    
    Part(JsonEntry newConfig, TextureAtlas textures, Array<BasePart> parts, BasePart body, AssetManager assetManager) {
        super(newConfig, textures, assetManager);
        this.parts = parts;
        link = body;
        this.body = body;
        String relativeTo = currentConfig.getString(false, body.name, "offset", "relativeTo");
        offsetX = currentConfig.getFloat(false, 0, "offset", "X");
        offsetY = currentConfig.getFloat(false, 0, "offset", "Y");
        
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
    float currentAimAngle;
    float aimingSpeed;
    String aimAnimationType;
    String[] aimTextures;
    int[] aimAngleLimit;
    
    Array<Barrel> barrels;
    
    Cannon(JsonEntry partConfig, TextureAtlas textures, Array<BasePart> parts, BasePart body, AssetManager assetManager) {
        super(partConfig, textures, parts, body, assetManager);
        
        canAim = currentConfig.getBoolean(false, false, "canAim");
        if (canAim) {
            if (aimAngleLimit == null) {
                aimAngleLimit = currentConfig.getIntArray(new int[]{-360, 360}, "aimAngleLimit");
            }
            
            aimingSpeed = currentConfig.getFloat(false, 50, "aimingSpeed");
            
            aimAnimationType = currentConfig.getString(false, "noAnimation", "aimAnimation");
            
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
        
        JsonEntry barrelsJsonEntry = currentConfig.get(false, "barrels");
        barrels = new Array<>();
        for (int i = 0; i < barrelsJsonEntry.size; i++) {
            log("loading barrel " + barrelsJsonEntry.get(i).name + " for " + name, DEBUG);
            barrels.add(new Barrel(textures, barrelsJsonEntry.get(i), this));
        }
        
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
    
    @Override
    protected void update(float delta) {
        super.update(delta);
        if (active) {
            if (canAim) {
                currentAimAngle = lerpAngleWithConstantSpeed(currentAimAngle, clamp(MathUtils.radiansToDegrees * MathUtils.atan2(
                        y + originY - (player.bounds.getY() + player.bounds.getHeight() / 2),
                        x + originX - (player.bounds.getX() + player.bounds.getWidth() / 2)),
                        aimAngleLimit[0], aimAngleLimit[1]), aimingSpeed, delta);
                
                switch (aimAnimationType) {
                    case ("textureChange"):
                        
                        String texture;
                        if (currentAimAngle >= -22.5 && currentAimAngle <= 22.5) {
                            texture = aimTextures[1];
                        } else if (currentAimAngle >= 22.5 && currentAimAngle <= 67.5) {
                            texture = aimTextures[7];
                        } else if (currentAimAngle >= 67.5 && currentAimAngle <= 112.5) {
                            texture = aimTextures[3];
                        } else if (currentAimAngle >= 112.5 && currentAimAngle <= 157.5) {
                            texture = aimTextures[6];
                        } else if ((currentAimAngle >= 157.5 && currentAimAngle <= 180) || (currentAimAngle >= -180 && currentAimAngle <= -157.5)) {
                            texture = aimTextures[0];
                        } else if (currentAimAngle >= -157.5 && currentAimAngle <= -112.5) {
                            texture = aimTextures[4];
                        } else if (currentAimAngle >= -112.5 && currentAimAngle <= -67.5) {
                            texture = aimTextures[2];
                        } else {
                            texture = aimTextures[5];
                        }
                        if (hasAnimation) {
                            String[] atlasAndFrameDuration = texture.replace(" ", "").split(",");
                            float frameDuration = Float.parseFloat(atlasAndFrameDuration[1]);
                            enemyAnimation = new Animation<>(
                                    frameDuration,
                                    textures.findRegions(name + "_" + atlasAndFrameDuration[0]),
                                    Animation.PlayMode.LOOP);
                        } else {
                            entitySprite.setRegion(textures.findRegion(texture));
                        }
                        break;
                    case ("rotate"):
                        rotation = currentAimAngle;
                        break;
                }
            }
        }
        for (int i = 0; i < barrels.size; i++) {
            barrels.get(i).update(delta);
        }
    }
    
    @Override
    void draw(SpriteBatch batch, float delta) {
        for (int i = 0; i < barrels.size; i++) {
            barrels.get(i).draw(batch, delta);
        }
        super.draw(batch, delta);
        for (int i = 0; i < barrels.size; i++) {
            barrels.get(i).drawPowerUpEffect(batch);
        }
    }
    
    @Override
    public void drawDebug(ShapeRenderer shapeRenderer) {
        super.drawDebug(shapeRenderer);
        for (int i = 0; i < barrels.size; i++) {
            barrels.get(i).drawDebug(shapeRenderer);
        }
    }
    
    @Override
    void dispose() {
        super.dispose();
        for (int i = 0; i < barrels.size; i++) {
            barrels.get(i).dispose();
        }
    }
}

class Barrel extends Entity {
    
    Animation<TextureRegion> barrelAnimation;
    private float animationPosition;
    boolean hasAnimation;
    
    float[] bulletOffset;
    float bulletOffsetAngle;
    float bulletOffsetDistance;
    
    int bulletsPerShot;
    int burstSpacing;
    float bulletSpread;
    
    float fireRate;
    float fireTimer;
    
    boolean drawBulletsOnTop;
    
    BulletData bulletData;
    Array<EnemyBullet> bullets;
    
    Sound shootingSound;
    
    boolean powerUpActive;
    ParticleEffectPool.PooledEffect powerUpEffect;
    boolean hasPowerUpEffect;
    String powerUpEffectPath;
    float powerUpEffectShootDelay;
    float powerUpScale;
    float powerUpOffsetAngle;
    float powerUpOffsetDistance;
    
    boolean powerDownActive;
    ParticleEffectPool.PooledEffect powerDownEffect;
    boolean hasPowerDownEffect;
    String powerDownEffectPath;
    float powerDownScale;
    float powerDownOffsetAngle;
    float powerDownOffsetDistance;
    
    float recoil;
    float currentRecoilOffset;
    float recoilReturnSpeed;
    
    float offsetAngle;
    float offsetDistance;
    
    Cannon base;
    
    Barrel(TextureAtlas textures, JsonEntry config, Cannon base) {
        
        bullets = new Array<>();
        
        JsonEntry baseConfig = base.currentConfig;
        drawBulletsOnTop = config.getBooleanWithFallback(baseConfig, false, false, "drawBulletsOnTop");
        
        this.base = base;
        
        hasAnimation = config.getBooleanWithFallback(baseConfig, false, false, "hasAnimation");
        String texture = config.getString("noTexture", "texture");
        if (hasAnimation) {
            entitySprite = new Sprite();
            barrelAnimation = new Animation<>(
                    config.getFloatWithFallback(baseConfig, true, 1, "frameDuration"),
                    textures.findRegions(config.name + "_" + texture),
                    Animation.PlayMode.LOOP);
        } else {
            entitySprite = new Sprite(textures.findRegion(texture));
        }
        
        width = config.getFloat(100, "width");
        height = config.getFloat(100, "height");
        setSize(width, height);
        init();
        
        float[] offset = config.getFloatArray(false, new float[]{0, 0}, "offset");
        offsetAngle = MathUtils.atan2(offset[1], offset[0]) * MathUtils.radiansToDegrees;
        offsetDistance = getDistanceBetweenTwoPoints(0, 0, offset[0], offset[1]);
        
        float fireRateRandomness = config.getFloatWithFallback(baseConfig, false, 0, "fireRate", "randomness");
        fireRate = config.getFloatWithFallback(baseConfig, false, 1, "fireRate", "baseRate") + getRandomInRange((int) (-fireRateRandomness * 10), (int) (fireRateRandomness * 10)) / 10f;
        fireTimer = -config.getFloatWithFallback(baseConfig, false, 0, "fireRate", "initialDelay");
        
        bulletData = new BulletData(config.getWithFallBack(baseConfig.get(true, "bullet"), false, "bullet"));
        
        shootingSound = base.assetManager.get(config.getStringWithFallback(baseConfig, true, "sfx/gun1.ogg", "shootSound"));
        
        bulletOffset = config.getFloatArrayWithFallback(baseConfig, true, new float[]{0, 0}, "bulletOffset");
        
        bulletOffsetAngle = MathUtils.atan2(bulletOffset[1], bulletOffset[0]) * MathUtils.radiansToDegrees;
        bulletOffsetDistance = getDistanceBetweenTwoPoints(0, 0, bulletOffset[0], bulletOffset[1]);
        
        bulletsPerShot = config.getIntWithFallback(baseConfig, false, 1, "bulletsPerShot");
        if (bulletsPerShot > 0) {
            burstSpacing = config.getIntWithFallback(baseConfig, false, 0, "burstSpacing");
        }
        bulletSpread = config.getFloatWithFallback(baseConfig, true, 0, "bulletSpread");
        
        hasPowerUpEffect = config.getWithFallBack(baseConfig.get(false, "powerUpEffect"), false, "powerUpEffect").isString();
        if (hasPowerUpEffect) {
            powerUpEffectPath = config.getStringWithFallback(baseConfig, true, "particles/laser_powerup_red.p", "powerUpEffect");
            powerUpScale = config.getFloatWithFallback(baseConfig, true, 1, "powerUpEffectScale");
            powerUpEffectShootDelay = config.getFloatWithFallback(baseConfig, true, 1, "powerUpShootDelay");
            float[] powerUpOffset = config.getFloatArrayWithFallback(baseConfig, true, new float[]{0, 0}, "powerUpEffectOffset");
            powerUpOffset[0] += bulletOffset[0];
            powerUpOffset[1] += bulletOffset[1];
            powerUpOffsetAngle = MathUtils.atan2(powerUpOffset[1], powerUpOffset[0]) * MathUtils.radiansToDegrees;
            powerUpOffsetDistance = getDistanceBetweenTwoPoints(0, 0, powerUpOffset[0], powerUpOffset[1]);
        }
        
        hasPowerDownEffect = config.getWithFallBack(baseConfig.get(false, "powerDownEffect"), false, "powerDownEffect").isString();
        if (hasPowerDownEffect) {
            powerDownEffectPath = config.getStringWithFallback(baseConfig, true, "particles/smoke.p", "powerDownEffect");
            powerDownScale = config.getFloatWithFallback(baseConfig, true, 1, "powerDownEffectScale");
            float[] powerDownOffset = config.getFloatArrayWithFallback(baseConfig, true, new float[]{0, 0}, "powerDownEffectOffset");
            powerDownOffset[0] += bulletOffset[0];
            powerDownOffset[1] += bulletOffset[1];
            powerDownOffsetAngle = MathUtils.atan2(powerDownOffset[1], powerDownOffset[0]) * MathUtils.radiansToDegrees;
            powerDownOffsetDistance = getDistanceBetweenTwoPoints(0, 0, powerDownOffset[0], powerDownOffset[1]);
        }
        
        if (config.getWithFallBack(baseConfig.get(false, "recoil"), false, "recoil").isNumber()) {
            recoil = config.getFloatWithFallback(baseConfig, true, 5, "recoil");
            recoilReturnSpeed = config.getFloatWithFallback(baseConfig, true, 10, "recoilReturnSpeed");
        }
        
        
        if (hasPowerUpEffect) {
            powerUpEffect = particleEffectPoolLoader.getParticleEffectByPath(powerUpEffectPath);
            powerUpEffect.scaleEffect(powerUpScale);
        }
        if (hasPowerDownEffect) {
            powerDownEffect = particleEffectPoolLoader.getParticleEffectByPath(powerDownEffectPath);
            powerDownEffect.scaleEffect(powerDownScale);
        }
        
    }
    
    void drawSprite(SpriteBatch batch) {
        if (hasAnimation) {
            entitySprite.setRegion(barrelAnimation.getKeyFrame(animationPosition));
        }
        entitySprite.draw(batch);
    }
    
    void draw(SpriteBatch batch, float delta) {
        if (base.visible && base.health > 0 && base.link.health > 0 && base.body.health > 0) {
            if (powerDownActive) {
                powerDownEffect.draw(batch);
            }
            if (drawBulletsOnTop) {
                drawSprite(batch);
            }
        }
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).update(delta);
            bullets.get(i).draw(batch, delta);
            if (bullets.get(i).queuedForDeletion) {
                bullets.get(i).dispose();
                bullets.removeIndex(i);
            }
        }
        if (base.visible && base.health > 0 && base.link.health > 0 && base.body.health > 0) {
            if (!drawBulletsOnTop) {
                drawSprite(batch);
            }
        }
    }
    
    void drawPowerUpEffect(SpriteBatch batch) {
        if (base.visible && base.health > 0 && base.link.health > 0 && base.body.health > 0) {
            if (powerUpActive && base.active) {
                powerUpEffect.draw(batch);
            }
        }
    }
    
    @Override
    public void drawDebug(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(x + originX, y + originY, 5);
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).drawDebug(shapeRenderer);
        }
        if (powerUpActive) {
            shapeRenderer.setColor(Color.YELLOW);
            drawParticleEffectBounds(shapeRenderer, powerUpEffect);
        }
        if (powerDownActive) {
            shapeRenderer.setColor(Color.RED);
            drawParticleEffectBounds(shapeRenderer, powerDownEffect);
        }
    }
    
    void update(float delta) {
        
        animationPosition += delta;
        
        rotation = base.rotation + base.movementRotation;
        x = base.x + base.movementOffsetX + base.originX - width / 2f + MathUtils.cosDeg(rotation + offsetAngle) * offsetDistance + MathUtils.cosDeg(rotation) * currentRecoilOffset;
        y = base.y + base.movementOffsetY + base.originY - height / 2f + MathUtils.sinDeg(rotation + offsetAngle) * offsetDistance + MathUtils.sinDeg(rotation) * currentRecoilOffset;
        entitySprite.setPosition(x, y);
        entitySprite.setRotation(rotation);
        
        if (base.active) {
            if (powerUpActive) {
                float newX = x + width / 2f + MathUtils.cosDeg(rotation + powerUpOffsetAngle) * powerUpOffsetDistance;
                float newY = y + height / 2f + MathUtils.sinDeg(rotation + powerUpOffsetAngle) * powerUpOffsetDistance;
                powerUpEffect.setPosition(newX, newY);
                powerUpEffect.update(delta);
            }
            if (powerDownActive) {
                float newX = x + width / 2f + MathUtils.cosDeg(rotation + powerDownOffsetAngle) * powerDownOffsetDistance;
                float newY = y + height / 2f + MathUtils.sinDeg(rotation + powerDownOffsetAngle) * powerDownOffsetDistance;
                powerDownEffect.setPosition(newX, newY);
                powerDownEffect.update(delta);
            }
            if (fireTimer >= 1 && base.health > 0) {
                shoot();
                fireTimer = 0;
            } else {
                fireTimer += delta * fireRate;
            }
            if (currentRecoilOffset > 0) {
                currentRecoilOffset = clamp(currentRecoilOffset - delta * recoilReturnSpeed, 0, recoil);
            }
        }
    }
    
    void shoot() {
        
        if(base.x <= -base.width - 20){
            base.active = false;
            base.rotation = 0;
        }else {
            secondThread.execute(() -> {
                try {
                    int bulletsShot = 0;
                    if (powerUpEffect != null) {
                        powerUpActive = true;
                        powerUpEffect.reset(false);
                    }
                    Thread.sleep((int) (powerUpEffectShootDelay * 1000));
                    powerUpActive = false;
                    if (burstSpacing < 100 && base.soundVolume > 0) {
                        shootingSound.play();
                    }
                    while (true) {
                        if (!is_paused) {
                            Thread.sleep(burstSpacing);
                            Gdx.app.postRunnable(Barrel.this::spawnBullet);
                            bulletsShot++;
                            if (bulletsShot >= bulletsPerShot) {
                                break;
                            }
                        } else if (stopThread) {
                            break;
                        }
                    }
                    if (powerDownEffect != null) {
                        powerDownActive = true;
                        powerDownEffect.reset(false);
                    }
                } catch (InterruptedException e) {
                    log("Burst thread interrupted", DEBUG);
                }
            });
        }
    }
    
    void spawnBullet() {
        BulletData newBulletData = new BulletData(base.currentConfig.get("bullet"));
        
        float newX = x + width / 2f - bulletData.width / 2f + MathUtils.cosDeg(rotation + bulletOffsetAngle) * bulletOffsetDistance;
        float newY = y + height / 2f - bulletData.height / 2f + MathUtils.sinDeg(rotation + bulletOffsetAngle) * bulletOffsetDistance;
        float newRot = rotation;
        
        newRot += getRandomInRange(-10, 10) * bulletSpread;
        
        bullets.add(new EnemyBullet(base.assetManager, newBulletData, base.player, newX, newY, newRot, bulletData.hasCollisionWithPlayerBullets));
        if (burstSpacing >= 100 && base.soundVolume > 0) {
            shootingSound.play();
        }
        currentRecoilOffset = recoil;
    }
    
    void dispose() {
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).dispose();
        }
        if (powerUpEffect != null) {
            powerUpEffect.free();
        }
        if (powerDownEffect != null) {
            powerDownEffect.free();
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
        
        log("loading movement: " + movementConfig.name, DEBUG);
        
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
            String moveByRaw = movementConfig.getString("inf", "moveBy").trim();
            if (moveByRaw.equals("inf")) {
                moveBy = Integer.MAX_VALUE;
            } else if (moveByRaw.endsWith("%")) {
                float maxValue = 0;
                switch (type) {
                    case ("moveLinearX"):
                    case ("moveSinX"):
                        maxValue = 800;
                        break;
                    case ("moveLinearY"):
                    case ("moveSinY"):
                        maxValue = 480;
                        break;
                    case ("rotateSin"):
                    case ("rotateRelativeTo"):
                    case ("rotate"):
                        maxValue = 360;
                        break;
                }
                moveBy = convertPercentsToAbsoluteValue(moveByRaw, maxValue);
            } else {
                moveBy = Float.parseFloat(moveByRaw);
            }
            directionModifier = moveBy >= 0 ? (byte) 1 : (byte) -1;
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
                    target.movementOffsetX = relativeTarget.originX - target.originX + relativeTarget.movementOffsetX + MathUtils.cosDeg(progress + angleOffset) * currentRadius;
                    target.movementOffsetY = relativeTarget.originX - target.originX + relativeTarget.movementOffsetY + MathUtils.sinDeg(progress + angleOffset) * currentRadius;
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
        
        log("loading phase: " + phaseData.name, DEBUG);
        
        this.boss = boss;
        
        actions = new Array<>();
        phaseTriggers = new Array<>();
        activated = false;
        config = phaseData;
        JsonEntry triggers = null;
        for (int i = 0; i < phaseData.size; i++) {
            if (!phaseData.get(i).name.equals("triggers")) {
                actions.add(new Action(partGroups, phaseData.get(i), parts, animations, boss, "", actions));
            } else {
                if (triggers == null) {
                    triggers = phaseData.get(i);
                } else {
                    log("multiple trigger entries detected for " + phaseData.name + ", ignoring", WARNING);
                }
            }
        }
        if (triggers == null) {
            log("no triggers for " + phaseData.name, WARNING);
        } else {
            log(triggers.size + " trigger(s) for " + phaseData.name, DEBUG);
            for (int i = 0; i < triggers.size; i++) {
                PhaseTrigger trigger = new PhaseTrigger(triggers.get(i), parts, "", partGroups, phaseTriggers);
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
    
    Action(JsonEntry partGroups, JsonEntry actionValue, Array<BasePart> parts, Array<Movement> animations, Boss boss, String predeterminedTarget, Array<Action> actions) {
        
        this.boss = boss;
        config = actionValue;
        movements = new Array<>();
        String target;
        if (predeterminedTarget.equals("")) {
            
            Array<String> targets = getTargetsFromGroup(actionValue.getString(parts.get(0).name, "target"), partGroups);
            
            if (targets.isEmpty()) {
                log("Invalid action target, empty list", ERROR);
            }
            target = targets.get(0);
            if (targets.size > 1) {
                for (int i = 1; i < targets.size; i++) {
                    actions.add(new Action(partGroups, actionValue, parts, animations, boss, targets.get(i), actions));
                }
            }
        } else {
            target = predeterminedTarget;
        }
        log("loading action: " + actionValue.name + ", target: " + target, DEBUG);
        for (int i = 0; i < parts.size; i++) {
            if (parts.get(i).name.equals(target)) {
                this.target = parts.get(i);
                break;
            }
        }
        if (this.target == null) {
            log("Invalid action target, " + target + " not found", ERROR);
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
                log("no ai change for " + actionValue.name, DEBUG);
            }
        }
        if (actionValue.get(false, "move").isObject()) {
            movementCount = actionValue.get("move").size;
            hasMovement = true;
            for (int i = 0; i < movementCount; i++) {
                Movement movement = new Movement(actionValue.get("move", i), target, parts, animations, boss.body);
                animations.add(movement);
                movements.add(movement);
            }
        } else {
            log("no movement for " + actionValue.name + ", target: " + target, DEBUG);
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
    
    PhaseTrigger(JsonEntry triggerData, Array<BasePart> parts, String predeterminedTarget, JsonEntry partGroups, Array<PhaseTrigger> triggers) {
        
        isResetPhase = triggerData.parent().parent().name.equals("RESET");
        
        conditionsMet = false;
        triggerType = triggerData.getString("health", "triggerType");
        String targetPart;
        if (predeterminedTarget.equals("")) {
            
            Array<String> targets = getTargetsFromGroup(triggerData.getString(parts.get(0).name, "target"), partGroups);
            
            if (targets.isEmpty()) {
                log("Error setting up trigger, empty list", ERROR);
            }
            targetPart = targets.get(0);
            if (targets.size > 1) {
                for (int i = 1; i < targets.size; i++) {
                    triggers.add(new PhaseTrigger(triggerData, parts, targets.get(i), partGroups, triggers));
                }
            }
        } else {
            targetPart = predeterminedTarget;
        }
        log("loading phase trigger: " + triggerData.name + ", phase: " + triggerData.parent().parent().name + ", target: " + targetPart, DEBUG);
        for (int i2 = 0; i2 < parts.size; i2++) {
            if (parts.get(i2).name.equals(targetPart)) {
                triggerTarget = parts.get(i2);
                break;
            }
        }
        if (triggerTarget == null) {
            log("Error setting up trigger, " + targetPart + " not found", ERROR);
        }
        String valueRaw = triggerData.getString("1", "value").trim();
        if (valueRaw.endsWith("%")) {
            float maxValue = 0;
            switch (triggerType) {
                case ("positionX"):
                    maxValue = 800;
                    break;
                case ("positionY"):
                    maxValue = 480;
                    break;
                case ("rotation"):
                    maxValue = 360;
                    break;
                case ("health"):
                    maxValue = triggerTarget.health;
                    break;
            }
            value = convertPercentsToAbsoluteValue(valueRaw, maxValue);
        } else {
            value = Float.parseFloat(valueRaw);
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
