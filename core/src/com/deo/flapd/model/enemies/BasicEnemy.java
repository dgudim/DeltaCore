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
import com.deo.flapd.view.GameUi;
import com.deo.flapd.view.MenuScreen;

import java.util.Random;

public class BasicEnemy {

    public static Array <Rectangle> enemies;
    public static Array <Float> healths;
    private static Array <ParticleEffect> fires;
    private static Array <ParticleEffect> explosions;
    private static Array <Float> scales;
    private Sprite enemy;
    private float millis;
    private Random random;
    private EnemyBullet enemyBullet;

    private Sound explosion;

    private boolean sound;

    private float width, height, fire_x, fire_y;

    private static Array<Boolean> explosionQueue, remove_Enemy;

    public BasicEnemy(AssetManager assetManager, float width, float height, float Bwidth, float Bheight, float Boffset_x, float Boffset_y, float Bspread, float fire_offset_x, float fire_offset_y) {
        this.height = height;
        this.width = width;
        this.fire_x = fire_offset_x;
        this.fire_y = fire_offset_y;
        enemyBullet = new EnemyBullet((Texture) assetManager.get("pew2.png"), Bwidth, Bheight, Boffset_x, Boffset_y, Bspread);
        enemy = new Sprite((Texture) assetManager.get("trainingbot.png"));
        enemies = new Array<>();
        healths = new Array<>();
        random = new Random();
        scales = new Array<>();

        explosions = new Array<>();
        fires = new Array<>();
        explosionQueue = new Array<>();
        remove_Enemy = new Array<>();

        sound = MenuScreen.Sound;
        explosion = Gdx.audio.newSound(Gdx.files.internal("music/explosion.ogg"));
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
            explosionQueue.add(false);
            remove_Enemy.add(false);

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

            if(!is_paused) {
                enemy.x -= 110 * Gdx.graphics.getDeltaTime();
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
                explosions.add(explosionEffect);
                explosionQueue.removeIndex(i4);
                enemies.removeIndex(i4);
                healths.removeIndex(i4);
                scales.removeIndex(i4);
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
                scales.removeIndex(i4);
                fires.get(i4).dispose();
                fires.removeIndex(i4);
                remove_Enemy.removeIndex(i4);
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
        enemies.clear();
        scales.clear();
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

    public void shoot(int i){
        enemyBullet.Spawn(10, enemies.get(i), 0.5f);
    }
}
