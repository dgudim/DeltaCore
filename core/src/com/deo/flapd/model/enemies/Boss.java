package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.ShipObject;

import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.log;

public class Boss {

    private JsonValue bossConfig;
    private Array<BasePart> parts;
    private Array<Phase> phases;
    private TextureAtlas bossAtlas;
    private float x = 1000;
    private float y = 1000;
    private int[] spawnAt;
    boolean dead;
    boolean visible;
    ShipObject player;
    Array<Movement> animations;
    private boolean hasAlreadySpawned;
    private int spawnScore;
    private BasePart body;

    Boss(String bossName, AssetManager assetManager) {

        log("\n\n loading boss config, name: " + bossName);

        bossConfig = new JsonReader().parse(Gdx.files.internal("enemies/bosses/" + bossName + "/config.json"));
        bossAtlas = assetManager.get("enemies/bosses/" + bossName + "/" + bossConfig.getString("textures"));
        parts = new Array<>();
        animations = new Array<>();
        hasAlreadySpawned = false;
        spawnScore = bossConfig.get("spawnConditions").getInt("score") + getRandomInRange(-bossConfig.get("spawnConditions").getInt("randomness"), bossConfig.get("spawnConditions").getInt("randomness"));
        spawnAt = bossConfig.get("spawnAt").asIntArray();
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
                    parts.add(new Cannon(bossConfig.get("parts").get(i), bossAtlas, parts, body));
                    break;
                case ("clone"):
                    String cloneType = bossConfig.get("parts").get(i).getString("copyFrom");
                    cloneType = bossConfig.get("parts").get(cloneType).getString("type");
                    switch (cloneType) {
                        case ("part"):
                            parts.add(new Part(bossConfig.get("parts").get(i), bossAtlas, parts, body));
                            break;
                        case ("cannon"):
                            parts.add(new Cannon(bossConfig.get("parts").get(i), bossAtlas, parts, body));
                            break;
                    }
                    break;
            }
        }

        phases = new Array<>();

        for (int i = 0; i < bossConfig.get("phases").size; i++) {
            Phase phase = new Phase(bossConfig.get("phases").get(i), parts, animations);
            phases.add(phase);
        }

        spawn();
    }

    void draw(SpriteBatch batch) {
        if (visible) {
            for (int i = 0; i < parts.size; i++) {
                parts.get(i).draw(batch);
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
        dead = false;
        visible = true;
        hasAlreadySpawned = true;
        GameLogic.bossWave = true;
        phases.get(0).activate();
    }

    void setTargetPlayer(ShipObject player) {
        this.player = player;
        for (int i = 0; i < parts.size; i++) {
            parts.get(i).setTargetPlayer(player);
        }
    }

}

class BasePart {

    float health;
    float height;
    float width;
    float offsetX = 0;
    float offsetY = 0;
    float originX;
    float originY;
    float rotation;
    Sprite part;
    float x;
    float y;
    String name;
    String type;
    JsonValue config;
    ProgressBar healthBar;
    ShipObject player;
    boolean collisionEnabled = true;
    boolean hasCollision = true;
    boolean visible;
    boolean active;
    boolean showHealthBar;

    ParticleEffect explosionEffect;
    boolean exploded;

    BasePart(JsonValue config, TextureAtlas textures) {

        log("\n loading boss part, name: " + config.name);

        exploded = false;
        name = config.name;
        this.config = config;
        if (config.getString("type").equals("clone")) {
            this.config = config.parent.get(config.getString("copyFrom"));
        }
        type = this.config.getString("type");
        health = this.config.getInt("health");
        width = this.config.getFloat("width");
        height = this.config.getFloat("height");
        hasCollision = this.config.getBoolean("hasCollision");
        part = new Sprite(textures.findRegion(this.config.getString("texture")));
        part.setSize(width, height);
        originX = width / 2f;
        originY = height / 2;
        if (!this.config.getString("originX").equals("standard")) {
            originX = this.config.getFloat("originX");
        }
        if (!this.config.getString("originY").equals("standard")) {
            originY = this.config.getFloat("originY");
        }
        if (!hasCollision) {
            collisionEnabled = false;
        }
        part.setOrigin(originX, originY);

        Pixmap pixmap2 = new Pixmap(0, 6, Pixmap.Format.RGBA8888);
        pixmap2.setColor(Color.RED);
        pixmap2.fill();
        TextureRegionDrawable BarForeground1 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap2)));
        pixmap2.dispose();

        Pixmap pixmap3 = new Pixmap(100, 6, Pixmap.Format.RGBA8888);
        pixmap3.setColor(Color.RED);
        pixmap3.fill();
        TextureRegionDrawable BarForeground2 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap3)));
        pixmap3.dispose();

        Pixmap pixmap = new Pixmap(100, 6, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        TextureRegionDrawable BarBackground = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();

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
            explosionEffect.load(Gdx.files.internal(this.config.getString("explosionEffect")), Gdx.files.internal("particles"));
            explosionEffect.setPosition(x + originX, y + originY);
        }

    }

    void draw(SpriteBatch batch) {
        if (visible) {
            part.draw(batch);
            if (showHealthBar) {
                healthBar.draw(batch, 1);
            }
        }
        if (exploded) {
            explosionEffect.setPosition(x + originX, y + originY);
            explosionEffect.draw(batch);
        }
    }

    void update(float delta) {
        part.setPosition(x, y);
        part.setRotation(rotation);
        if (showHealthBar) {
            healthBar.setValue(health);
            healthBar.setPosition(originX + x - 12.5f, y - 7);
            healthBar.act(delta);
        }
        if (hasCollision && collisionEnabled && health > 0) {
            for (int i = 0; i < player.bullet.bullets.size; i++) {
                if (player.bullet.bullets.get(i).overlaps(part.getBoundingRectangle())) {
                    health -= player.bullet.damages.get(i);
                    player.bullet.removeBullet(i, true);
                }
            }
        }
        if (health <= 0 && !exploded) {
            explosionEffect.start();
            exploded = true;
        }
        if (exploded) {
            explosionEffect.update(delta);
        }
    }

    void setTargetPlayer(ShipObject player) {
        this.player = player;
    }
}

class Part extends BasePart {

    private Array<BasePart> parts;
    private float relativeX, relativeY;
    private BasePart link;

    Part(JsonValue config, TextureAtlas textures, Array<BasePart> parts, BasePart body) {
        super(config, textures);
        this.parts = parts;
        link = body;
        String relativeTo = this.config.get("offset").getString("relativeTo");
        relativeX = this.config.get("offset").getFloat("X");
        relativeY = this.config.get("offset").getFloat("Y");
        if (config.getString("type").equals("clone")) {
            for (int i = 0; i < config.get("override").size; i++) {
                if (config.get("override").get(i).name.equals("offset")) {
                    relativeX = config.get("override").get(i).getFloat("X");
                    relativeY = config.get("override").get(i).getFloat("Y");
                    relativeTo = config.get("override").get(i).getString("relativeTo");
                }
            }

        }

        if (!this.config.get("linked").isBoolean()) {
            for (int i = 0; i < parts.size; i++) {
                if (this.config.getString("linked").equals(parts.get(i).name)) {
                    link = parts.get(i);
                    break;
                }
            }
        }

        getOffset(relativeTo);
        offsetX = relativeX;
        offsetY = relativeY;

    }

    private void getOffset(String relativeTo) {
        for (int i = 0; i < parts.size; i++) {
            if (parts.get(i).name.equals(relativeTo)) {
                relativeX += parts.get(i).offsetX;
                relativeY += parts.get(i).offsetY;
                if (!parts.get(i).type.equals("basePart")) {
                    String relativeToP = parts.get(i).config.get("offset").getString("relativeTo");
                    getOffset(relativeToP);
                }
                break;
            }
        }
    }

    @Override
    void update(float delta) {
        super.update(delta);
    }

    @Override
    void draw(SpriteBatch batch) {
        if (visible && health > 0 && link.health > 0) {
            part.draw(batch);
            if (showHealthBar) {
                healthBar.draw(batch, 1);
            }
        }
        if (exploded) {
            explosionEffect.setPosition(x + originX, y + originY);
            explosionEffect.draw(batch);
        }
    }
}

class Cannon extends Part {

    boolean canAim;
    int[] aimAngleLimit;
    float spread;
    float fireRate;
    float fireRateRandomness;
    float timer;

    Cannon(JsonValue partConfig, TextureAtlas textures, Array<BasePart> parts, BasePart body) {
        super(partConfig, textures, parts, body);

        canAim = this.config.getBoolean("canAim");
        aimAngleLimit = this.config.get("aimAngleLimit").asIntArray();
        spread = this.config.getFloat("spread");

        fireRate = this.config.get("fireRate").getFloat("baseRate");
        fireRateRandomness = this.config.get("fireRate").getFloat("randomness");

    }

    @Override
    void update(float delta) {
        super.update(delta);
        if (canAim && active) {
            rotation = MathUtils.clamp(MathUtils.radiansToDegrees * MathUtils.atan2(y - player.bounds.getY() - 30, x - player.bounds.getX() - 30), aimAngleLimit[0], aimAngleLimit[1]);
        }
        timer += delta;
        if (timer > fireRate + MathUtils.random() * fireRateRandomness) {
            shoot();
        }
    }

    void shoot() {

    }
}

class Movement {

    private final int MOVEX = 0;
    private final int MOVEY = 1;
    private final int ROTATE = 2;
    private final int MOVESINX = 3;
    private final int MOVESINY = 4;
    private final int ROTATESIN = 5;
    float to;
    float speed;
    float progress;
    boolean active;
    BasePart target;
    int type;

    Movement(JsonValue actionValue, String target, Array<BasePart> parts) {

        log("\n loading boss movement, name: " + actionValue.name);

        active = false;
        for (int i = 0; i < parts.size; i++) {
            if (parts.get(i).name.equals(target)) {
                this.target = parts.get(i);
                break;
            }
        }
        speed = actionValue.getFloat("speed");

        switch (actionValue.name) {
            case ("moveLinearX"): {
                to = actionValue.getFloat("targetX");
                type = MOVEX;
                break;
            }
            case ("moveLinearY"): {
                to = actionValue.getFloat("targetY");
                type = MOVEY;
                break;
            }
            case ("moveSinX"): {
                to = actionValue.getFloat("amplitudeX");
                type = MOVESINX;
                break;
            }
            case ("moveSinY"): {
                to = actionValue.getFloat("amplitudeY");
                type = MOVESINY;
                break;
            }
            case ("rotate"): {
                to = actionValue.getFloat("targetAngle");
                type = ROTATE;
                break;
            }
            case ("rotateSin"): {
                to = actionValue.getFloat("amplitude");
                type = ROTATESIN;
                break;
            }
        }
    }

    void start() {
        active = true;
    }

    void update(float delta) {
        if (active) {
            switch (type) {
                case (MOVEX):
                    if (target.x < to) {
                        target.x += speed * delta * 10;
                    } else if (target.x > to) {
                        target.x -= speed * delta * 10;
                    }
                    if (target.x > to - speed * delta * 10 && target.x < to + speed * delta * 10) {
                        active = false;
                    }
                    break;
                case (MOVEY):
                    if (target.y < to) {
                        target.y += speed * delta * 10;
                    } else if (target.y > to) {
                        target.y -= speed * delta * 10;
                    }
                    if (target.y > to - speed * delta * 10 && target.y < to + speed * delta * 10) {
                        active = false;
                    }
                    break;
                case (ROTATE):
                    if (target.rotation < to) {
                        target.rotation += speed * delta * 10;
                    } else if (target.rotation > to) {
                        target.rotation -= speed * delta * 10;
                    }
                    if (target.rotation > to - speed * delta * 10 && target.rotation < to + speed * delta * 10) {
                        active = false;
                    }
                    break;
                case (MOVESINX): {
                    target.x = target.x + (float) (Math.sin(progress) * to);
                    progress += speed * delta * 10;
                    break;
                }
                case (MOVESINY): {
                    target.y = target.y + (float) (Math.sin(progress) * to);
                    progress += speed * delta * 10;
                    break;
                }
                case (ROTATESIN): {
                    target.rotation = (float) (Math.sin(progress) * to);
                    progress += speed * delta * 10;
                    break;
                }
            }
        }
    }
}

class ComplexMovement extends Movement {

    Vector2[] points;
    boolean loop;
    int currentPoint;
    boolean randomPointMode;

    ComplexMovement(JsonValue actionValue, String target, Array<BasePart> parts) {
        super(actionValue, target, parts);
    }

    @Override
    void update(float delta) {
        super.update(delta);
    }
}

class Phase {

    Array<Action> actions;
    boolean activated;
    JsonValue config;
    Array<PhaseTrigger> phaseTriggers;

    Phase(JsonValue phaseData, Array<BasePart> parts, Array<Movement> animations) {

        log("\n loading boss phase, name: " + phaseData.name);

        actions = new Array<>();
        phaseTriggers = new Array<>();
        activated = false;
        config = phaseData;
        for (int i = 0; i < phaseData.size; i++) {
            Action action = new Action(phaseData.get(i), parts, animations);
            actions.add(action);
        }
        JsonValue triggers = null;
        try {
            triggers = phaseData.parent.parent.get("phaseTriggers").get(config.name).get("triggers");
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
            boolean activate = true;
            for (int i = 0; i < phaseTriggers.size; i++) {
                activate = activate && phaseTriggers.get(i).conditionsMet;
            }
            if (activate) {
                activate();
            }
        }
    }

}

class Action {

    Array<BasePart> baseParts;
    boolean enableCollisions;
    boolean showHealthBar;
    boolean visible;
    boolean enabled;
    BasePart target;
    Array<Movement> movements;
    boolean hasMovement;

    Action(JsonValue actionValue, Array<BasePart> baseParts, Array<Movement> animations) {

        log("\n loading boss action, name: " + actionValue.name);

        this.baseParts = baseParts;
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

        int movementCount;

        if (actionValue.get("move").isBoolean()) {
            log("\n no movement for " + actionValue.name);
            hasMovement = false;
        } else {
            movementCount = actionValue.get("move").size;
            hasMovement = true;
            for (int i = 0; i < movementCount; i++) {
                animations.add(new Movement(actionValue.get("move").get(i), target, baseParts));
            }
        }

        movements = animations;

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
        target.visible = visible;
        target.active = enabled;
        target.showHealthBar = showHealthBar;
    }
}

class PhaseTrigger {

    boolean conditionsMet;
    float value;
    String triggerType;
    String triggerModifier;
    BasePart triggerTarget;

    PhaseTrigger(JsonValue triggerData, Array<BasePart> parts) {

        log("\n loading boss phase trigger, trigger name: " + triggerData.name + ", phase name: " + triggerData.parent.parent.name);

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
