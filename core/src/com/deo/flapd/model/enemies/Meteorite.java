package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Drops;
import com.deo.flapd.model.Meteorites;
import com.deo.flapd.model.UraniumCell;

import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomBoolean;
import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class Meteorite extends Entity {

    private float flyingDirection;
    private float rotationSpeed;
    public float radius;
    private ParticleEffect explosionEffect, trail;
    private Sound explosionSound;
    private float soundVolume;

    public boolean remove;

    public Meteorite(AssetManager assetManager, boolean easterEgg, float x, float flyingDirection, float radius) {

        if (easterEgg) {
            entitySprite = new Sprite((Texture) assetManager.get("cat_meteorite.png"));
        } else {
            entitySprite = new Sprite((Texture) assetManager.get("Meteo.png"));
        }

        soundVolume = getFloat("soundVolume");

        if (easterEgg) {
            explosionSound = Gdx.audio.newSound(Gdx.files.internal("sfx/hitcat.ogg"));
        } else {
            explosionSound = Gdx.audio.newSound(Gdx.files.internal("sfx/explosion.ogg"));
        }

        height = radius * 2;
        width = radius * 2;
        originX = radius;
        originY = radius;

        health = radius * 3;
        rotationSpeed = 150 / radius;

        this.x = x;
        y = 480;

        super.init();

        trail = new ParticleEffect();
        trail.load(Gdx.files.internal("particles/particle_nowind.p"), Gdx.files.internal("particles"));
        trail.scaleEffect(radius / 25);
        trail.setPosition(x + originX, y + originY);
        trail.start();

        explosionEffect = new ParticleEffect();
        explosionEffect.load(Gdx.files.internal("particles/explosion.p"), Gdx.files.internal("particles"));
        explosionEffect.scaleEffect(radius / 25);

        this.flyingDirection = flyingDirection;
        this.radius = radius;

    }

    public void drawEffect(SpriteBatch batch) {
        if (isExploded) {
            explosionEffect.draw(batch);
        } else {
            trail.draw(batch);
        }
    }

    public void draw(SpriteBatch batch) {
        entitySprite.draw(batch);
    }

    public void update(float delta) {

        super.update();
        if (isExploded) {
            explosionEffect.update(delta);
        } else {

            x += 130 * flyingDirection * delta;
            y -= 130 * delta;

            rotation += rotationSpeed * delta;

            trail.setPosition(x + originX, y + originY);
            trail.update(delta);
            if (health <= 0) {
                isExploded = true;
                explosionEffect.setPosition(x + originX, y + originY);
                explosionEffect.start();
                if (soundVolume > 0) {
                    explosionSound.play(soundVolume);
                }
                if (getRandomBoolean(50)) {
                    Bonus.Spawn(getRandomInRange(0, 4), entityHitBox);
                    if (getRandomBoolean(10)) {
                        Bonus.Spawn(5, entityHitBox);
                    }
                }
                GameLogic.Score += radius / 15 + 1;
                Meteorites.meteoritesDestroyed++;
                Drops.drop(entityHitBox, 2, 2, 3);
                UraniumCell.Spawn(entityHitBox, getRandomInRange(0, (int) (radius / 17.5f)) + 2, 1, 2);
            }
        }
        boolean outsideBounds = x < -radius - 10 || x > 810 || y < -10;
        remove = (explosionEffect.isComplete() && isExploded) || outsideBounds;
    }

    public void dispose() {
        trail.dispose();
        explosionEffect.dispose();
        explosionSound.dispose();
    }

}
