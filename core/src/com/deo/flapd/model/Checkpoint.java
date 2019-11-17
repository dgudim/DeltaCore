package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.view.GameUi;

import java.util.Random;

public class Checkpoint {

    private Sprite checkpoint_blue, checkpoint_green;
    private boolean checkpointState, effects;
    private Polygon bounds, shipBounds;
    private ParticleEffect fire;
    private ParticleEffect fire2;
    private float speed;
    private float destination_posX;
    private float destination_posY;
    private Random random;
    private Preferences prefs;

    public Checkpoint(AssetManager assetManager, Polygon shipBounds){
        checkpoint_blue = new Sprite((Texture)assetManager.get("checkpoint.png"));
        checkpoint_green = new Sprite((Texture)assetManager.get("checkpoint_green.png"));

        bounds = new Polygon(new float[]{0f, 0f, 102, 0f, 102, 102, 0f, 102});

        random = new Random();

        this.shipBounds = shipBounds;

        bounds.setPosition(-100, -100);

        prefs = Gdx.app.getPreferences("Preferences");
    }

    public void Spawn(float destination_posX, float destination_posY, float speed){
        this.destination_posX = destination_posX;
        this.destination_posY = destination_posY;
        this.speed = speed;

        bounds.setPosition(950, random.nextInt(201)+100);

        fire = new ParticleEffect();
        fire.load(Gdx.files.internal("particles/fire_down.p"), Gdx.files.internal("particles"));

        fire2 = new ParticleEffect();
        fire2.load(Gdx.files.internal("particles/fire_down.p"), Gdx.files.internal("particles"));

        fire.start();
        fire2.start();

        effects = true;

        checkpointState = false;
    }

    public void draw(SpriteBatch batch, boolean is_paused){

        if(destination_posX < bounds.getX()){
            bounds.setPosition(bounds.getX()-speed, bounds.getY());
        }

        if(destination_posY < bounds.getY()){
            bounds.setPosition(bounds.getX(), bounds.getY()-speed);
        }

        if(destination_posY > bounds.getY()){
            bounds.setPosition(bounds.getX(), bounds.getY()+speed);
        }

        if(effects) {
            fire.setPosition(bounds.getX() + 18, bounds.getY() + 14);
            fire2.setPosition(bounds.getX() + 84, bounds.getY() + 14);

            if (!is_paused) {
                fire.draw(batch, Gdx.graphics.getDeltaTime());
                fire2.draw(batch, Gdx.graphics.getDeltaTime());
            } else {
                fire.draw(batch, 0);
                fire2.draw(batch, 0);
            }
        }

        if(checkpointState){
            checkpoint_green.setPosition(bounds.getX(), bounds.getY());
            checkpoint_green.setOrigin(bounds.getBoundingRectangle().getWidth()/2, bounds.getBoundingRectangle().getHeight()/2);
            checkpoint_green.setSize(bounds.getBoundingRectangle().getWidth(), bounds.getBoundingRectangle().getHeight());
            checkpoint_green.draw(batch);
        }else{
            checkpoint_blue.setPosition(bounds.getX(), bounds.getY());
            checkpoint_blue.setOrigin(bounds.getBoundingRectangle().getWidth()/2, bounds.getBoundingRectangle().getHeight()/2);
            checkpoint_blue.setSize(bounds.getBoundingRectangle().getWidth(), bounds.getBoundingRectangle().getHeight());
            checkpoint_blue.draw(batch);
        }

        if(shipBounds.getBoundingRectangle().overlaps(bounds.getBoundingRectangle())){
            checkpointState = true;
            destination_posY = 900;
            destination_posX = bounds.getX();
            speed = 5;
            prefs.putInteger("enemiesKilled",GameUi.enemiesKilled);
            prefs.putInteger("moneyEarned",GameUi.moneyEarned);
            prefs.putInteger("enemiesSpawned",GameUi.enemiesSpawned);
            prefs.putInteger("Score",GameUi.Score);
            prefs.putFloat("Health",GameUi.Health);
            prefs.putFloat("Shield",GameUi.Shield);
            prefs.putBoolean("has1stBossSpawned", GameLogic.has1stBossSpawned);
            prefs.putInteger("bonuses_collected", GameLogic.bonuses_collected);
            prefs.putInteger("lastCheckpoint", GameLogic.lastCheckpoint);
            prefs.putInteger("bulletsShot", Bullet.bulletsShot);
            prefs.putInteger("meteoritesDestroyed", Meteorite.meteoritesDestroyed);
            prefs.putFloat("ShipX", shipBounds.getX());
            prefs.putFloat("ShipY", shipBounds.getY());
            prefs.flush();
        }

        if (bounds.getY() > 850 && checkpointState){
            fire.dispose();
            fire2.dispose();
            checkpointState = false;
            effects = false;
        }

    }

}
