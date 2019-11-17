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
import com.deo.flapd.model.UraniumCell;
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

    private float width, height;

    private Bonus bonus;

    private static Array<Boolean> explosionQueue, remove_Enemy;

    private UraniumCell uraniumCell;

    public Kamikadze(UraniumCell uraniumCell, AssetManager assetManager, float width, float height, Polygon shipBounds, Bonus bonus) {
        bounds = shipBounds;
        this.height = height;
        this.width = width;
        enemy = new Sprite((Texture)assetManager.get("atomic_bomb.png"));
        enemies = new Array<>();
        healths = new Array<>();
        random = new Random();
        timers = new Array<>();
        timers2 = new Array<>();

        explosions = new Array<>();
        fires = new Array<>();
        explosionQueue = new Array<>();
        remove_Enemy = new Array<>();

        sound = MenuScreen.Sound;
        explosion = Gdx.audio.newSound(Gdx.files.internal("music/explosion.ogg"));

        this.bonus = bonus;
        this.uraniumCell = uraniumCell;
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
            explosionQueue.add(false);
            remove_Enemy.add(false);

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

        for(int i4 = 0; i4 < enemies.size; i4++){
            if(explosionQueue.get(i4)) {
                ParticleEffect explosionEffect = new ParticleEffect();
                explosionEffect.load(Gdx.files.internal("particles/explosion.p"), Gdx.files.internal("particles"));
                explosionEffect.setPosition(enemies.get(i4).x + enemies.get(i4).width / 2, enemies.get(i4).y + enemies.get(i4).height / 2);
                explosionEffect.start();
                if (random.nextBoolean()) {
                    bonus.Spawn((int) (random.nextFloat() + 0.4) + 3, 1, enemies.get(i4));
                }
                uraniumCell.Spawn(enemies.get(i4), random.nextInt(5)+1, 1, 2);
                explosions.add(explosionEffect);
                explosionQueue.removeIndex(i4);
                enemies.removeIndex(i4);
                healths.removeIndex(i4);
                timers.removeIndex(i4);
                timers2.removeIndex(i4);
                fires.get(i4).dispose();
                fires.removeIndex(i4);
                remove_Enemy.removeIndex(i4);
                if(sound) {
                    explosion.play(MenuScreen.SoundVolume/100);
                }
            }else if (remove_Enemy.get(i4)){
                explosionQueue.removeIndex(i4);
                enemies.removeIndex(i4);
                healths.removeIndex(i4);
                timers.removeIndex(i4);
                timers2.removeIndex(i4);
                fires.get(i4).dispose();
                fires.removeIndex(i4);
                remove_Enemy.removeIndex(i4);
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
        enemies.clear();
        timers.clear();
        timers2.clear();
        healths.clear();
        explosionQueue.clear();
        remove_Enemy.clear();
    }

    public static void removeEnemy(int i, boolean explode){
        if(explode){
            explosionQueue.set(i, true);
            remove_Enemy.set(i, true);
        }else{
            explosionQueue.set(i, false);
            remove_Enemy.set(i, true);
        }
    }
}
