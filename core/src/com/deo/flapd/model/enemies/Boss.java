package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
import com.deo.flapd.model.ShipObject;

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

    Boss(String bossName, AssetManager assetManager) {
        bossConfig = new JsonReader().parse(Gdx.files.internal("enemies/bosses/" + bossName + "/config.json"));
        bossAtlas = assetManager.get("enemies/bosses/" + bossName + "/" + bossConfig.getString("textures"));
        parts = new Array<>();
        spawnAt = bossConfig.get("spawnAt").asIntArray();
        for (int i = 0; i < bossConfig.get("parts").size; i++) {
            String type = bossConfig.get("parts").get(i).getString("type");
            switch (type) {
                case ("basePart"):
                    parts.add(new BasePart(bossConfig.get("parts").get(i), bossAtlas));
                    break;
                case ("part"):
                    parts.add(new Part(bossConfig.get("parts").get(i), bossAtlas, parts));
                    break;
                case ("cannon"):
                    parts.add(new Cannon(bossConfig.get("parts").get(i), bossAtlas, parts));
                    break;
                case ("clone"):
                    String cloneType = bossConfig.get("parts").get(i).getString("copyFrom");
                    cloneType = bossConfig.get("parts").get(cloneType).getString("type");
                    switch (cloneType) {
                        case ("basePart"):
                            parts.add(new BasePart(bossConfig.get("parts").get(i), bossAtlas));
                            break;
                        case ("part"):
                            parts.add(new Part(bossConfig.get("parts").get(i), bossAtlas, parts));
                            break;
                        case ("cannon"):
                            parts.add(new Cannon(bossConfig.get("parts").get(i), bossAtlas, parts));
                            break;
                    }
                    break;
            }
        }

        phases = new Array<>();

        for (int i = 0; i < bossConfig.get("phases").size; i++) {
            Phase phase = new Phase(bossConfig.get("phases").get(i), parts);
            phases.add(phase);
        }

        phases.get(0).activate();

    }

    void draw(SpriteBatch batch) {
        for (int i = 0; i < parts.size; i++) {
            parts.get(i).draw(batch);
        }
    }

    void update(float delta) {
        for (int i = 0; i < parts.size; i++) {
            parts.get(i).x = x + parts.get(i).offsetX;
            parts.get(i).y = y + parts.get(i).offsetY;
            parts.get(i).update(delta);
        }
    }

    void spawn() {
        x = spawnAt[0];
        y = spawnAt[1];
        dead = false;
        visible = true;
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
    private Sprite part;
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

    BasePart(JsonValue config, TextureAtlas textures) {
        name = config.name;
        this.config = config;
        if (config.getString("type").equals("clone")) {
            this.config = config.parent.get(config.getString("copyFrom"));
        }
        type = this.config.getString("type");
        health = this.config.getInt("health");
        width = this.config.getInt("width");
        height = this.config.getInt("height");
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

    }

    void draw(SpriteBatch batch) {
        part.draw(batch);
        healthBar.draw(batch, 1);

    }

    void update(float delta) {
        healthBar.setPosition(originX - 12.5f, y - 7);
        part.setRotation(rotation);
        healthBar.act(delta);
    }

    void setTargetPlayer(ShipObject player) {
        this.player = player;
    }

}

class Part extends BasePart {

    private Array<BasePart> parts;
    private float relativeX, relativeY;

    Part(JsonValue config, TextureAtlas textures, Array<BasePart> parts) {
        super(config, textures);
        this.parts = parts;
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
        getOffset(relativeTo);
        offsetX = relativeX;
        offsetY = relativeY;
        hasCollision = this.config.getBoolean("hasCollision");
        if (!hasCollision) {
            collisionEnabled = false;
        }

        System.out.println(offsetX);
        System.out.println(offsetY);
        System.out.println("\n");
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

}

class Cannon extends Part {

    boolean canAim;
    int[] aimAngleLimit;
    float spread;
    float fireRate;
    float fireRateRandomness;
    float timer;

    Cannon(JsonValue partConfig, TextureAtlas textures, Array<BasePart> parts) {
        super(partConfig, textures, parts);

        canAim = this.config.getBoolean("canAim");
        aimAngleLimit = this.config.get("aimAngleLimit").asIntArray();
        spread = this.config.getFloat("spread");

        fireRate = this.config.get("fireRate").getFloat("baseRate");
        fireRateRandomness = this.config.get("fireRate").getFloat("randomness");

    }

    @Override
    void update(float delta) {
        super.update(delta);
        if (canAim) {
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

    float to;
    float speed;
    boolean active;
    BasePart target;
    boolean vertical;

    Movement(JsonValue actionValue, Array<BasePart> parts) {
        active = false;
        for (int i = 0; i < parts.size; i++) {
            if (parts.get(i).name.equals(actionValue.getString("target"))) {
                target = parts.get(i);
            }
        }
        if (actionValue.name.equals("moveLinearX")) {
            speed = actionValue.getFloat("speed");
            to = actionValue.getFloat("targetX");
            vertical = false;
        } else if (actionValue.name.equals("moveLinearY")) {
            speed = actionValue.getFloat("speed");
            to = actionValue.getFloat("targetY");
            vertical = true;
        }
    }

    void start() {
        active = true;
    }

    void update(float delta) {
        if (active) {
            if (vertical) {
                if (target.y < to) {
                    target.y += speed * delta * 10;
                } else if (target.y > to) {
                    target.y -= speed * delta * 10;
                } else {
                    active = false;
                }
            } else {
                if (target.x < to) {
                    target.x += speed * delta * 10;
                } else if (target.y > to) {
                    target.x -= speed * delta * 10;
                } else {
                    active = false;
                }
            }
        }
    }

}

class SinusMovement extends Movement {

    SinusMovement(JsonValue actionValue, Array<BasePart> parts) {
        super(actionValue, parts);
    }

}

class ComplexMovement extends Movement {

    Vector2[] points;
    boolean loop;
    int currentPoint;
    boolean randomPointMode;

    ComplexMovement(JsonValue actionValue, Array<BasePart> parts) {
        super(actionValue, parts);
    }

    @Override
    void update(float delta) {
        super.update(delta);
    }
}

class Phase {

    Array<Action> actions;

    Phase(JsonValue phaseData, Array<BasePart> parts) {
        actions = new Array<>();
        for (int i = 0; i < phaseData.size; i++) {
            Action action = new Action(parts);
            actions.add(action);
        }
    }

    void activate() {
        for (int i = 0; i < actions.size; i++) {
            actions.get(i).activate();

        }
    }

}

class Action {

    Array<BasePart> baseParts;

    Action(Array<BasePart> baseParts) {
        this.baseParts = baseParts;
    }

    void activate() {

    }
}
