package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Meteorite extends Entity {

    private final float flyingDirection;
    private final float rotationSpeed;
    public float radius;
    private final ParticleEffect trail;

    public boolean remove;

    public Meteorite(AssetManager assetManager, boolean easterEgg, float x, float flyingDirection, float radius) {

        if (easterEgg) {
            entitySprite = new Sprite((Texture) assetManager.get("cat_meteorite.png"));
        } else {
            entitySprite = new Sprite((Texture) assetManager.get("Meteo.png"));
        }

        height = radius * 2;
        width = radius * 2;
        originX = radius;
        originY = radius;

        rotationSpeed = 1050 / radius;

        this.x = x;
        y = 480;

        super.init();

        trail = new ParticleEffect();
        trail.load(Gdx.files.internal("particles/particle_nowind.p"), Gdx.files.internal("particles"));
        trail.scaleEffect(radius / 25);
        trail.setPosition(x + originX, y + originY);
        trail.start();

        this.flyingDirection = flyingDirection;
        this.radius = radius;

    }

    public void drawEffect(SpriteBatch batch) {
        trail.draw(batch);
    }

    public void draw(SpriteBatch batch) {
        entitySprite.draw(batch);
    }

    public void update(float delta) {

        super.update();

        x += 53 * flyingDirection * delta;
        y -= 53 * delta;

        rotation += rotationSpeed * delta;

        trail.setPosition(x + originX, y + originY);
        trail.update(delta);

        remove = x < -radius - 300 || x > 1110 || y < -300;
    }

    public void dispose() {
        trail.dispose();
    }

}
