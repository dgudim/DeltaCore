package com.deo.flapd.model.enemies;

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
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.getTargetsFromGroup;
import static com.deo.flapd.utils.DUtils.lerpAngleWithConstantSpeed;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.view.screens.GameScreen.is_paused;
import static java.lang.StrictMath.abs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
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
import com.deo.flapd.control.GameVariables;
import com.deo.flapd.model.Entity;
import com.deo.flapd.model.Player;
import com.deo.flapd.model.bullets.EnemyBullet;
import com.deo.flapd.model.loot.Drops;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.MusicManager;
import com.deo.flapd.utils.SoundManager;
import com.deo.flapd.utils.particles.ParticleEffectPoolLoader;

public class Boss {
    
    public JsonEntry bossConfig;
    private final Array<BasePart> parts;
    private final Array<Phase> phases;
    private final int[] spawnAt;
    boolean visible;
    Array<Movement> animations;
    public boolean hasAlreadySpawned;
    private final int spawnScore;
    public BasePart body;
    
    boolean hasAi;
    EnemyAi bossAi;
    
    public String bossName;
    
    String bossMusic;
    MusicManager musicManager;
    
    Boss(String bossName, CompositeManager compositeManager, Player player) {
        log("------------------------------------------------------\n", DEBUG);
        log("---------loading " + bossName, DEBUG);
        long genTime = TimeUtils.millis();
        
        AssetManager assetManager = compositeManager.getAssetManager();
        musicManager = compositeManager.getMusicManager();
        
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
        bossMusic = bossConfig.getString(false, "", "music");
        
        for (int i = 0; i < bossConfig.get("parts").size; i++) {
            String type = bossConfig.get("parts", i).getString("part", "type");
            switch (type) {
                case ("basePart"):
                    if (body == null) {
                        body = new BasePart(bossConfig.get("parts", i), bossAtlas, compositeManager);
                        parts.add(body);
                    } else {
                        log("can't have two or more base parts per boss, ignoring " + bossConfig.get("parts", i).name, ERROR);
                    }
                    break;
                case ("part"):
                    parts.add(new Part(bossConfig.get("parts", i), bossAtlas, parts, this, compositeManager));
                    break;
                case ("cannon"):
                    
                    JsonEntry config = bossConfig.get("parts", i);
                    
                    JsonEntry barrelsJsonEntry = config.get(false, "barrels");
                    if (barrelsJsonEntry.isNull()) {
                        JsonValue toAdd = new JsonValue(JsonValue.ValueType.object);
                        toAdd.setName("barrels");
                        JsonValue toAdd_mainBarrel = new JsonValue(JsonValue.ValueType.object);
                        toAdd_mainBarrel.setName("barrel_" + config.name);
                        toAdd_mainBarrel.addChild("texture", new JsonValue(config.getString("noTexture", "texture")));
                        toAdd_mainBarrel.addChild("hasAnimation", new JsonValue(config.getBoolean(false, false, "hasAnimation")));
                        toAdd_mainBarrel.addChild("frameDuration", new JsonValue(config.getFloat(false, 1, "frameDuration")));
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
                        if (config.get(false, "hasAnimation").isBoolean()) {
                            config.jsonValue.get("hasAnimation").set(false);
                        }
                        config.addValue(new JsonEntry(toAdd));
                    }
                    
                    parts.add(new Cannon(config, bossAtlas, parts, this, compositeManager));
                    break;
                case ("shield"):
                    parts.add(new Shield(bossConfig.get("parts", i), bossAtlas, parts, this, compositeManager));
                    break;
                case ("clone"):
                    String copyFrom = bossConfig.get("parts", i).getString(parts.get(0).name, "copyFrom");
                    String cloneType = bossConfig.getString("part", "parts", copyFrom, "type");
                    switch (cloneType) {
                        case ("part"):
                            parts.add(new Part(bossConfig.get("parts", i), bossAtlas, parts, this, compositeManager));
                            break;
                        case ("cannon"):
                            parts.add(new Cannon(bossConfig.get("parts", i), bossAtlas, parts, this, compositeManager));
                            break;
                        case ("shield"):
                            parts.add(new Shield(bossConfig.get("parts", i), bossAtlas, parts, this, compositeManager));
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
        
        setTargetPlayer(player);
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
        if (GameVariables.score >= spawnScore && !hasAlreadySpawned) {
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
        GameVariables.bossWave = true;
        if (!bossMusic.equals("")) {
            musicManager.setNewMusicSource(bossMusic, 1);
        }
        phases.get(0).activate();
    }
    
    private void setTargetPlayer(Player player) {
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
        GameVariables.bossWave = false;
        
        if (!bossMusic.equals("")) {
            this.musicManager.setNewMusicSource("music/main", 1, 5, 5);
        }
        
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
    
    AssetManager assetManager;
    private final Drops drops;
    
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
    
    boolean hasEffects;
    int effectCount;
    Array<ParticleEffectPool.PooledEffect> particleEffects;
    Array<Float> particleEffectAngles;
    Array<Float> particleEffectDistances;
    boolean[] effectLayerFlags;
    
    TextureAtlas textures;
    
    Array<BasePart> links;
    
    int[] bonusType, itemRarity, itemCount, moneyCount;
    float itemTimer, moneyTimer;
    int bonusChance;
    
    String explosionSound;
    SoundManager soundManager;
    CompositeManager compositeManager;
    ParticleEffectPoolLoader particleEffectPool;
    
    BasePart(JsonEntry newConfig, TextureAtlas textures, CompositeManager compositeManager) {
        
        log("loading " + newConfig.name, DEBUG);
        
        this.compositeManager = compositeManager;
        particleEffectPool = compositeManager.getParticleEffectPool();
        
        active = false;
        currentConfig = newConfig;
        assetManager = compositeManager.getAssetManager();
        soundManager = compositeManager.getSoundManager();
        explosionSound = currentConfig.getString(false, "explosion", "explosionSound");
        drops = compositeManager.getDrops();
        
        exploded = false;
        name = newConfig.name;
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
            
            explosionEffect = particleEffectPool.getParticleEffectByPath(currentConfig.getString("particles/explosion.p", "explosionEffect"));
            explosionEffect.scaleEffect(currentConfig.getFloat(1, "explosionScale"));
            log("creating explosion effect for " + newConfig.name, DEBUG);
        }
        
        hasEffects = currentConfig.get(false, "effects").isObject();
        
        if (hasEffects) {
            effectCount = currentConfig.getInt(0, "effects", "count");
            
            particleEffects = new Array<>();
            particleEffectAngles = new Array<>();
            particleEffectDistances = new Array<>();
            
            effectLayerFlags = new boolean[effectCount];
            
            for (int i = 0; i < effectCount; i++) {
                float[] effectOffset = currentConfig.getFloatArray(new float[]{0, 0}, "effects", "offset" + i);
                particleEffectAngles.add(MathUtils.atan2(effectOffset[1], effectOffset[0]) * MathUtils.radiansToDegrees);
                particleEffectDistances.add(getDistanceBetweenTwoPoints(0, 0, effectOffset[0], effectOffset[1]));
                effectLayerFlags[i] = currentConfig.getBoolean(false, "effects", "drawOnTop" + i);
                
                ParticleEffectPool.PooledEffect effect = particleEffectPool.getParticleEffectByPath(currentConfig.getString("particles/fire2.p", "effects", "effect" + i));
                effect.scaleEffect(currentConfig.getFloat(1, "effects", "scale" + i));
                effect.setPosition(
                        x + width / 2f + MathUtils.cosDeg(
                                rotation + particleEffectAngles.get(i)) * particleEffectDistances.get(i),
                        y + height / 2f + MathUtils.sinDeg(
                                rotation + particleEffectAngles.get(i)) * particleEffectDistances.get(i));
                particleEffects.add(effect);
            }
        }
        
        String texture = currentConfig.getString("noTexture", "texture");
        hasAnimation = currentConfig.getBoolean(false, false, "hasAnimation");
        
        if (hasAnimation) {
            entitySprite = new Sprite();
            entityAnimation = new Animation<>(
                    currentConfig.getFloat(1, "frameDuration"),
                    textures.findRegions(texture),
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
    
    void drawEffects(SpriteBatch batch, float delta, boolean top) {
        if (hasEffects) {
            for (int i = 0; i < particleEffects.size; i++) {
                if (effectLayerFlags[i] == top) {
                    particleEffects.get(i).draw(batch, delta);
                }
            }
        }
    }
    
    void drawSpriteWithEffects(SpriteBatch batch, float delta) {
        if (hasAnimation) {
            entitySprite.setRegion(entityAnimation.getKeyFrame(animationPosition));
        }
        drawEffects(batch, delta, false);
        entitySprite.draw(batch);
        drawEffects(batch, delta, true);
    }
    
    void drawHealthBar(SpriteBatch batch) {
        if (showHealthBar) {
            healthBar.draw(batch, 1);
        }
    }
    
    void draw(SpriteBatch batch, float delta) {
        if (visible) {
            drawSpriteWithEffects(batch, delta);
            drawHealthBar(batch);
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
        shapeRenderer.setColor(Color.YELLOW);
        if (hasEffects) {
            for (int i = 0; i < particleEffects.size; i++) {
                drawParticleEffectBounds(shapeRenderer, particleEffects.get(i));
            }
        }
    }
    
    @Override
    protected void updateEntity(float delta) {
        entitySprite.setPosition(x + movementOffsetX, y + movementOffsetY);
        entitySprite.setRotation(rotation + movementRotation);
        entitySprite.setColor(color);
    }
    
    @Override
    protected void updateHealth(float delta) {
        if (health > 0) {
            entityHitBox.setPosition(x + movementOffsetX, y + movementOffsetY);
            if (regeneration > 0 && maxHealth > 0) {
                health = clamp(health + regeneration * delta, 0, maxHealth);
            }
        } else {
            entityHitBox.setPosition(-1000, -1000).setSize(0, 0);
        }
    }
    
    void updateEntityAndHealthBar(float delta) {
        updateEntity(delta);
        updateHealth(delta);
        
        animationPosition += delta;
        
        if (showHealthBar) {
            healthBar.setValue(health);
            healthBar.setPosition(width / 2 + x + movementOffsetX - 12.5f, y + movementOffsetY - 7);
            healthBar.act(delta);
        }
    }
    
    void updateEffects(float delta) {
        if (hasEffects) {
            for (int i = 0; i < particleEffects.size; i++) {
                particleEffects.get(i).setPosition(
                        x + movementOffsetX + originX + MathUtils.cosDeg(
                                rotation + movementRotation + particleEffectAngles.get(i)) * particleEffectDistances.get(i),
                        y + movementOffsetY + originY + MathUtils.sinDeg(
                                rotation + movementRotation + particleEffectAngles.get(i)) * particleEffectDistances.get(i));
            }
        }
        if (exploded) {
            explosionEffect.update(delta);
        }
    }
    
    void updateCollisions() {
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
    }
    
    void update(float delta) {
        updateEntityAndHealthBar(delta);
        updateCollisions();
        updateEffects(delta);
    }
    
    void setTargetPlayer(Player player) {
        this.player = player;
    }
    
    void dispose() {
        if (hasCollision && !type.equals("shield")) {
            explosionEffect.free();
        }
        if (hasEffects) {
            for (int i = 0; i < particleEffects.size; i++) {
                particleEffects.get(i).free();
            }
            particleEffects.clear();
        }
    }
    
    void addLinkedPart(BasePart part) {
        links.add(part);
    }
    
    void explode() {
        if (hasCollision && !type.equals("shield")) {
            drops.dropMoney(entityHitBox, getRandomInRange(moneyCount[0], moneyCount[1]), moneyTimer);
            
            if (getRandomInRange(0, 100) <= bonusChance) {
                drops.dropBonus(getRandomInRange(bonusType[0], bonusType[1]), entityHitBox);
            }
            
            drops.drop(entityHitBox, getRandomInRange(itemCount[0], itemCount[1]), itemTimer, getRandomInRange(itemRarity[0], itemRarity[1]));
            
            explosionEffect.setPosition(x + movementOffsetX + width / 2, y + movementOffsetY + height / 2);
            
            soundManager.playSound_noLink(explosionSound);
            
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
    final Boss boss;
    
    Part(JsonEntry newConfig, TextureAtlas textures, Array<BasePart> parts, Boss boss, CompositeManager compositeManager) {
        super(newConfig, textures, compositeManager);
        this.parts = parts;
        link = boss.body;
        this.boss = boss;
        String relativeTo = currentConfig.getString(false, boss.body.name, "offset", "relativeTo");
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
        if (visible && health > 0 && link.health > 0 && boss.body.health > 0) {
            drawSpriteWithEffects(batch, delta);
            drawHealthBar(batch);
        }
        if (exploded) {
            explosionEffect.draw(batch);
        }
    }
}

class Shield extends Part {
    
    Shield(JsonEntry newConfig, TextureAtlas textures, Array<BasePart> parts, Boss boss, CompositeManager compositeManager) {
        super(newConfig, textures, parts, boss, compositeManager);
    }
    
    @Override
    void updateCollisions() {
        entitySprite.setAlpha(health / maxHealth);
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
    
    Cannon(JsonEntry partConfig, TextureAtlas textures, Array<BasePart> parts, Boss boss, CompositeManager compositeManager) {
        super(partConfig, textures, parts, boss, compositeManager);
        
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
    protected void update(float delta) {
        super.update(delta);
        if (active) {
            if (canAim) {
                currentAimAngle = lerpAngleWithConstantSpeed(currentAimAngle, clamp(MathUtils.radiansToDegrees * MathUtils.atan2(
                        y + movementOffsetY + originY - (player.y + player.height / 2),
                        x + movementOffsetX + originX - (player.x + player.width / 2)),
                        aimAngleLimit[0], aimAngleLimit[1]), aimingSpeed, delta);
                
                switch (aimAnimationType) {
                    case ("textureChange"):
                        
                        String texture;
                        if ((currentAimAngle >= 0 && currentAimAngle <= 22.5) || (currentAimAngle >= 337.5 && currentAimAngle <= 360)) {
                            texture = aimTextures[1];
                        } else if (currentAimAngle >= 22.5 && currentAimAngle <= 67.5) {
                            texture = aimTextures[7];
                        } else if (currentAimAngle >= 67.5 && currentAimAngle <= 112.5) {
                            texture = aimTextures[3];
                        } else if (currentAimAngle >= 112.5 && currentAimAngle <= 157.5) {
                            texture = aimTextures[6];
                        } else if ((currentAimAngle >= 157.5 && currentAimAngle <= 202.5)) {
                            texture = aimTextures[0];
                        } else if (currentAimAngle >= 202.5 && currentAimAngle <= 247.5) {
                            texture = aimTextures[4];
                        } else if (currentAimAngle >= 247.5 && currentAimAngle <= 292.5) {
                            texture = aimTextures[2];
                        } else {
                            texture = aimTextures[5];
                        }
                        if (hasAnimation) {
                            String[] atlasAndFrameDuration = texture.replace(" ", "").split(",");
                            float frameDuration = Float.parseFloat(atlasAndFrameDuration[1]);
                            entityAnimation = new Animation<>(
                                    frameDuration,
                                    textures.findRegions(atlasAndFrameDuration[0]),
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
    void drawSpriteWithEffects(SpriteBatch batch, float delta) {
        boolean draw = visible && health > 0 && link.health > 0 && boss.body.health > 0;
        if (draw) {
            if (hasAnimation) {
                entitySprite.setRegion(entityAnimation.getKeyFrame(animationPosition));
            }
            drawEffects(batch, delta, false);
        }
        drawBarrels(batch, delta, draw, false);
        if (draw) {
            entitySprite.draw(batch);
        }
        drawBarrels(batch, delta, draw, true);
        if (draw) {
            drawEffects(batch, delta, true);
            drawHealthBar(batch);
        }
        
    }
    
    void drawBarrels(SpriteBatch batch, float delta, boolean draw, boolean onTop) {
        for (int i = 0; i < barrels.size; i++) {
            if (barrels.get(i).drawBarrelOnTop == onTop) {
                barrels.get(i).draw(batch, delta, draw);
            }
        }
    }
    
    @Override
    void draw(SpriteBatch batch, float delta) {
        
        drawSpriteWithEffects(batch, delta);
        
        if (exploded) {
            explosionEffect.draw(batch);
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
    
    String name;
    
    int shootingKeyFrame;
    
    float[] bulletOffset;
    float bulletOffsetAngle;
    float bulletOffsetDistance;
    
    int bulletsPerShot;
    int burstSpacing;
    float bulletSpread;
    
    float fireRate;
    float triggerVolume;
    float fireTimer;
    
    boolean drawBulletsOnTop;
    boolean drawBarrelOnTop;
    
    JsonEntry currentConfig;
    Array<EnemyBullet> bullets;
    
    String shootingSound;
    
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
        
        ParticleEffectPoolLoader particleEffectPool = base.particleEffectPool;
        
        bullets = new Array<>();
        currentConfig = config;
        name = config.name;
        
        active = true;
        
        JsonEntry baseConfig = base.currentConfig;
        drawBulletsOnTop = config.getBooleanWithFallback(baseConfig, false, false, "drawBulletsOnTop");
        
        drawBarrelOnTop = config.getBoolean(false, false, "drawOnTop");
        
        this.base = base;
        
        hasAnimation = config.getBoolean(false, false, "hasAnimation");
        shootingKeyFrame = config.getIntWithFallback(baseConfig, false, -1, "shootingKeyFrame");
        String texture = config.getString(false, "noTexture", "texture");
        if (hasAnimation) {
            entitySprite = new Sprite();
            entityAnimation = new Animation<>(
                    config.getFloatWithFallback(baseConfig, false, 1, "frameDuration"),
                    textures.findRegions(texture),
                    Animation.PlayMode.LOOP);
        } else {
            entitySprite = new Sprite(textures.findRegion(texture));
        }
        
        width = config.getFloat(false, 0, "width");
        height = config.getFloat(false, 0, "height");
        setSize(width, height);
        init();
        
        float[] offset = config.getFloatArray(false, new float[]{0, 0}, "offset");
        offsetAngle = MathUtils.atan2(offset[1], offset[0]) * MathUtils.radiansToDegrees;
        offsetDistance = getDistanceBetweenTwoPoints(0, 0, offset[0], offset[1]);
        
        float fireRateRandomness = config.getFloatWithFallback(baseConfig, false, 0, "fireRate", "randomness");
        fireRate = config.getFloatWithFallback(baseConfig, false, 1, "fireRate", "baseRate") + getRandomInRange((int) (-fireRateRandomness * 10), (int) (fireRateRandomness * 10)) / 10f;
        fireTimer = -config.getFloatWithFallback(baseConfig, false, 0, "fireRate", "initialDelay");
        
        triggerVolume = config.getFloatWithFallback(baseConfig, false, 2, "fireRate", "triggerOnVolume");
        
        shootingSound = config.getStringWithFallback(baseConfig, true, "gun1", "shootSound");
        
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
            powerUpEffect = particleEffectPool.getParticleEffectByPath(powerUpEffectPath);
            powerUpEffect.scaleEffect(powerUpScale);
        }
        if (hasPowerDownEffect) {
            powerDownEffect = particleEffectPool.getParticleEffectByPath(powerDownEffectPath);
            powerDownEffect.scaleEffect(powerDownScale);
        }
        
    }
    
    void drawSpriteSpriteWithEffects(SpriteBatch batch) {
        if (hasAnimation) {
            entitySprite.setRegion(entityAnimation.getKeyFrame(animationPosition));
        }
        entitySprite.draw(batch);
        if (powerUpActive) {
            powerUpEffect.draw(batch);
        }
    }
    
    void draw(SpriteBatch batch, float delta, boolean draw) {
        if (draw) {
            if (powerDownActive) {
                powerDownEffect.draw(batch);
            }
            if (drawBulletsOnTop) {
                drawSpriteSpriteWithEffects(batch);
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
        if (draw) {
            if (!drawBulletsOnTop) {
                drawSpriteSpriteWithEffects(batch);
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
        
        if (base.active && active) {
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
                boolean shoot = false;
                if ((hasAnimation || base.hasAnimation) && shootingKeyFrame != -1) {
                    if (entityAnimation != null) {
                        shoot = entityAnimation.getKeyFrameIndex(animationPosition) == shootingKeyFrame;
                    } else {
                        shoot = base.entityAnimation.getKeyFrameIndex(animationPosition) == shootingKeyFrame;
                    }
                } else if (triggerVolume > 1 || base.boss.musicManager.getAmplitude() >= triggerVolume) {
                    shoot = true;
                }
                if (shoot) {
                    shoot();
                    fireTimer = 0;
                }
            } else {
                fireTimer += delta * fireRate;
            }
            if (currentRecoilOffset > 0) {
                currentRecoilOffset = clamp(currentRecoilOffset - delta * recoilReturnSpeed, 0, recoil);
            }
        }
    }
    
    void shoot() {
        
        if (base.x <= -base.width - 20) {
            base.active = false;
            active = false;
            base.rotation = 0;
        } else {
            secondThread.execute(() -> {
                try {
                    int bulletsShot = 0;
                    if (powerUpEffect != null) {
                        powerUpActive = true;
                        powerUpEffect.reset(false);
                    }
                    Thread.sleep((int) (powerUpEffectShootDelay * 1000));
                    powerUpActive = false;
                    if (burstSpacing < 100) {
                        base.soundManager.playSound_noLink(shootingSound);
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
        
        bullets.add(new EnemyBullet(base.compositeManager,
                currentConfig.getWithFallBack(base.currentConfig.get(true, "bullet"), false, "bullet"), base.player){
            @Override
            public void calculateSpawnPosition() {
                this.newX = Barrel.this.x + Barrel.this.width / 2f - this.width / 2f + MathUtils.cosDeg(base.currentAimAngle + bulletOffsetAngle) * bulletOffsetDistance;
                this.newY = Barrel.this.y + Barrel.this.height / 2f - this.height / 2f + MathUtils.sinDeg(base.currentAimAngle + bulletOffsetAngle) * bulletOffsetDistance;
                this.newRot = base.currentAimAngle;
    
                this.newRot += getRandomInRange(-10, 10) * bulletSpread;
            }
        });
        if (burstSpacing >= 100) {
            base.soundManager.playSound_noLink(shootingSound);
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
    private final boolean musicSync;
    
    private float lastShakeXPos;
    private float lastShakeYPos;
    private float shakePeriod;
    private float shakeIntensityX;
    private float shakeIntensityY;
    
    private float currentAddPosition;
    private float speed;
    private float progress;
    private boolean active;
    private final BasePart target;
    
    private BasePart relativeTarget;
    private float radiusExpansionRate;
    private float currentRadius;
    private float targetRadius;
    private float angleOffset;
    
    private final String type;
    private byte directionModifier;
    private final boolean stopPreviousAnimations;
    
    private final Array<Movement> animations;
    private final Boss boss;
    
    Movement(JsonEntry movementConfig, BasePart target, Array<BasePart> parts, Array<Movement> animations, Boss boss) {
        
        this.boss = boss;
        
        log("loading movement: " + movementConfig.name, DEBUG);
        
        this.animations = animations;
        this.target = target;
        
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
        
        musicSync = movementConfig.getBoolean(false, false, "musicSync");
        
        if (type.equals("shake")) {
            shakeIntensityX = movementConfig.getFloat(10, "shakeIntensityX");
            shakeIntensityY = movementConfig.getFloat(10, "shakeIntensityY");
            shakePeriod = movementConfig.getFloat(0.3f, "shakePeriod");
        } else {
            speed = movementConfig.getFloat(100, "speed");
            String moveByRaw = movementConfig.getString(false, "inf", "moveBy").trim();
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
        delta *= musicSync ? boss.musicManager.getAmplitude() : 1;
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
    Entity target;
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
        String targetBarrel = "";
        boolean targetIsBarrel = target.startsWith("barrel:");
        if (targetIsBarrel) {
            String[] split = target.trim().replace("barrel:", "").split(":");
            target = split[0];
            targetBarrel = split[1];
        }
        for (int i = 0; i < parts.size; i++) {
            if (parts.get(i).name.equals(target)) {
                this.target = parts.get(i);
                break;
            }
        }
        if (targetIsBarrel) {
            if (this.target instanceof Cannon) {
                Cannon castTarget = (Cannon) this.target;
                for (int i = 0; i < castTarget.barrels.size; i++) {
                    if (castTarget.barrels.get(i).name.equals(targetBarrel)) {
                        this.target = castTarget.barrels.get(i);
                        break;
                    }
                }
                if (!(this.target instanceof Barrel)) {
                    log("didn't find barrel " + targetBarrel + " in " + target, ERROR);
                    this.target = null;
                }
            } else {
                log(this.target + " is not a cannon", ERROR);
                this.target = null;
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
            frameDuration = actionValue.getFloat(false, this.target.entityAnimation.getFrameDuration(), "frameDuration");
        }
        
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
            int movementCount = actionValue.get("move").size;
            hasMovement = true;
            if (this.target instanceof BasePart) {
                for (int i = 0; i < movementCount; i++) {
                    Movement movement = new Movement(actionValue.get("move", i), (BasePart) this.target, parts, animations, boss);
                    animations.add(movement);
                    movements.add(movement);
                }
            } else {
                log("cant load movements for non BasePath entities" + target, ERROR);
                hasMovement = false;
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
        
        TextureAtlas textures = null;
        if (target instanceof BasePart) {
            BasePart castTarget = (BasePart) target;
            if (castTarget.hasCollision) {
                castTarget.collisionEnabled = config.get(false, "enableCollisions").isNull() ? castTarget.collisionEnabled : enableCollisions;
            }
            castTarget.visible = config.get(false, "visible").isNull() ? castTarget.visible : visible;
            castTarget.showHealthBar = config.get(false, "showHealthBar").isNull() ? castTarget.showHealthBar : showHealthBar;
            if (!changeTexture.equals("false")) {
                textures = castTarget.textures;
            }
        } else {
            Barrel castTarget = (Barrel) target;
            if (!changeTexture.equals("false")) {
                textures = castTarget.base.textures;
            }
        }
        
        if (!changeTexture.equals("false")) {
            if (target.hasAnimation) {
                target.entityAnimation = new Animation<>(
                        frameDuration,
                        textures.findRegions(changeTexture),
                        Animation.PlayMode.LOOP);
            } else {
                target.entitySprite.setRegion(textures.findRegion(changeTexture));
            }
        }
        
        target.active = config.get(false, "active").isNull() ? target.active : active;
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
