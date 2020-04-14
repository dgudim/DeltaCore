package com.deo.flapd.control;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Checkpoint;
import com.deo.flapd.model.Meteorite;
import com.deo.flapd.model.SpaceShip;
import com.deo.flapd.model.enemies.Boss_battleShip;
import com.deo.flapd.model.enemies.Boss_evilEye;
import com.deo.flapd.model.enemies.Kamikadze;

import java.util.Random;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getString;


public class GameLogic {

    private Polygon bounds;

    private Random random;

    private float difficulty;

    public static int bonuses_collected;
    private float millis;

    public static boolean bossWave, has1stBossSpawned, has2ndBossSpawned;

    public static int lastCheckpoint;

    public static int Score;
    public static int enemiesKilled;
    public static int enemiesSpawned;
    public static int money, moneyEarned;

    private float speedMultiplier;

    private Game game;

    private float ShootingSpeed;

    private Bullet bullet;
    private Meteorite meteorite;
    private Kamikadze kamikadze;
    private Boss_battleShip boss_battleShip;
    private Checkpoint checkpoint;
    private Boss_evilEye boss_evilEye;

    public GameLogic(Polygon bounds, boolean newGame, Game game, Bullet bullet, Meteorite meteorite, Kamikadze kamikadze, Boss_battleShip boss_battleShip, Checkpoint checkpoint, Boss_evilEye boss_evilEye) {
        this.bounds = bounds;
        random = new Random();

        this.game = game;

        this.bullet = bullet;
        this.meteorite = meteorite;
        this.kamikadze = kamikadze;
        this.boss_battleShip = boss_battleShip;
        this.checkpoint = checkpoint;
        this.boss_evilEye = boss_evilEye;

        difficulty = getFloat("difficulty");

        speedMultiplier = new JsonReader().parse(Gdx.files.internal("shop/tree.json")).get(getString("currentEngine")).get("parameterValues").asIntArray()[0];

        if(!newGame){
            bonuses_collected = getInteger("bonuses_collected");
            lastCheckpoint = getInteger("lastCheckpoint");
            has1stBossSpawned = getBoolean("has1stBossSpawned");
            has2ndBossSpawned = getBoolean("has2ndBossSpawned");

            Score = getInteger("Score");
            moneyEarned = getInteger("moneyEarned");

            enemiesKilled = getInteger("enemiesKilled");
            enemiesSpawned = getInteger("enemiesSpawned");
        }else {
            bonuses_collected = 0;
            lastCheckpoint = 0;
            has1stBossSpawned = false;
            has2ndBossSpawned = false;

            Score = 0;
            moneyEarned = 0;

            enemiesKilled = 0;
            enemiesSpawned = 0;
        }
        money = getInteger("money");
        bossWave = false;

        ShootingSpeed = bullet.getShootingSpeed();
    }

    public void handleInput(float delta, float deltaX, float deltaY, boolean is_firing, boolean is_firing_secondary) {
        deltaX*=speedMultiplier;
        deltaY*=speedMultiplier;

        if(Gdx.input.isKeyPressed(Input.Keys.W))
            deltaY = speedMultiplier;
        if(Gdx.input.isKeyPressed(Input.Keys.A))
            deltaX = -speedMultiplier;
        if(Gdx.input.isKeyPressed(Input.Keys.S))
            deltaY = -speedMultiplier;
        if(Gdx.input.isKeyPressed(Input.Keys.D))
            deltaX = speedMultiplier;
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE))
            is_firing = true;
        if(Gdx.input.isKeyPressed(Input.Keys.M))
            is_firing_secondary = true;
        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
            game.pause();


        bounds.setPosition(bounds.getX() + 250 * deltaX * delta, bounds.getY() + 250 * deltaY * delta);
        bounds.setRotation(MathUtils.clamp((deltaY - deltaX) * 7, -9, 9));

        if (is_firing && millis > 10/ShootingSpeed) {
            bullet.Spawn(1, false);
            millis = 0;
        }

        if (is_firing_secondary && millis > 10/ShootingSpeed) {
            bullet.Spawn(2.25f, true);
            millis = 0;
        }

        millis = millis + 50 * (bonuses_collected / 50.0f + 1) * delta;

        if (!bossWave) {

            if ((random.nextInt(40) == 5 || random.nextInt(40) > 37) && enemiesKilled >= 50) {
                if ((random.nextInt(40) == 5 || random.nextInt(400) > 370) && enemiesKilled >= 50) {
                    kamikadze.Spawn((int) (800 * difficulty), 0.3f, 7);
                }
            }

            if (random.nextInt(6000) == 5770) {
                meteorite.Spawn(random.nextInt(480), (random.nextInt(60) - 30) / 10f, random.nextInt(40) + 30*difficulty);
            }

            if(Score > lastCheckpoint+9000 && !bossWave){
                lastCheckpoint = Score;
                checkpoint.Spawn(random.nextInt(300)+150, random.nextInt(201)+100, 1);
            }
        }

        if (Score > 30000 && !has1stBossSpawned) {
            bossWave = true;
            has1stBossSpawned = true;
            boss_evilEye.Spawn();
        }

        if (Score > 70000 && !has2ndBossSpawned) {
            bossWave = true;
            has2ndBossSpawned = true;
            boss_battleShip.Spawn();
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

    public void detectCollisions(boolean is_paused) {

        for (int i = 0; i < Kamikadze.enemies.size; i++) {
            if (!is_paused) {

                Rectangle enemy = Kamikadze.enemies.get(i);

                if (enemy.overlaps(bounds.getBoundingRectangle())) {

                    SpaceShip.takeDamage(Kamikadze.healths.get(i));

                    Score = Score + Kamikadze.healths.get(i) / 2;

                    enemiesKilled++;

                    Kamikadze.removeEnemy(i, true);

                }

                for (int i2 = 0; i2 < Bullet.bullets.size; i2++) {
                    if (enemy.overlaps(Bullet.bullets.get(i2))) {

                        Score = (int) (Score + 30 + enemy.x / 20);

                        Kamikadze.healths.set(i, Kamikadze.healths.get(i) - Bullet.damages.get(i2));

                        Bullet.removeBullet(i2, true);

                        if (Kamikadze.healths.get(i) <= 0) {

                            Kamikadze.removeEnemy(i, true);

                            enemiesKilled++;
                        }
                    }
                }

                for (int i3 = 0; i3 < Meteorite.meteorites.size; i3++) {
                    if (Meteorite.meteorites.get(i3).overlaps(enemy)) {

                        if (Meteorite.healths.get(i3) > Kamikadze.healths.get(i)) {
                            Meteorite.healths.set(i3, Meteorite.healths.get(i3) - Kamikadze.healths.get(i));
                            Kamikadze.removeEnemy(i, true);

                        } else if (Meteorite.healths.get(i3) < Kamikadze.healths.get(i)) {
                            Kamikadze.healths.set(i, (int) (Kamikadze.healths.get(i)  - Meteorite.healths.get(i3)));
                            Meteorite.removeMeteorite(i3, true);

                            Meteorite.meteoritesDestroyed++;

                        } else {
                            Kamikadze.removeEnemy(i, true);
                            Meteorite.removeMeteorite(i3, true);

                            Meteorite.meteoritesDestroyed++;

                        }
                    }
                }
            }
        }

        for (int i = 0; i < Meteorite.meteorites.size; i++) {

            Rectangle meteorite = Meteorite.meteorites.get(i);
            Float radius = Meteorite.radiuses.get(i);

            if (!is_paused) {

                if (meteorite.overlaps(bounds.getBoundingRectangle())) {

                    SpaceShip.takeDamage(Meteorite.healths.get(i));

                    Score = (int) (Score + Meteorite.radiuses.get(i) / 2);

                    Meteorite.removeMeteorite(i, true);

                    Meteorite.meteoritesDestroyed++;

                }

                for (int i2 = 0; i2 < Bullet.bullets.size; i2++) {
                    if (meteorite.overlaps(Bullet.bullets.get(i2))) {

                        Score = (int) (Score + radius / 2);

                        Meteorite.healths.set(i, Meteorite.healths.get(i) - Bullet.damages.get(i2));

                        Bullet.removeBullet(i2, true);

                        if (Meteorite.healths.get(i) <= 0) {

                            Meteorite.removeMeteorite(i, true);

                            Meteorite.meteoritesDestroyed++;

                        }
                    }
                }
            }
        }

    }
}
