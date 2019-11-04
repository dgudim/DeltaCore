package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Meteorite;
import com.deo.flapd.model.SpaceShip;
import com.deo.flapd.view.GameUi;
import com.deo.flapd.view.MenuScreen;

import java.util.Random;

public class SniperEnemy {

    public static Array <Rectangle> enemies;
    public static Array <Float> healths;
    private static Array <ParticleEffect> fires;
    private static Array <ParticleEffect> explosions;
    private static Array <Float> scales;
    private Sprite enemy;
    private float millis;
    private Random random;
    private Polygon bounds;
    private EnemyBullet_sniper enemyBullet;

    private Sound explosion;

    private boolean sound;

    private float width, height, fire_x, fire_y;

    private Bonus bonus;

    public SniperEnemy(AssetManager assetManager, float width, float height, float Bwidth, float Bheight, float Boffset_x, float Boffset_y, float Bspread, float fire_offset_x, float fire_offset_y, Polygon shipBounds) {
        bounds = shipBounds;
        this.height = height;
        this.width = width;
        this.fire_x = fire_offset_x;
        this.fire_y = fire_offset_y;
        enemyBullet = new EnemyBullet_sniper((Texture)assetManager.get("pew.png"), shipBounds, Bwidth, Bheight, Boffset_x, Boffset_y, Bspread);
        enemy = new Sprite((Texture)assetManager.get("enemy_sniper.png"));
        enemies = new Array<>();
        healths = new Array<>();
        random = new Random();
        scales = new Array<>();

        explosions = new Array<>();
        fires = new Array<>();

        sound = MenuScreen.Sound;
        explosion = Gdx.audio.newSound(Gdx.files.internal("music/explosion.ogg"));

        bonus = new Bonus(assetManager, 50, 50, shipBounds);
    }

    public void Spawn(float health, float scale) {
        if(millis > 10) {

            Rectangle enemy = new Rectangle();

            enemy.x = 800;
            enemy.y = random.nextInt(400)+20;

            enemy.setSize(width*scale, height*scale);

            enemies.add(enemy);
            healths.add(health);
            scales.add(scale);

            ParticleEffect fire = new ParticleEffect();
            fire.load(Gdx.files.internal("particles/fire_engine_left_blue.p"), Gdx.files.internal("particles"));
            fire.start();

            fires.add(fire);

            millis = 0;
            GameUi.enemiesSpawned++;
        }
        millis = millis + 50 * Gdx.graphics.getDeltaTime();
    }

    public void draw(SpriteBatch batch, boolean is_paused) {

        enemyBullet.draw(batch, is_paused);

        for (int i = 0; i < enemies.size; i ++) {

            Rectangle enemy = enemies.get(i);
            ParticleEffect fire = fires.get(i);
            float scale = scales.get(i);

            this.enemy.setPosition(enemy.x, enemy.y);
            this.enemy.setSize(enemy.width, enemy.height);
            this.enemy.setOrigin(enemy.width / 2f, enemy.height / 2f);

            fire.setPosition(enemy.x + fire_x * scale, enemy.y + fire_y * scale);
            fire.draw(batch);
            if(!is_paused) {
                fire.update(Gdx.graphics.getDeltaTime());
            }else{
                fire.update(0);
            }

            this.enemy.draw(batch);

            if(enemy.x < -enemy.width-110){
                removeEnemy(i, false);
            }

            if (!is_paused) {
                enemy.x -= 70 * Gdx.graphics.getDeltaTime();

                if(enemy.overlaps(bounds.getBoundingRectangle())){

                    if (GameUi.Shield >= healths.get(i)) {
                        GameUi.Shield -= healths.get(i);
                        SpaceShip.set_color(1, 0, 1, true);
                    } else {
                        GameUi.Health = GameUi.Health - (healths.get(i) - GameUi.Shield) / 5;
                        GameUi.Shield = 0;
                        SpaceShip.set_color(1, 0, 1, false);
                    }

                    GameUi.Score = (int)(GameUi.Score + healths.get(i)/2);

                    GameUi.enemiesKilled++;

                    if(random.nextBoolean()) {
                        bonus.Spawn((int) (random.nextFloat() + 0.4) + 1, 1, enemy);
                    }

                    removeEnemy(i, true);

                    if(sound) {
                        explosion.play(MenuScreen.SoundVolume/100);
                    }

                }

                for (int i2 = 0; i2 < Bullet.bullets.size; i2++) {
                    if (enemy.overlaps(Bullet.bullets.get(i2))) {

                        GameUi.Score = (int)(GameUi.Score + 30 + enemy.x / 20);

                        healths.set(i, healths.get(i) - Bullet.damages.get(i2));

                        Bullet.removeBullet(i2, true);

                        if(healths.get(i) <= 0) {

                            if(random.nextBoolean()) {
                                bonus.Spawn((int) (random.nextFloat() + 0.4) + 1, 1, enemy);
                            }

                            removeEnemy(i, true);

                            GameUi.enemiesKilled++;

                            if (sound){
                                explosion.play(MenuScreen.SoundVolume/100);
                            }
                        }
                    }
                }

                for (int i3 = 0; i3 < Meteorite.meteorites.size; i3++) {
                    if (Meteorite.meteorites.get(i3).overlaps(enemy)) {

                        if(Meteorite.healths.get(i3) > healths.get(i)){
                            Meteorite.healths.set(i3, Meteorite.healths.get(i3) - healths.get(i));
                            removeEnemy(i, true);

                            if(sound) {
                                explosion.play(MenuScreen.SoundVolume/100);
                            }

                        }else if(Meteorite.healths.get(i3) < healths.get(i)){
                            healths.set(i, healths.get(i) - Meteorite.healths.get(i3));
                            Meteorite.removeMeteorite(i3, true);

                            Meteorite.meteoritesDestroyed++;

                            if(sound) {
                                explosion.play(MenuScreen.SoundVolume/100);
                            }

                        }else{
                            removeEnemy(i, true);
                            Meteorite.removeMeteorite(i3, true);

                            Meteorite.meteoritesDestroyed++;

                            if(sound) {
                                explosion.play(MenuScreen.SoundVolume/100);
                            }
                        }
                    }
                }
            }
        }
        for(int i3 = 0; i3 < explosions.size; i3 ++){
            explosions.get(i3).draw(batch);
            if(!is_paused) {
                explosions.get(i3).update(Gdx.graphics.getDeltaTime());
            }else{
                explosions.get(i3).update(0);
            }
            if(explosions.get(i3).isComplete()){
                explosions.get(i3).dispose();
                explosions.removeIndex(i3);
            }
        }
    }

    public void dispose(){
        explosion.dispose();
        enemyBullet.dispose();
        for(int i3 = 0; i3 < explosions.size; i3 ++){
            explosions.get(i3).dispose();
            explosions.removeIndex(i3);
        }
        for(int i3 = 0; i3 < fires.size; i3 ++){
            fires.get(i3).dispose();
            fires.removeIndex(i3);
        }
        for(int i3 = 0; i3 < enemies.size; i3 ++){
            enemies.removeIndex(i3);
            scales.removeIndex(i3);
            healths.removeIndex(i3);
        }
        bonus.dispose();
    }

    public static void removeEnemy(int i, boolean explode){
        if(explode){
            ParticleEffect explosionEffect = new ParticleEffect();
            explosionEffect.load(Gdx.files.internal("particles/explosion.p"), Gdx.files.internal("particles"));
            explosionEffect.setPosition(enemies.get(i).x + enemies.get(i).width/2, enemies.get(i).y + enemies.get(i).height/2);
            explosionEffect.start();
            explosions.add(explosionEffect);
        }
        enemies.removeIndex(i);
        healths.removeIndex(i);
        fires.get(i).dispose();
        fires.removeIndex(i);
        scales.removeIndex(i);
    }

    public void shoot(int i){
        enemyBullet.Spawn(50, enemies.get(i), 0.5f);
    }
}
