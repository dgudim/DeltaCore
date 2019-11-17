package com.deo.flapd.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.deo.flapd.model.Checkpoint;
import com.deo.flapd.model.SpaceShip;
import com.deo.flapd.model.enemies.BasicEnemy;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Meteorite;
import com.deo.flapd.model.enemies.Boss_battleShip;
import com.deo.flapd.model.bullets.EnemyBullet;
import com.deo.flapd.model.bullets.EnemyBullet_shotgun;
import com.deo.flapd.model.bullets.EnemyBullet_sniper;
import com.deo.flapd.model.enemies.Kamikadze;
import com.deo.flapd.model.enemies.ShotgunEnemy;
import com.deo.flapd.model.enemies.SniperEnemy;
import com.deo.flapd.view.GameUi;

import java.util.Random;


public class GameLogic {

    private Polygon bounds;

    private Random random;

    private float difficulty;

    public static int bonuses_collected;
    private float millis;

    public static boolean bossWave, has1stBossSpawned;

    public static int lastCheckpoint;

    public GameLogic(Polygon bounds, boolean newGame) {
        this.bounds = bounds;
        random = new Random();

        Preferences prefs = Gdx.app.getPreferences("Preferences");
        difficulty = prefs.getFloat("difficulty");

        if(!newGame){
            bonuses_collected = prefs.getInteger("bonuses_collected");
            lastCheckpoint = prefs.getInteger("lastCheckpoint");
            has1stBossSpawned = prefs.getBoolean("has1stBossSpawned");
        }else {
            bonuses_collected = 0;
            lastCheckpoint = 0;
            has1stBossSpawned = false;
        }
        bossWave = false;
    }

    public void handleInput(float deltaX, float deltaY, boolean is_firing, Bullet bullet, BasicEnemy enemy, ShotgunEnemy shotgunEnemy, SniperEnemy sniperEnemy, Meteorite meteorite, Kamikadze kamikadze, Boss_battleShip boss_battleShip, Checkpoint checkpoint) {
        bounds.setPosition(bounds.getX() + 300 * deltaX * Gdx.graphics.getDeltaTime(), bounds.getY() + 300 * deltaY * Gdx.graphics.getDeltaTime());
        bounds.setRotation((deltaY - deltaX) * 7);

        if (is_firing && millis > 10) {
            bullet.Spawn(40, 1);
            millis = 0;
        }

        millis = millis + 50 * (bonuses_collected / 10f + 1) * Gdx.graphics.getDeltaTime();

        if (!bossWave) {
            if ((random.nextInt(40) == 5 || random.nextInt(40) > 37) && GameUi.enemiesKilled <= 3) {
                enemy.Spawn(80 * difficulty, 0.8f);
            }

            if ((random.nextInt(40) == 5 || random.nextInt(40) > 37) && GameUi.enemiesKilled >= 50) {
                sniperEnemy.Spawn(120 * difficulty, 0.3f);
                if ((random.nextInt(40) == 5 || random.nextInt(400) > 370) && GameUi.enemiesKilled >= 50) {
                    kamikadze.Spawn(800, 0.3f, 7);
                }
            }

            if ((random.nextInt(40) == 10 || random.nextInt(40) > 37) && GameUi.enemiesKilled >= 3) {
                shotgunEnemy.Spawn(200 * difficulty, 0.5f);
            }

            if (random.nextInt(6000) == 5770) {
                meteorite.Spawn(random.nextInt(480) * difficulty, (random.nextInt(60) - 30) / 10, random.nextInt(40) + 30);
            }

            for (int i2 = 0; i2 < BasicEnemy.enemies.size; i2++) {
                if (random.nextInt(20) > 15 && GameUi.enemiesKilled <= 3) {
                    enemy.shoot(i2);
                }
            }

            for (int i2 = 0; i2 < ShotgunEnemy.enemies.size; i2++) {
                if (random.nextInt(40) > 37 && GameUi.enemiesKilled >= 3) {
                    shotgunEnemy.shoot(i2);
                }
            }

            for (int i2 = 0; i2 < SniperEnemy.enemies.size; i2++) {
                if (random.nextInt(50) > 48 && GameUi.enemiesKilled >= 50) {
                    sniperEnemy.shoot(i2);
                }
            }

            if(GameUi.Score > lastCheckpoint+9000 && !bossWave){
                lastCheckpoint = GameUi.Score;
                checkpoint.Spawn(random.nextInt(300)+150, random.nextInt(201)+100, 1);
            }
        }

        if (GameUi.Score > 80000 && !has1stBossSpawned) {
            bossWave = true;
            has1stBossSpawned = true;
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

        for (int i = 0; i < BasicEnemy.enemies.size; i++) {

            Rectangle enemy = BasicEnemy.enemies.get(i);

            if (!is_paused) {

                if (enemy.overlaps(bounds.getBoundingRectangle())) {

                    if (GameUi.Shield >= BasicEnemy.healths.get(i)) {
                        GameUi.Shield -= BasicEnemy.healths.get(i);
                        SpaceShip.set_color(1, 0, 1, true);
                    } else {
                        GameUi.Health = GameUi.Health - (BasicEnemy.healths.get(i) - GameUi.Shield) / 5;
                        GameUi.Shield = 0;
                        SpaceShip.set_color(1, 0, 1, false);
                    }

                    GameUi.Score = (int) (GameUi.Score + BasicEnemy.healths.get(i) / 2);

                    GameUi.enemiesKilled++;

                    BasicEnemy.removeEnemy(i, true);

                }

                for (int i2 = 0; i2 < Bullet.bullets.size; i2++) {
                    if (enemy.overlaps(Bullet.bullets.get(i2))) {

                        GameUi.Score = (int) (GameUi.Score + 30 + enemy.x / 20);

                        BasicEnemy.healths.set(i, BasicEnemy.healths.get(i) - Bullet.damages.get(i2));

                        Bullet.removeBullet(i2, true);

                        BasicEnemy.colors.get(i).set(Color.RED);

                        if (BasicEnemy.healths.get(i) <= 0) {

                            BasicEnemy.removeEnemy(i, true);

                            GameUi.enemiesKilled++;
                        }
                    }
                }

                for (int i3 = 0; i3 < Meteorite.meteorites.size; i3++) {
                    if (Meteorite.meteorites.get(i3).overlaps(enemy)) {

                        if (Meteorite.healths.get(i3) > BasicEnemy.healths.get(i)) {
                            Meteorite.healths.set(i3, Meteorite.healths.get(i3) - BasicEnemy.healths.get(i));
                            BasicEnemy.removeEnemy(i, true);

                        } else if (Meteorite.healths.get(i3) < BasicEnemy.healths.get(i)) {
                            BasicEnemy.healths.set(i, BasicEnemy.healths.get(i) - Meteorite.healths.get(i3));
                            Meteorite.removeMeteorite(i3, true);

                            BasicEnemy.colors.get(i).set(Color.RED);

                        } else {
                            BasicEnemy.removeEnemy(i, true);
                            Meteorite.removeMeteorite(i3, true);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < Kamikadze.enemies.size; i++) {
            if (!is_paused) {

                Rectangle enemy = Kamikadze.enemies.get(i);

                if (enemy.overlaps(bounds.getBoundingRectangle())) {

                    if (GameUi.Shield >= Kamikadze.healths.get(i)) {
                        GameUi.Shield -= Kamikadze.healths.get(i);
                        SpaceShip.set_color(1, 0, 1, true);
                    } else {
                        GameUi.Health = GameUi.Health - (Kamikadze.healths.get(i) - GameUi.Shield) / 5;
                        GameUi.Shield = 0;
                        SpaceShip.set_color(1, 0, 1, false);
                    }

                    GameUi.Score = (int) (GameUi.Score + Kamikadze.healths.get(i) / 2);

                    GameUi.enemiesKilled++;

                    Kamikadze.removeEnemy(i, true);

                }

                for (int i2 = 0; i2 < Bullet.bullets.size; i2++) {
                    if (enemy.overlaps(Bullet.bullets.get(i2))) {

                        GameUi.Score = (int) (GameUi.Score + 30 + enemy.x / 20);

                        Kamikadze.healths.set(i, Kamikadze.healths.get(i) - Bullet.damages.get(i2));

                        Bullet.removeBullet(i2, true);

                        if (Kamikadze.healths.get(i) <= 0) {

                            Kamikadze.removeEnemy(i, true);

                            GameUi.enemiesKilled++;
                        }
                    }
                }

                for (int i3 = 0; i3 < Meteorite.meteorites.size; i3++) {
                    if (Meteorite.meteorites.get(i3).overlaps(enemy)) {

                        if (Meteorite.healths.get(i3) > Kamikadze.healths.get(i)) {
                            Meteorite.healths.set(i3, Meteorite.healths.get(i3) - Kamikadze.healths.get(i));
                            Kamikadze.removeEnemy(i, true);

                        } else if (Meteorite.healths.get(i3) < Kamikadze.healths.get(i)) {
                            Kamikadze.healths.set(i, Kamikadze.healths.get(i) - Meteorite.healths.get(i3));
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

        for (int i = 0; i < ShotgunEnemy.enemies.size; i++) {

            Rectangle enemy = ShotgunEnemy.enemies.get(i);
            if (!is_paused) {

                if (enemy.overlaps(bounds.getBoundingRectangle())) {

                    if (GameUi.Shield >= ShotgunEnemy.healths.get(i)) {
                        GameUi.Shield -= ShotgunEnemy.healths.get(i);
                        SpaceShip.set_color(1, 0, 1, true);
                    } else {
                        GameUi.Health = GameUi.Health - (ShotgunEnemy.healths.get(i) - GameUi.Shield) / 5;
                        GameUi.Shield = 0;
                        SpaceShip.set_color(1, 0, 1, false);
                    }

                    GameUi.Score = (int) (GameUi.Score + ShotgunEnemy.healths.get(i) / 2);

                    GameUi.enemiesKilled++;

                    ShotgunEnemy.removeEnemy(i, true);

                }

                for (int i2 = 0; i2 < Bullet.bullets.size; i2++) {
                    if (enemy.overlaps(Bullet.bullets.get(i2))) {

                        GameUi.Score = (int) (GameUi.Score + 30 + enemy.x / 20);

                        ShotgunEnemy.healths.set(i, ShotgunEnemy.healths.get(i) - Bullet.damages.get(i2));

                        Bullet.removeBullet(i2, true);

                        ShotgunEnemy.colors.get(i).set(Color.RED);

                        if (ShotgunEnemy.healths.get(i) <= 0) {

                            ShotgunEnemy.removeEnemy(i, true);

                            GameUi.enemiesKilled++;

                        }
                    }
                }

                for (int i3 = 0; i3 < Meteorite.meteorites.size; i3++) {
                    if (Meteorite.meteorites.get(i3).overlaps(enemy)) {

                        if (Meteorite.healths.get(i3) > ShotgunEnemy.healths.get(i)) {
                            Meteorite.healths.set(i3, Meteorite.healths.get(i3) - ShotgunEnemy.healths.get(i));
                            ShotgunEnemy.removeEnemy(i, true);

                        } else if (Meteorite.healths.get(i3) < ShotgunEnemy.healths.get(i)) {
                            ShotgunEnemy.healths.set(i, ShotgunEnemy.healths.get(i) - Meteorite.healths.get(i3));
                            Meteorite.removeMeteorite(i3, true);

                            ShotgunEnemy.colors.get(i).set(Color.RED);

                        } else {
                            ShotgunEnemy.removeEnemy(i, true);
                            Meteorite.removeMeteorite(i3, true);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < SniperEnemy.enemies.size; i++) {

            Rectangle enemy = SniperEnemy.enemies.get(i);

            if (!is_paused) {

                if (enemy.overlaps(bounds.getBoundingRectangle())) {

                    if (GameUi.Shield >= SniperEnemy.healths.get(i)) {
                        GameUi.Shield -= SniperEnemy.healths.get(i);
                        SpaceShip.set_color(1, 0, 1, true);
                    } else {
                        GameUi.Health = GameUi.Health - (SniperEnemy.healths.get(i) - GameUi.Shield) / 5;
                        GameUi.Shield = 0;
                        SpaceShip.set_color(1, 0, 1, false);
                    }

                    GameUi.Score = (int) (GameUi.Score + SniperEnemy.healths.get(i) / 2);

                    GameUi.enemiesKilled++;

                    SniperEnemy.removeEnemy(i, true);

                }

                for (int i2 = 0; i2 < Bullet.bullets.size; i2++) {
                    if (enemy.overlaps(Bullet.bullets.get(i2))) {

                        GameUi.Score = (int) (GameUi.Score + 30 + enemy.x / 20);

                        SniperEnemy.healths.set(i, SniperEnemy.healths.get(i) - Bullet.damages.get(i2));

                        Bullet.removeBullet(i2, true);

                        SniperEnemy.colors.get(i).set(Color.RED);

                        if (SniperEnemy.healths.get(i) <= 0) {

                            SniperEnemy.removeEnemy(i, true);

                            GameUi.enemiesKilled++;

                        }
                    }
                }

                for (int i3 = 0; i3 < Meteorite.meteorites.size; i3++) {
                    if (Meteorite.meteorites.get(i3).overlaps(enemy)) {

                        if (Meteorite.healths.get(i3) > SniperEnemy.healths.get(i)) {
                            Meteorite.healths.set(i3, Meteorite.healths.get(i3) - SniperEnemy.healths.get(i));
                            SniperEnemy.removeEnemy(i, true);

                        } else if (Meteorite.healths.get(i3) < SniperEnemy.healths.get(i)) {
                            SniperEnemy.healths.set(i, SniperEnemy.healths.get(i) - Meteorite.healths.get(i3));
                            Meteorite.removeMeteorite(i3, true);

                            SniperEnemy.colors.get(i).set(Color.RED);

                        } else {
                            SniperEnemy.removeEnemy(i, true);
                            Meteorite.removeMeteorite(i3, true);
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

                    if (GameUi.Shield >= Meteorite.healths.get(i)) {
                        GameUi.Shield -= Meteorite.healths.get(i);
                        SpaceShip.set_color(1, 0, 1, true);
                    } else {
                        GameUi.Health = GameUi.Health - (Meteorite.healths.get(i) - GameUi.Shield) / 5;
                        GameUi.Shield = 0;
                        SpaceShip.set_color(1, 0, 1, false);
                    }

                    GameUi.Score = (int) (GameUi.Score + Meteorite.radiuses.get(i) / 2);

                    Meteorite.removeMeteorite(i, true);

                    Meteorite.meteoritesDestroyed++;

                }

                for (int i2 = 0; i2 < Bullet.bullets.size; i2++) {
                    if (meteorite.overlaps(Bullet.bullets.get(i2))) {

                        GameUi.Score = (int) (GameUi.Score + radius / 2);

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

        for (int i = 0; i < EnemyBullet.bullets.size; i++) {

            Rectangle bullet = EnemyBullet.bullets.get(i);

            if (!is_paused) {

                if (bullet.overlaps(bounds.getBoundingRectangle())) {
                    if (GameUi.Shield >= EnemyBullet.damages.get(i)) {
                        GameUi.Shield -= EnemyBullet.damages.get(i);
                        SpaceShip.set_color(1, 0, 1, true);
                    } else {
                        GameUi.Health = GameUi.Health - (EnemyBullet.damages.get(i) - GameUi.Shield) / 5;
                        GameUi.Shield = 0;
                        SpaceShip.set_color(1, 0, 0, false);
                    }
                    EnemyBullet.removeBullet(i, true);
                }

                for (int i2 = 0; i2 < Meteorite.meteorites.size; i2++) {
                    if (Meteorite.meteorites.get(i2).overlaps(bullet)) {

                        Meteorite.healths.set(i2, Meteorite.healths.get(i2) - EnemyBullet.damages.get(i));

                        EnemyBullet.removeBullet(i, true);

                        if (Meteorite.healths.get(i2) <= 0) {
                            Meteorite.removeMeteorite(i2, true);
                        }
                    }
                }
                for (int i2 = 0; i2 < Bullet.bullets.size; i2++) {

                    if (bullet.overlaps(Bullet.bullets.get(i2))) {
                        EnemyBullet.removeBullet(i, true);
                        Bullet.removeBullet(i2, true);
                        GameUi.Score += 20;
                    }

                }

            }
        }

        for (int i = 0; i < EnemyBullet_shotgun.bullets.size; i++) {

            Rectangle bullet = EnemyBullet_shotgun.bullets.get(i);

            if (!is_paused) {

                if (bullet.overlaps(bounds.getBoundingRectangle())) {
                    if (GameUi.Shield >= EnemyBullet_shotgun.damages.get(i)) {
                        GameUi.Shield -= EnemyBullet_shotgun.damages.get(i);
                        SpaceShip.set_color(1, 0, 1, true);
                    } else {
                        GameUi.Health = GameUi.Health - (EnemyBullet_shotgun.damages.get(i) - GameUi.Shield) / 5;
                        GameUi.Shield = 0;
                        SpaceShip.set_color(1, 0, 0, false);
                    }
                    EnemyBullet_shotgun.removeBullet(i, true);
                }

                for (int i2 = 0; i2 < Meteorite.meteorites.size; i2++) {
                    if (Meteorite.meteorites.get(i2).overlaps(bullet)) {

                        Meteorite.healths.set(i2, Meteorite.healths.get(i2) - EnemyBullet_shotgun.damages.get(i));

                        EnemyBullet_shotgun.removeBullet(i, true);

                        if (Meteorite.healths.get(i2) <= 0) {
                            Meteorite.removeMeteorite(i2, true);
                        }
                    }
                }
                for (int i2 = 0; i2 < Bullet.bullets.size; i2++) {

                    if (bullet.overlaps(Bullet.bullets.get(i2))) {
                        EnemyBullet_shotgun.removeBullet(i, true);
                        Bullet.removeBullet(i2, true);
                        GameUi.Score += 20;
                    }

                }

            }
        }

        for (int i = 0; i < EnemyBullet_sniper.bullets.size; i++) {

            Rectangle bullet = EnemyBullet_sniper.bullets.get(i);

            if (!is_paused) {

                if (bullet.overlaps(bounds.getBoundingRectangle())) {
                    if (GameUi.Shield >= EnemyBullet_sniper.damages.get(i)) {
                        GameUi.Shield -= EnemyBullet_sniper.damages.get(i);
                        SpaceShip.set_color(1, 0, 1, true);
                    } else {
                        GameUi.Health = GameUi.Health - (EnemyBullet_sniper.damages.get(i) - GameUi.Shield) / 5;
                        GameUi.Shield = 0;
                        SpaceShip.set_color(1, 0, 0, false);
                    }
                    EnemyBullet_sniper.removeBullet(i, true);
                }

                for (int i2 = 0; i2 < Meteorite.meteorites.size; i2++) {
                    if (Meteorite.meteorites.get(i2).overlaps(bullet)) {

                        Meteorite.healths.set(i2, Meteorite.healths.get(i2) - EnemyBullet_sniper.damages.get(i));

                        EnemyBullet_sniper.removeBullet(i, true);

                        if (Meteorite.healths.get(i2) <= 0) {
                            Meteorite.removeMeteorite(i2, true);

                        }
                    }
                }
                for (int i2 = 0; i2 < Bullet.bullets.size; i2++) {

                    if (bullet.overlaps(Bullet.bullets.get(i2))) {
                        EnemyBullet_sniper.removeBullet(i, true);
                        Bullet.removeBullet(i2, true);
                        GameUi.Score += 1500;
                    }

                }

            }

        }

    }
}
