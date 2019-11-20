package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.UraniumCell;
import com.deo.flapd.model.bullets.EnemyBullet_shotgun;
import com.deo.flapd.view.GameUi;
import com.deo.flapd.view.MenuScreen;

import java.util.Random;

public class ShotgunEnemy {

    public static Array <Rectangle> enemies;
    public static Array <Float> healths;
    private Array <ParticleEffect> fires;
    private Array <ParticleEffect> explosions;
    private Array <Float> scales;
    public static Array <Color> colors;
    private Sprite enemy;
    private float millis;
    private Random random;
    private EnemyBullet_shotgun enemyBullet;

    private Sound explosion;

    private boolean sound;

    private float width, height, fire_x, fire_y;

    private Bonus bonus;

    private static Array<Boolean> explosionQueue, remove_Enemy;

    private UraniumCell uraniumCell;

    public ShotgunEnemy(UraniumCell uraniumCell, AssetManager assetManager, float width, float height, float Bwidth, float Bheight, float Boffset_x, float Boffset_y, float Bspread, float fire_offset_x, float fire_offset_y, Bonus bonus) {
        this.height = height;
        this.width = width;
        this.fire_x = fire_offset_x;
        this.fire_y = fire_offset_y;
        enemyBullet = new EnemyBullet_shotgun(assetManager, Bwidth, Bheight, Boffset_x, Boffset_y, Bspread);
        enemy = new Sprite((Texture)assetManager.get("enemy_shotgun.png"));

        enemies = new Array<>();
        healths = new Array<>();
        random = new Random();
        scales = new Array<>();

        explosions = new Array<>();
        fires = new Array<>();
        explosionQueue = new Array<>();
        remove_Enemy = new Array<>();

        colors = new Array<>();

        sound = MenuScreen.Sound;
        explosion = Gdx.audio.newSound(Gdx.files.internal("music/explosion.ogg"));

        this.bonus = bonus;
        this.uraniumCell = uraniumCell;
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

            Color color = new Color(Color.WHITE);
            colors.add(color);

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
            Color color = colors.get(i);

            this.enemy.setPosition(enemy.x, enemy.y);
            this.enemy.setSize(enemy.width, enemy.height);
            this.enemy.setOrigin(enemy.width / 2f, enemy.height / 2f);
            this.enemy.setColor(color);

            fire.setPosition(enemy.x + fire_x * scale, enemy.y + fire_y * scale);
            fire.draw(batch);
            if(!is_paused) {
                fire.update(Gdx.graphics.getDeltaTime());
            }else{
                fire.update(0);
            }

            this.enemy.draw(batch);

            if (!is_paused) {
                enemy.x -= 70 * Gdx.graphics.getDeltaTime();

                if (enemy.x < -enemy.width - 110) {
                    ShotgunEnemy.removeEnemy(i, false);
                }

                if(color.g < 1){
                    color.g = (float) MathUtils.clamp(color.g + 0.07, 0, 1);
                }

                if(color.b < 1){
                    color.b = (float)MathUtils.clamp(color.b + 0.07, 0, 1);
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
        for(int i4 = 0; i4 < enemies.size; i4++){
            if(explosionQueue.get(i4)) {
                ParticleEffect explosionEffect = new ParticleEffect();
                explosionEffect.load(Gdx.files.internal("particles/explosion.p"), Gdx.files.internal("particles"));
                explosionEffect.setPosition(enemies.get(i4).x + enemies.get(i4).width / 2, enemies.get(i4).y + enemies.get(i4).height / 2);
                explosionEffect.start();
                if(random.nextBoolean()) {
                    bonus.Spawn((int) (random.nextFloat() + 0.4) + 1, 1, enemies.get(i4));
                }
                uraniumCell.Spawn(enemies.get(i4), random.nextInt(4)+1, 1, 2);
                explosions.add(explosionEffect);
                explosionQueue.removeIndex(i4);
                enemies.removeIndex(i4);
                healths.removeIndex(i4);
                scales.removeIndex(i4);
                fires.get(i4).dispose();
                fires.removeIndex(i4);
                remove_Enemy.removeIndex(i4);
                colors.removeIndex(i4);
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
                colors.removeIndex(i4);
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
        colors.clear();
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
        enemyBullet.Spawn(7, enemies.get(i), 0.5f);
    }
}
