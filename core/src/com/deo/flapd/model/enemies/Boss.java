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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class Boss {

    private JsonValue bossConfig;
    private Array<BasePart> parts;
    private TextureAtlas bossAtlas;
    private float x = 1000;
    private float y = 1000;
    private int[] spawnAt;
    boolean dead;
    boolean visible;

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
            parts.get(i).update();
        }
    }

    void spawn(){
        x = spawnAt[0];
        y = spawnAt[1];
        dead = false;
        visible = true;
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

    BasePart(JsonValue config, TextureAtlas textures) {
        name = config.name;
        this.config = config;
        type = config.getString("type");
        health = config.getInt("health");
        width = config.getInt("width");
        height = config.getInt("height");
        part = new Sprite(textures.findRegion(config.getString("texture")));
        part.setSize(width, height);
        originX = width / 2f;
        originY = height / 2;
        if (!config.getString("originX").equals("standard")) {
            originX = config.getFloat("originX");
        }
        if (!config.getString("originY").equals("standard")) {
            originY = config.getFloat("originY");
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
    }

    void update(){
        healthBar.setPosition(originX- 12.5f, y-7);
    }

}

class Part extends BasePart {

    private Array<BasePart> parts;
    private float relativeX, relativeY;

    Part(JsonValue config, TextureAtlas textures, Array<BasePart> parts) {
        super(config, textures);
        this.parts = parts;
        String relativeTo = config.get("offset").getString("relativeTo");
        relativeX = config.get("offset").getFloat("X");
        relativeY = config.get("offset").getFloat("Y");
        getOffset(relativeTo);
        offsetX = relativeX;
        offsetY = relativeY;
        System.out.println(offsetX);
        System.out.println(offsetY);
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

    Cannon(JsonValue partConfig, TextureAtlas textures, Array<BasePart> parts) {
        super(partConfig, textures, parts);
    }

}

class Movement {

    float to;
    float speed;
    boolean active;
    BasePart target;
    boolean vertical;

    Movement(JsonValue actionValue, Array<BasePart> parts){
        active = false;
        for (int i = 0; i < parts.size; i++) {
            if (parts.get(i).name.equals(actionValue.getString("target"))) {
                target = parts.get(i);
            }
        }
        if(actionValue.name.equals("moveLinearX")){
            speed = actionValue.getFloat("speed");
            to = actionValue.getFloat("targetX");
            vertical = false;
        }else if(actionValue.name.equals("moveLinearY")){
            speed = actionValue.getFloat("speed");
            to = actionValue.getFloat("targetY");
            vertical = true;
        }
    }

    void start(){
        active = true;
    }

    void update(float delta){
        if(active) {
            if (vertical) {
                if (target.y < to) {
                    target.y += speed * delta * 10;
                } else if (target.y > to) {
                    target.y -= speed * delta * 10;
                } else {
                    active = false;
                }
            }else{
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

class SinusMovement extends Movement{

    SinusMovement(JsonValue actionValue, Array<BasePart> parts) {
        super(actionValue, parts);
    }
}

class ComplexMovement extends Movement{

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

class Phase{

    Phase(JsonValue phaseData){

    }

}
