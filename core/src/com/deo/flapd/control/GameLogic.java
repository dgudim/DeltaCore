package com.deo.flapd.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Polygon;
import com.deo.flapd.model.enemies.BasicEnemy;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Meteorite;
import com.deo.flapd.model.enemies.Kamikadze;
import com.deo.flapd.model.enemies.ShotgunEnemy;
import com.deo.flapd.model.enemies.SniperEnemy;
import com.deo.flapd.view.GameUi;

import java.util.Random;


public class GameLogic{

    private Polygon bounds;

    private Random random;

    private Preferences prefs;

    private float difficulty;

    public static int bonuses_collected;
    private float millis;

    public GameLogic(Polygon bounds){
        this.bounds = bounds;
        random = new Random();

        prefs = Gdx.app.getPreferences("Preferences");
        difficulty = prefs.getFloat("difficulty");

        bonuses_collected = 0;
    }

    public void handleInput(float deltaX, float deltaY, boolean is_firing, Bullet bullet, BasicEnemy enemy, ShotgunEnemy shotgunEnemy, SniperEnemy sniperEnemy, Meteorite meteorite, Kamikadze kamikadze) {
        bounds.setPosition(bounds.getX() + 300 * deltaX * Gdx.graphics.getDeltaTime(), bounds.getY() + 300 * deltaY * Gdx.graphics.getDeltaTime());
        bounds.setRotation((deltaY-deltaX)*7);

        if (is_firing && millis>10) {
            bullet.Spawn(20, 1);
            millis = 0;
        }
        millis = millis + 50*(bonuses_collected/10+1)*Gdx.graphics.getDeltaTime();

        if((random.nextInt(40) == 5 || random.nextInt(40) > 37)&& GameUi.enemiesKilled<=3){
            enemy.Spawn(40*difficulty, 0.8f);
        }

        if((random.nextInt(40) == 5 || random.nextInt(40) > 37)&& GameUi.enemiesKilled>=50){
            sniperEnemy.Spawn(60*difficulty, 0.3f);
            if((random.nextInt(40) == 5 || random.nextInt(400) > 370)&& GameUi.enemiesKilled>=50) {
                kamikadze.Spawn(400, 0.3f, 7);
            }
        }

        if((random.nextInt(40) == 10 || random.nextInt(40) > 37)&&GameUi.enemiesKilled>=3){
            shotgunEnemy.Spawn(100*difficulty, 0.5f);
        }

        if(random.nextInt(6000) == 5770){
            meteorite.Spawn(random.nextInt(480)*difficulty, (random.nextInt(60)-30)/10, random.nextInt(40)+30);
        }

        for (int i2 = 0; i2 < enemy.enemies.size; i2 ++) {
            if(random.nextInt(20)>15 && GameUi.enemiesKilled<=3) {
                enemy.shoot(i2);
            }
        }

        for (int i2 = 0; i2 < shotgunEnemy.enemies.size; i2 ++) {
            if(random.nextInt(40)>37 && GameUi.enemiesKilled>=3) {
                shotgunEnemy.shoot(i2);
            }
        }

        for (int i2 = 0; i2 < sniperEnemy.enemies.size; i2 ++) {
            if(random.nextInt(50)>48 && GameUi.enemiesKilled>=50) {
                sniperEnemy.shoot(i2);
            }
        }

        if (bounds.getX() < 0) {
            bounds.setPosition(0, bounds.getY());
        }
        if (bounds.getX() > 800 - bounds.getBoundingRectangle().getWidth()) {
            bounds.setPosition(800 - bounds.getBoundingRectangle().getWidth(), bounds.getY());
        }
        if (bounds.getY() < 0) {
            bounds.setPosition(bounds.getX(), 0);
        }
        if (bounds.getY() > 480 - bounds.getBoundingRectangle().getHeight()) {
            bounds.setPosition(bounds.getX(), 480 - bounds.getBoundingRectangle().getHeight());
        }
    }
}
