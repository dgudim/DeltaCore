package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class Boss {

    private JsonValue bossConfig;
    private Array<BasePart> parts;
    private TextureAtlas bossAtlas;
    private float x = 1000;
    private float y = 1000;

    Boss(String bossName, AssetManager assetManager) {
        bossConfig = new JsonReader().parse(Gdx.files.internal("enemies/bosses/" + bossName + "/config.json"));
        bossAtlas = assetManager.get("enemies/bosses/" + bossName + "/" + bossConfig.getString("textures"));
        parts = new Array<>();
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
            parts.get(i).y = x + parts.get(i).offsetX;
        }
    }

}

class BasePart {

    float health;
    float height;
    float width;
    float offsetX = 0;
    float offsetY = 0;
    private Sprite part;
    float x;
    float y;
    String name;
    String type;
    JsonValue config;

    BasePart(JsonValue config, TextureAtlas textures) {
        name = config.name;
        this.config = config;
        type = config.getString("type");
        health = config.getInt("health");
        width = config.getInt("width");
        height = config.getInt("height");
        part = new Sprite(textures.findRegion(config.getString("texture")));
        part.setSize(width, height);
        float originX = width / 2f;
        float originY = height / 2;
        if (!config.getString("originX").equals("standard")) {
            originX = config.getFloat("originX");
        }
        if (!config.getString("originY").equals("standard")) {
            originY = config.getFloat("originY");
        }
        part.setOrigin(originX, originY);
    }

    void draw(SpriteBatch batch) {
        part.draw(batch);
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
