package com.deo.flapd.model;

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

public class Meteorite {

    private Polygon bounds;
    public static Array<Rectangle> meteorites;
    public static Array<Float> radiuses;
    public static Array<Float> degrees;
    public static Array<Float> healths;
    private static Array <ParticleEffect> explosions;
    private static Array <ParticleEffect> fires;
    private Sprite meteorite;

    private Sound explosion;

    private boolean sound;

    public static int meteoritesDestroyed;

    private Bonus bonus;
    private Random random;

    public Meteorite(AssetManager assetManager, Polygon shipBounds) {
        bounds = shipBounds;
        meteorite = new Sprite((Texture)assetManager.get("Meteo.png"));
        meteorites = new Array<>();
        radiuses = new Array<>();
        degrees = new Array<>();
        healths = new Array<>();

        explosions = new Array<>();
        fires = new Array<>();

        meteoritesDestroyed = 0;
        random = new Random();
        sound = MenuScreen.Sound;
        explosion = Gdx.audio.newSound(Gdx.files.internal("music/explosion.ogg"));

        bonus = new Bonus(assetManager, 50, 50, shipBounds);
    }

    public void Spawn(float x, float degree, float radius) {

            Rectangle meteorite = new Rectangle();
            meteorite.x = x;
            meteorite.y = 480;
            meteorite.setSize(radius*2, radius*2);
            meteorites.add(meteorite);
            radiuses.add(radius);
            degrees.add(degree);
            healths.add(radius*1.5f);

            ParticleEffect fire = new ParticleEffect();
            fire.load(Gdx.files.internal("particles/particle_nowind.p"), Gdx.files.internal("particles"));
            fire.start();

            fires.add(fire);
    }

    public void draw(SpriteBatch batch, boolean is_paused) {

        for (int i = 0; i < meteorites.size; i ++) {

            Rectangle meteorite = meteorites.get(i);
            ParticleEffect fire = fires.get(i);

            fire.setPosition(meteorite.x + meteorite.width/2, meteorite.y + meteorite.width/2);
            fire.draw(batch);
            if(!is_paused){
                fire.update(Gdx.graphics.getDeltaTime());
            }else{
                fire.update(0);
            }

            this.meteorite.setPosition(meteorite.x, meteorite.y);
            this.meteorite.setOrigin(radiuses.get(i), radiuses.get(i));
            this.meteorite.setSize(radiuses.get(i) * 2, radiuses.get(i) * 2);
            this.meteorite.draw(batch);

            if (!is_paused){
                meteorite.x += 130 * degrees.get(i) * Gdx.graphics.getDeltaTime();
                meteorite.y -= 130 * Gdx.graphics.getDeltaTime();
                this.meteorite.setRotation(this.meteorite.getRotation() + 1.5f / meteorites.size);

            if (meteorite.y < -radiuses.get(i) * 4 || meteorite.x > 1000 || meteorite.x < 0 - radiuses.get(i) * 4) {
                removeMeteorite(i, false);
            }

            if (meteorite.overlaps(bounds.getBoundingRectangle())) {

                if (GameUi.Shield >= healths.get(i)) {
                    GameUi.Shield -= healths.get(i);
                    SpaceShip.set_color(1, 0, 1, true);
                } else {
                    GameUi.Health = GameUi.Health - (healths.get(i) - GameUi.Shield) / 5;
                    GameUi.Shield = 0;
                    SpaceShip.set_color(1, 0, 1, false);
                }

                GameUi.Score = (int)(GameUi.Score + radiuses.get(i) / 2);

                if(random.nextBoolean()) {
                    bonus.Spawn((int) (random.nextFloat() + 0.4) + 1, 1, meteorite);
                }

                removeMeteorite(i, true);

                meteoritesDestroyed++;

                if(sound) {
                    explosion.play(MenuScreen.SoundVolume/100);
                }

            }

            for (int i2 = 0; i2 < Bullet.bullets.size; i2++) {
                if (meteorite.overlaps(Bullet.bullets.get(i2))) {

                    GameUi.Score = (int) (GameUi.Score + radiuses.get(i) / 2);

                    healths.set(i, healths.get(i) - Bullet.damages.get(i2));

                    Bullet.removeBullet(i2, true);

                    if(healths.get(i) <= 0) {

                        if(random.nextBoolean()) {
                            bonus.Spawn((int) (random.nextFloat() + 0.4) + 1, 1, meteorite);
                        }

                        removeMeteorite(i, true);

                        meteoritesDestroyed++;

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
        bonus.draw(batch, is_paused);
    }

    public void dispose(){
        explosion.dispose();
        for(int i3 = 0; i3 < explosions.size; i3 ++){
            explosions.get(i3).dispose();
            explosions.removeIndex(i3);
        }
        for(int i2 = 0; i2 < fires.size; i2 ++){
            fires.get(i2).dispose();
            fires.removeIndex(i2);
        }
        for(int i3 = 0; i3 < meteorites.size; i3 ++){
            meteorites.removeIndex(i3);
            degrees.removeIndex(i3);
            healths.removeIndex(i3);
            radiuses.removeIndex(i3);
        }
        bonus.dispose();
    }

    public static void removeMeteorite(int i, boolean explode){
        if(explode){
            ParticleEffect explosionEffect = new ParticleEffect();
            explosionEffect.load(Gdx.files.internal("particles/explosion.p"), Gdx.files.internal("particles"));
            explosionEffect.setPosition(meteorites.get(i).x + meteorites.get(i).width/2, meteorites.get(i).y + meteorites.get(i).height/2);
            explosionEffect.start();
            explosions.add(explosionEffect);
        }
        meteorites.removeIndex(i);
        degrees.removeIndex(i);
        radiuses.removeIndex(i);
        healths.removeIndex(i);
        fires.get(i).dispose();
        fires.removeIndex(i);
    }

}


