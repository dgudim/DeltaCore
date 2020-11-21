package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.enemies.Bosses;

import java.util.Random;

import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putFloat;
import static com.deo.flapd.utils.DUtils.putInteger;

public class Checkpoint {

    private Sprite checkpoint_blue, checkpoint_green;
    private boolean checkpointState, effects;
    private Polygon bounds, shipBounds;
    private ShipObject player;
    private ParticleEffect fire;
    private ParticleEffect fire2;
    private float speed;
    private float destination_posX;
    private float destination_posY;
    private Random random;

    public Checkpoint(AssetManager assetManager, ShipObject ship) {
        checkpoint_blue = new Sprite((Texture) assetManager.get("checkpoint.png"));
        checkpoint_green = new Sprite((Texture) assetManager.get("checkpoint_green.png"));

        bounds = new Polygon(new float[]{0f, 0f, 102, 0f, 102, 102, 0f, 102});

        player = ship;

        shipBounds = player.bounds;

        random = new Random();

        this.shipBounds = shipBounds;

        bounds.setPosition(-200, -200);

        checkpoint_blue.setSize(0, 0);
        checkpoint_green.setSize(0, 0);
        checkpoint_blue.setPosition(1000, 1000);
        checkpoint_green.setPosition(1000, 1000);
    }

    public void Spawn(float destination_posX, float destination_posY, float speed) {
        this.destination_posX = destination_posX;
        this.destination_posY = destination_posY;
        this.speed = speed;

        bounds.setPosition(950, random.nextInt(201) + 100);

        fire = new ParticleEffect();
        fire.load(Gdx.files.internal("particles/fire_down.p"), Gdx.files.internal("particles"));

        fire2 = new ParticleEffect();
        fire2.load(Gdx.files.internal("particles/fire_down.p"), Gdx.files.internal("particles"));

        fire.start();
        fire2.start();

        effects = true;

        checkpointState = false;
    }

    public void drawEffects(SpriteBatch batch, float delta) {
        if (effects) {
            fire.setPosition(bounds.getX() + 18, bounds.getY() + 14);
            fire2.setPosition(bounds.getX() + 84, bounds.getY() + 14);

            fire.draw(batch, delta);
            fire2.draw(batch, delta);
        }
    }

    public void drawBase(SpriteBatch batch) {

        if (destination_posX < bounds.getX()) {
            bounds.setPosition(bounds.getX() - speed, bounds.getY());
        }

        if (destination_posY < bounds.getY()) {
            bounds.setPosition(bounds.getX(), bounds.getY() - speed);
        }

        if (destination_posY > bounds.getY()) {
            bounds.setPosition(bounds.getX(), bounds.getY() + speed);
        }

        if (checkpointState) {
            checkpoint_green.setPosition(bounds.getX(), bounds.getY());
            checkpoint_green.setOrigin(bounds.getBoundingRectangle().getWidth() / 2, bounds.getBoundingRectangle().getHeight() / 2);
            checkpoint_green.setSize(bounds.getBoundingRectangle().getWidth(), bounds.getBoundingRectangle().getHeight());
            checkpoint_green.draw(batch);
        } else {
            checkpoint_blue.setPosition(bounds.getX(), bounds.getY());
            checkpoint_blue.setOrigin(bounds.getBoundingRectangle().getWidth() / 2, bounds.getBoundingRectangle().getHeight() / 2);
            checkpoint_blue.setSize(bounds.getBoundingRectangle().getWidth(), bounds.getBoundingRectangle().getHeight());
            checkpoint_blue.draw(batch);
        }

        if (shipBounds.getBoundingRectangle().overlaps(bounds.getBoundingRectangle()) && player.Health > 0 && !checkpointState) {
            checkpointState = true;
            destination_posY = 900;
            destination_posX = bounds.getX();
            speed = 5;
            putInteger("enemiesKilled", GameLogic.enemiesKilled);
            putInteger("moneyEarned", GameLogic.moneyEarned);
            putInteger("Score", GameLogic.Score);
            putFloat("Health", player.Health);
            putFloat("Shield", player.Shield);
            putFloat("Charge", player.Charge);
            for(int i = 0; i< Bosses.bosses.size; i++){
                putBoolean("boss_spawned_"+Bosses.bosses.get(i).bossConfig.name, Bosses.bosses.get(i).hasAlreadySpawned);
            }
            putInteger("bonuses_collected", GameLogic.bonuses_collected);
            putInteger("lastCheckpoint", GameLogic.lastCheckpoint);
            putInteger("bulletsShot", player.bulletsShot);
            putInteger("meteoritesDestroyed", Meteorites.meteoritesDestroyed);
            putFloat("ShipX", shipBounds.getX());
            putFloat("ShipY", shipBounds.getY());
            putInteger("money", GameLogic.money);
        }

        if (bounds.getY() > 850 && checkpointState) {
            fire.dispose();
            fire2.dispose();
            checkpointState = false;
            effects = false;
        }

    }

}
