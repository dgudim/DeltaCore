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
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Drops;
import com.deo.flapd.model.UraniumCell;

import java.util.Random;

import static com.deo.flapd.utils.DUtils.getFloat;

public class Kamikadze {

    public static Array<Rectangle> enemies;
    public static Array<Integer> healths;
    private Array<ParticleEffect> fires;
    private Array<ParticleEffect> explosions;
    private Array<Float> timers;
    private Array<Float> timers2;
    private Sprite enemy;
    private float millis;
    private Random random;
    private Polygon bounds;

    private Sound explosion;

    private float soundVolume;

    private float width, height;

    private static Array<Boolean> explosionQueue, remove_Enemy;

    public Kamikadze(AssetManager assetManager, float width, float height, Polygon shipBounds, boolean easterEgg) {
        bounds = shipBounds;
        this.height = height;
        this.width = width;
        enemies = new Array<>();
        healths = new Array<>();
        random = new Random();
        timers = new Array<>();
        timers2 = new Array<>();

        if (easterEgg) {
            enemy = new Sprite((Texture) assetManager.get("cat_bomb.png"));
            explosion = Gdx.audio.newSound(Gdx.files.internal("sfx/hitcat.ogg"));
        } else {
            enemy = new Sprite((Texture) assetManager.get("atomic_bomb.png"));
            explosion = Gdx.audio.newSound(Gdx.files.internal("sfx/explosion.ogg"));
        }

        explosions = new Array<>();
        fires = new Array<>();
        explosionQueue = new Array<>();
        remove_Enemy = new Array<>();

        soundVolume = getFloat("soundVolume");

        enemy.setSize(0, 0);
        enemy.setPosition(1000, 1000);
    }

    public void Spawn(int health, float scale, float explosionTimer) {
        if (millis > 10) {

            Rectangle enemy = new Rectangle();

            enemy.x = 800;
            enemy.y = random.nextInt(400) + 40;

            enemy.setSize(width * scale, height * scale);

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
            GameLogic.enemiesSpawned++;
        }
    }

    public void drawEffects(SpriteBatch batch, float delta) {
        for (int i = 0; i < enemies.size; i++) {

            Rectangle enemy = enemies.get(i);
            ParticleEffect fire = fires.get(i);
            float timer = timers.get(i);

            timer = timer - delta;
            timers.set(i, timer);

            if (timer < 1) {
                removeEnemy(i, true);
            }

            fire.setPosition(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2);
            fire.draw(batch);
            fire.update(delta);
        }

        for (int i3 = 0; i3 < explosions.size; i3++) {
            explosions.get(i3).draw(batch);
            explosions.get(i3).update(delta);
            if (explosions.get(i3).isComplete()) {
                explosions.get(i3).dispose();
                explosions.removeIndex(i3);
            }
        }
    }

    public void drawBase(SpriteBatch batch, float delta) {

        millis = millis + 50 * delta;

        for (int i = 0; i < enemies.size; i++) {

            Rectangle enemy = enemies.get(i);
            float timer = timers.get(i);
            float ideal_timer = timers2.get(i);

            timer = timer - delta;
            timers.set(i, timer);

            if (timer < 1) {
                removeEnemy(i, true);
            }

            Vector2 pos1 = new Vector2();
            pos1.set(enemy.x, enemy.y);
            Vector2 pos2 = new Vector2();
            pos2.set(bounds.getX(), bounds.getY());
            pos1.lerp(pos2, delta);

            this.enemy.setColor(1, timer / ideal_timer, timer / ideal_timer, 1);
            this.enemy.setPosition(pos1.x, pos1.y);
            this.enemy.setSize(enemy.width, enemy.height);
            this.enemy.setOrigin(enemy.width / 2f, enemy.height / 2f);
            this.enemy.setRotation(MathUtils.radiansToDegrees * MathUtils.atan2(enemy.y - bounds.getY(), enemy.x - bounds.getX()));

            this.enemy.draw(batch);

            enemy.x = pos1.x;
            enemy.y = pos1.y;

        }

        for (int i4 = 0; i4 < enemies.size; i4++) {
            if (explosionQueue.get(i4)) {
                ParticleEffect explosionEffect = new ParticleEffect();
                explosionEffect.load(Gdx.files.internal("particles/explosion.p"), Gdx.files.internal("particles"));
                explosionEffect.setPosition(enemies.get(i4).x + enemies.get(i4).width / 2, enemies.get(i4).y + enemies.get(i4).height / 2);
                explosionEffect.start();
                if (random.nextBoolean()) {
                    Bonus.Spawn(random.nextInt(4) + 1, 1, enemies.get(i4));
                }
                Drops.drop(enemies.get(i4), 1, 2, 3);
                UraniumCell.Spawn(enemies.get(i4), random.nextInt(5) + 1, 1, 2);
                explosions.add(explosionEffect);
                explosionQueue.removeIndex(i4);
                enemies.removeIndex(i4);
                healths.removeIndex(i4);
                timers.removeIndex(i4);
                timers2.removeIndex(i4);
                fires.get(i4).dispose();
                fires.removeIndex(i4);
                remove_Enemy.removeIndex(i4);
                if (soundVolume > 0) {
                    explosion.play(soundVolume / 100);
                }
            } else if (remove_Enemy.get(i4)) {
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

    public void dispose() {
        explosion.dispose();
        for (int i3 = 0; i3 < explosions.size; i3++) {
            explosions.get(i3).dispose();
            explosions.removeIndex(i3);
        }
        for (int i3 = 0; i3 < fires.size; i3++) {
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

    public static void removeEnemy(int i, boolean explode) {
        if (explode) {
            explosionQueue.set(i, true);
            remove_Enemy.set(i, true);
        } else {
            explosionQueue.set(i, false);
            remove_Enemy.set(i, true);
        }
    }
}
