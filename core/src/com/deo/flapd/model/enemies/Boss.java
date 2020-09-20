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

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.log;

public class Boss {

    public JsonValue bossConfig;
    private Array<BasePart> parts;
    private Array<Phase> phases;
    private TextureAtlas bossAtlas;
    private float x = 1500;
    private float y = 1500;
    private int[] spawnAt;
    boolean visible;
    ShipObject player;
    Array<Movement> animations;
    public boolean hasAlreadySpawned;
    private int spawnScore;
    private BasePart body;

    Boss(String bossName, AssetManager assetManager) {

        log("\n\n loading boss config, name: " + bossName);

        bossConfig = new JsonReader().parse(Gdx.files.internal("enemies/bosses/" + bossName + "/config.json"));
        bossAtlas = assetManager.get("enemies/bosses/" + bossName + "/" + bossConfig.getString("textures"));
        parts = new Array<>();
        animations = new Array<>();
        hasAlreadySpawned = getBoolean("boss_spawned_"+bossConfig.name);
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

    void dispose() {
        for (int i = 0; i < parts.size; i++) {
            parts.get(i).dispose();
        }
    }

    void reset() {
        new Runnable() {
            @Override
            public void run() {
                visible = false;
                body.x = 1500;
                body.y = 1500;
                GameLogic.bossWave = false;
                for(int i = 0; i<animations.size; i++){
                    animations.get(i).reset();
                }
                for(int i = 0; i<parts.size; i++){
                    parts.get(i).reset();
                }
                for(int i = 0; i<phases.size; i++){
                    phases.get(i).reset();
                }
                System.out.println("boss reset success!");
            }
        }.run();
    }
}

class BasePart {

    float health;
    float originalHealth;
    float height;
    float width;
    float offsetX = 0;
    float offsetY = 0;
    float originalOffsetX = 0;
    float originalOffsetY = 0;
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
    boolean hasCollision;
    boolean visible;
    boolean active;
    boolean showHealthBar;

    int layer;

    ParticleEffect explosionEffect;
    boolean exploded;

    TextureAtlas textures;

    Array<BasePart> links;

    BasePart(JsonValue config, TextureAtlas textures) {

        log("\n loading boss part, name: " + config.name);

        exploded = false;
        name = config.name;
        this.config = config;
        this.textures = textures;
        links = new Array<>();
        if (config.getString("type").equals("clone")) {
            this.config = config.parent.get(config.getString("copyFrom"));
        }
        type = this.config.getString("type");
        health = this.config.getInt("health");
        originalHealth = health;
        width = this.config.getFloat("width");
        height = this.config.getFloat("height");
        layer = this.config.getInt("layer");
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
            log("\n creating explosion effect for part: " + config.name);
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
            explosionEffect.draw(batch);
        }
    }

    void update(float delta) {
        part.setPosition(x, y);
        part.setRotation(rotation);
        if (showHealthBar) {
            healthBar.setValue(health);
            healthBar.setPosition(width / 2 + x - 12.5f, y - 7);
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
            explode();
            for (int i = 0; i < links.size; i++) {
                if (!links.get(i).exploded && links.get(i).explosionEffect != null) {
                    links.get(i).explode();
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
        }
    }

    void addLinkedPart(BasePart part) {
        links.add(part);
    }

    void explode() {
        explosionEffect.start();
        explosionEffect.setPosition(x + width / 2, y + height / 2);
        exploded = true;
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
    }
}

class Part extends BasePart {

    private Array<BasePart> parts;
    private float relativeX, relativeY;
    private BasePart link;
    private BasePart body;

    Part(JsonValue config, TextureAtlas textures, Array<BasePart> parts, BasePart body) {
        super(config, textures);
        this.parts = parts;
        link = body;
        this.body = body;
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

        boolean customLink = false;

        if (!this.config.get("linked").isBoolean()) {
            for (int i = 0; i < parts.size; i++) {
                if (this.config.getString("linked").equals(parts.get(i).name)) {
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
                    String relativeToP = parts.get(i).config.get("offset").getString("relativeTo");
                    getOffset(relativeToP);
                }
                break;
            }
        }
    }

    @Override
    void draw(SpriteBatch batch) {
        if (visible && health > 0 && link.health > 0 && body.health > 0) {
            part.draw(batch);
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
    int[] aimAngleLimit;
    float spread;
    float fireRate;
    float fireRateRandomness;
    float timer;
    JsonValue bulletData;

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
        if (timer > fireRate + MathUtils.random() * fireRateRandomness && health > 0) {
            shoot();
        }
    }

    void shoot() {

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
    float amplitude;
    float currentAddPosition;
    float speed;
    float progress;
    boolean active;
    BasePart target;
    byte type;
    byte typeModifier;
    boolean stopPrevAnim;
    JsonValue config;

    Array<Movement> animations;

    Movement(JsonValue actionValue, String target, Array<BasePart> parts, Array<Movement> animations) {

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

        switch (actionValue.name) {
            case ("moveLinearX"): {
                moveBy = actionValue.getFloat("moveBy");
                type = MOVEX;
                break;
            }
            case ("moveLinearY"): {
                moveBy = actionValue.getFloat("moveBy");
                type = MOVEY;
                break;
            }
            case ("moveSinX"): {
                amplitude = actionValue.getFloat("amplitude");
                type = MOVESINX;
                break;
            }
            case ("moveSinY"): {
                amplitude = actionValue.getFloat("amplitude");
                type = MOVESINY;
                break;
            }
            case ("rotate"): {
                moveBy = actionValue.getFloat("moveBy");
                type = ROTATE;
                break;
            }
            case ("rotateSin"): {
                amplitude = actionValue.getFloat("amplitude");
                type = ROTATESIN;
                break;
            }
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
                case (MOVEX):
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
                case (MOVEY):
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
                case (ROTATE):
                    if (typeModifier == POSITIVE) {
                        target.rotation += speed * delta * 10;
                    } else if (typeModifier == NEGATIVE) {
                        target.rotation -= speed * delta * 10;
                    }
                    break;
                case (MOVESINX): {
                    target.x = target.x + (float) (Math.sin(progress) * amplitude) * delta * 60;
                    progress += speed * delta * 10;
                    break;
                }
                case (MOVESINY): {
                    target.y = target.y + (float) (Math.sin(progress) * amplitude) * delta * 60;
                    progress += speed * delta * 10;
                    break;
                }
                case (ROTATESIN): {
                    target.rotation = (float) (Math.sin(progress) * amplitude) * delta * 60;
                    progress += speed * delta * 10;
                    break;
                }
            }
        }
    }

    void reset() {
        stop();
        progress = 0;
    }
}

class ComplexMovement extends Movement {

    Vector2[] points;
    boolean loop;
    int currentPoint;
    boolean randomPointMode;

    ComplexMovement(JsonValue actionValue, String target, Array<BasePart> parts, Array<Movement> animations) {
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
    JsonValue config;
    Array<PhaseTrigger> phaseTriggers;

    Phase(JsonValue phaseData, Array<BasePart> parts, Array<Movement> animations, Boss boss) {

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
                PhaseTrigger trigger = new PhaseTrigger(triggers.get(i), parts, boss);
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

    void reset() {
        activated = false;
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
    JsonValue config;

    Action(JsonValue actionValue, Array<BasePart> baseParts, Array<Movement> animations) {

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
            target.part.setRegion(target.textures.findRegion(changeTexture));
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
    Boss boss;

    PhaseTrigger(JsonValue triggerData, Array<BasePart> parts, Boss boss) {

        log("\n loading boss phase trigger, trigger name: " + triggerData.name + ", phase name: " + triggerData.parent.parent.name);

        isResetPhase = triggerData.name.equals("RESET");

        this.boss = boss;

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
        if(conditionsMet && isResetPhase){
            boss.reset();
            System.out.println("boss reset");
        }
    }

}
