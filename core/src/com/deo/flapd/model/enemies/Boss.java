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
import com.badlogic.gdx.math.Vector2;
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
    private Array<BasePart> parts;
    private Array<Phase> phases;
    private float x = 1500;
    private float y = 1500;
    private int[] spawnAt;
    boolean visible;
    ShipObject player;
    Array<Movement> animations;
    public boolean hasAlreadySpawned;
    private int spawnScore;
    private BasePart body;

    public String bossName;

    Boss(String bossName, AssetManager assetManager) {

        log("\n\n loading boss config, name: " + bossName);

        this.bossName = bossName;

        bossConfig = (JsonEntry) new JsonReader().parse(Gdx.files.internal("enemies/bosses/" + bossName + "/config.json"));
        TextureAtlas bossAtlas = assetManager.get("enemies/bosses/" + bossName + "/" + bossConfig.getString("textures"));
        parts = new Array<>();
        animations = new Array<>();
        hasAlreadySpawned = getBoolean("boss_spawned_" + bossName);
        spawnScore = bossConfig.getInt("spawnConditions", "score") + getRandomInRange(-bossConfig.getInt("spawnConditions", "randomness"), bossConfig.getInt("spawnConditions", "randomness"));
        spawnAt = bossConfig.getIntArray("spawnAt");
        for (int i = 0; i < bossConfig.get("parts").size; i++) {
            String type = bossConfig.get("parts").get(i).getString("type");
            switch (type) {
                case ("basePart"):
                    body = new BasePart(bossConfig.get("parts").get(i), bossAtlas);
                    parts.add(body);
                    break;
                case ("part"):
                    parts.add(new Part(bossConfig.get("parts").get(i), bossAtlas, parts, body));
                    break;
                case ("cannon"):
                    parts.add(new Cannon(bossConfig.get("parts").get(i), bossAtlas, parts, body, assetManager));
                    break;
                case ("clone"):
                    String cloneType = bossConfig.get("parts").get(i).getString("copyFrom");
                    cloneType = bossConfig.getString("parts", cloneType, "type");
                    switch (cloneType) {
                        case ("part"):
                            parts.add(new Part(bossConfig.get("parts").get(i), bossAtlas, parts, body));
                            break;
                        case ("cannon"):
                            parts.add(new Cannon(bossConfig.get("parts").get(i), bossAtlas, parts, body, assetManager));
                            break;
                    }
                    break;
            }
        }

        Array<Array<BasePart>> sortedParts = new Array<>();

        int maxLayer = 0;

        for (int i = 0; i < parts.size; i++) {
            maxLayer = Math.max(maxLayer, parts.get(i).layer);
        }

        maxLayer = maxLayer + 1;

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
            Phase phase = new Phase(bossConfig.get("phases").get(i), parts, animations, this);
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
            x = body.x;
            y = body.y;
            for (int i = 0; i < parts.size; i++) {
                parts.get(i).x = x + parts.get(i).offsetX;
                parts.get(i).y = y + parts.get(i).offsetY;
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
        if (newConfig.getString("type").equals("clone")) {
            currentConfig = newConfig.parent().get(newConfig.getString("copyFrom"));
        }
        type = currentConfig.getString("type");
        health = currentConfig.getInt("health");
        originalHealth = health;
        width = currentConfig.getFloat("width");
        height = currentConfig.getFloat("height");
        layer = currentConfig.getInt("layer");
        hasCollision = currentConfig.getBoolean("hasCollision");

        if (hasCollision) {
            itemRarity = currentConfig.getIntArray("drops", "items", "rarity");
            itemCount = currentConfig.getIntArray("drops", "items", "count");
            itemTimer = currentConfig.getFloat("drops", "items", "timer");

            bonusChance = currentConfig.getInt("drops", "bonuses", "chance");
            bonusType = currentConfig.getIntArray("drops", "bonuses", "type");

            moneyCount = currentConfig.getIntArray("drops", "money", "count");
            moneyTimer = currentConfig.getFloat("drops", "items", "timer");
        }

        if (hasCollision) {
            explosionSound = Gdx.audio.newSound(Gdx.files.internal(currentConfig.getString("explosionSound")));
        }
        soundVolume = getFloat("soundVolume");

        entitySprite = new Sprite(textures.findRegion(currentConfig.getString("texture")));

        originX = width / 2f;
        originY = height / 2;
        if (!currentConfig.getString("originX").equals("standard")) {
            originX = currentConfig.getFloat("originX");
        }
        if (!currentConfig.getString("originY").equals("standard")) {
            originY = currentConfig.getFloat("originY");
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
            explosionEffect.load(Gdx.files.internal(currentConfig.getString("explosionEffect")), Gdx.files.internal("particles"));
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

    private Array<BasePart> parts;
    private float relativeX, relativeY;
    private BasePart link;
    private BasePart body;

    Part(JsonEntry newConfig, TextureAtlas textures, Array<BasePart> parts, BasePart body) {
        super(newConfig, textures);
        this.parts = parts;
        link = body;
        this.body = body;
        String relativeTo = currentConfig.getString("offset", "relativeTo");
        relativeX = currentConfig.getFloat("offset", "X");
        relativeY = currentConfig.getFloat("offset", "Y");
        if (newConfig.getString("type").equals("clone")) {
            for (int i = 0; i < newConfig.get("override").size; i++) {
                if (newConfig.get("override").get(i).name.equals("offset")) {
                    relativeX = newConfig.get("override").get(i).getFloat("X");
                    relativeY = newConfig.get("override").get(i).getFloat("Y");
                    relativeTo = newConfig.get("override").get(i).getString("relativeTo");
                }
                if (newConfig.get("override").get(i).name.equals("originX")) {
                    originX = newConfig.getFloat(i);
                    entitySprite.setOrigin(originX, originY);
                }
                if (newConfig.get("override").get(i).name.equals("originY")) {
                    originY = newConfig.getFloat(i);
                    entitySprite.setOrigin(originX, originY);
                }
            }

        }

        boolean customLink = false;

        if (!currentConfig.get("linked").isBoolean()) {
            for (int i = 0; i < parts.size; i++) {
                if (currentConfig.getString("linked").equals(parts.get(i).name)) {
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
                    String relativeToP = parts.get(i).currentConfig.getString("offset", "relativeTo");
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
    int[] aimAngleLimit;
    float fireRate;
    float timer;
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

        isLaser = currentConfig.getBoolean("isLaser");

        canAim = currentConfig.getBoolean("canAim");
        aimAngleLimit = currentConfig.getIntArray("aimAngleLimit");

        float fireRateRandomness = currentConfig.getFloat("fireRate", "randomness");
        fireRate = currentConfig.getFloat("fireRate", "baseRate") + getRandomInRange((int) (-fireRateRandomness * 10), (int) (fireRateRandomness * 10)) / 10f;

        bulletData = new BulletData(currentConfig.get("bullet"));

        shootingSound = Gdx.audio.newSound(Gdx.files.internal(bulletData.shootSound));

    }

    @Override
    void update(float delta) {
        super.update(delta);
        if (active) {
            if (canAim) {
                rotation = MathUtils.clamp(MathUtils.radiansToDegrees * MathUtils.atan2(y - (player.bounds.getY() + player.bounds.getBoundingRectangle().getHeight() / 2), x - (player.bounds.getX() + player.bounds.getBoundingRectangle().getWidth() / 2)), aimAngleLimit[0], aimAngleLimit[1]);
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
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).draw(batch, delta);
            bullets.get(i).update(delta);
            if (bullets.get(i).queuedForDeletion) {
                bullets.get(i).dispose();
                bullets.removeIndex(i);
            }
        }
        super.draw(batch, delta);
    }

    void shoot() {
        for (int i = 0; i < bulletData.bulletsPerShot; i++) {
            BulletData newBulletData = new BulletData(currentConfig.get("bullet"));

            newBulletData.x = x + width / 2f - bulletData.width / 2f + MathUtils.cosDeg(rotation + bulletData.bulletAngle) * newBulletData.bulletDistance;
            newBulletData.y = y + height / 2f - bulletData.height / 2f + MathUtils.sinDeg(rotation + bulletData.bulletAngle) * newBulletData.bulletDistance;

            if (canAim) {
                newBulletData.angle = MathUtils.clamp(MathUtils.radiansToDegrees * MathUtils.atan2(y - (player.bounds.getY() + player.bounds.getBoundingRectangle().getHeight() / 2), x - (player.bounds.getX() + player.bounds.getBoundingRectangle().getWidth() / 2)), aimAngleLimit[0], aimAngleLimit[1]);
            }

            newBulletData.angle += getRandomInRange(-10, 10) * bulletData.spread;

            bullets.add(new EnemyBullet(textures, newBulletData, player));

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
    }
}

class Movement {

    private final byte MOVEX = 0;
    private final byte MOVEY = 1;
    private final byte ROTATE = 2;
    private final byte MOVESINX = 3;
    private final byte MOVESINY = 4;
    private final byte ROTATESIN = 5;
    private final byte NEGATIVE = 10;
    private final byte POSITIVE = 15;
    float moveBy;
    float currentAddPosition;
    float speed;
    float progress;
    boolean active;
    BasePart target;
    String type;
    byte typeModifier;
    boolean stopPrevAnim;
    JsonEntry config;

    Array<Movement> animations;

    Movement(JsonEntry actionValue, String target, Array<BasePart> parts, Array<Movement> animations) {

        log("\n loading boss movement, name: " + actionValue.name);

        active = false;
        config = actionValue;
        this.animations = animations;
        for (int i = 0; i < parts.size; i++) {
            if (parts.get(i).name.equals(target)) {
                this.target = parts.get(i);
                break;
            }
        }
        speed = actionValue.getFloat("speed");

        stopPrevAnim = actionValue.getBoolean("stopPreviousAnimations");

        if (actionValue.getString("moveBy").equals("inf")) {
            moveBy = 999779927;
        } else {
            moveBy = actionValue.getFloat("moveBy");
        }

        type = actionValue.name;

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
                    target.x = target.x + (float) (Math.sin(progress) * moveBy) * delta * 60;
                    progress += speed * delta * 10;
                    break;
                }
                case ("moveSinY"): {
                    target.y = target.y + (float) (Math.sin(progress) * moveBy) * delta * 60;
                    progress += speed * delta * 10;
                    break;
                }
                case ("rotateSin"): {
                    target.rotation = (float) (Math.sin(progress) * moveBy) * delta * 60;
                    progress += speed * delta * 10;
                    break;
                }
            }
        }
    }

    void reset() {
        stop();
        progress = 0;
        currentAddPosition = 0;
    }
}

class ComplexMovement extends Movement {

    Vector2[] points;
    boolean loop;
    int currentPoint;
    boolean randomPointMode;

    ComplexMovement(JsonEntry actionValue, String target, Array<BasePart> parts, Array<Movement> animations) {
        super(actionValue, target, parts, animations);
    }

    @Override
    void update(float delta) {
        super.update(delta);
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
            Action action = new Action(phaseData.get(i), parts, animations);
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

    Array<BasePart> baseParts;
    boolean enableCollisions;
    boolean showHealthBar;
    boolean visible;
    boolean enabled;
    String changeTexture;
    BasePart target;
    Array<Movement> movements;
    boolean hasMovement;
    JsonEntry config;

    Action(JsonEntry actionValue, Array<BasePart> baseParts, Array<Movement> animations) {

        log("\n loading boss action, name: " + actionValue.name);

        this.baseParts = baseParts;
        config = actionValue;
        movements = new Array<>();
        String target = actionValue.getString("target");
        for (int i = 0; i < baseParts.size; i++) {
            if (baseParts.get(i).name.equals(target)) {
                this.target = baseParts.get(i);
                break;
            }
        }
        showHealthBar = actionValue.getBoolean("showHealthBar");
        enableCollisions = actionValue.getBoolean("enableCollisions");
        visible = actionValue.getBoolean("visible");
        enabled = actionValue.getBoolean("active");
        changeTexture = actionValue.getString("changeTexture");

        int movementCount;

        if (actionValue.get("move").isBoolean()) {
            log("\n no movement for " + actionValue.name);
            hasMovement = false;
        } else {
            movementCount = actionValue.get("move").size;
            hasMovement = true;
            for (int i = 0; i < movementCount; i++) {
                Movement movement = new Movement(actionValue.get("move").get(i), target, baseParts, animations);
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

        log("\n loading boss phase trigger, trigger name: " + triggerData.name + ", phase name: " + triggerData.parent.parent.name);

        isResetPhase = triggerData.parent.parent.name.equals("RESET");

        conditionsMet = false;
        triggerType = triggerData.getString("triggerType");
        value = triggerData.getFloat("value");
        String targetPart = triggerData.getString("target");
        for (int i2 = 0; i2 < parts.size; i2++) {
            if (parts.get(i2).name.equals(targetPart)) {
                triggerTarget = parts.get(i2);
                break;
            }
        }
        if (triggerTarget == null) {
            log("\n error setting up trigger for part " + targetPart);
        }
        triggerModifier = triggerData.getString("triggerModifier");
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
