package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Meteorite;
import com.deo.flapd.model.SpaceShip;
import com.deo.flapd.view.GameUi;
import com.deo.flapd.view.MenuScreen;

import java.util.Random;

public class Kamikadze {

    public static Array <Rectangle> enemies;
    public static Array <Float> healths;
    private static Array <ParticleEffect> fires;
    private static Array <ParticleEffect> explosions;
    private static Array <Float> timers;
    private static Array <Float> timers2;
    private Sprite enemy;
    private float millis;
    private Random random;
    private Polygon bounds;

    private Sound explosion;

    private boolean sound;

    private float width, height, fire_x, fire_y;

    private Bonus bonus;

    public Kamikadze(AssetManager assetManager, float width, float height, float fire_offset_x, float fire_offset_y, Polygon shipBounds) {
        bounds = shipBounds;
        this.height = height;
        this.width = width;
        this.fire_x = fire_offset_x;
        this.fire_y = fire_offset_y;
        enemy = new Sprite((Texture)assetManager.get("atomic_bomb.png"));
        enemies = new Array<>();
        healths = new Array<>();
        random = new Random();
        timers = new Array<>();
        timers2 = new Array<>();

        explosions = new Array<>();
        fires = new Array<>();

        sound = MenuScreen.Sound;
        explosion = Gdx.audio.newSound(Gdx.files.internal("music/explosion.ogg"));

        bonus = new Bonus(assetManager, 50, 50, shipBounds);
    }

    public void Spawn(float health, float scale, float explosionTimer) {
        if(millis > 10) {

            Rectangle enemy = new Rectangle();

            enemy.x = 800;
            enemy.y = random.nextInt(400)+40;

            enemy.setSize(width*scale, height*scale);

            enemies.add(enemy);
            healths.add(health);
            timers.add(explosionTimer);
            timers2.add(explosionTimer);

            ParticleEffect fire = new ParticleEffect();
            fire.load(Gdx.files.internal("particles/particle_nowind2.p"), Gdx.files.internal("particles"));
            fire.start();

            fires.add(fire);

            millis = 0;
            GameUi.enemiesSpawned++;
        }
        millis = millis + 50 * Gdx.graphics.getDeltaTime();
    }

    public void draw(SpriteBatch batch, boolean is_paused) {

        for (int i = 0; i < enemies.size; i ++) {

            Rectangle enemy = enemies.get(i);
            ParticleEffect fire = fires.get(i);
            float timer = timers.get(i);
            float ideal_timer = timers2.get(i);

            if(!is_paused) {
                timer = timer - 1*Gdx.graphics.getDeltaTime();
                timers.set(i, timer);
            }

            if (timer < ideal_timer/10) {
                removeEnemy(i, true);
            }

            Vector2 pos1 = new Vector2();
            pos1.set(enemy.x, enemy.y);
            Vector2 pos2 = new Vector2();
            pos2.set(bounds.getX(), bounds.getY());
            pos1.lerp(pos2, 0.01f);

            this.enemy.setColor(1, timer/ideal_timer,timer/ideal_timer, 1);
            this.enemy.setPosition(pos1.x, pos1.y);
            this.enemy.setSize(enemy.width, enemy.height);
            this.enemy.setOrigin(enemy.width / 2f, enemy.height / 2f);
            this.enemy.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(enemy.y-bounds.getY(), enemy.x-bounds.getX()));

            fire.setPosition(enemy.x + enemy.width/2, enemy.y + enemy.height/2);
            fire.draw(batch);
            if(!is_paused) {
                fire.update(Gdx.graphics.getDeltaTime());
            }else{
                fire.update(0);
            }

            this.enemy.draw(batch);

            if (!is_paused) {
                enemy.x = pos1.x;
                enemy.y = pos1.y;

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
                        bonus.Spawn((int) (random.nextFloat() + 0.4) + 3, 1, enemy);
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
                                bonus.Spawn((int) (random.nextFloat() + 0.4) + 3, 1, enemy);
                            }

                            removeEnemy(i, true);

                            GameUi.enemiesKilled++;

                            if (sound){
                                explosion.play(MenuScreen.SoundVolume/100);
                            }
                            break;
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
                            break;

                        }else if(Meteorite.healths.get(i3) < healths.get(i)){
                            healths.set(i, healths.get(i) - Meteorite.healths.get(i3));
                            Meteorite.removeMeteorite(i, true);

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
                            break;
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
            timers.removeIndex(i3);
            timers2.removeIndex(i3);
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
        timers.removeIndex(i);
        timers2.removeIndex(i);
    }
}
