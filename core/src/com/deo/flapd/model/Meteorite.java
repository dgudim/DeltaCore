package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.view.MenuScreen;

import java.util.Random;

public class Meteorite {

    public static Array<Rectangle> meteorites;
    public static Array<Float> radiuses;
    private Array<Float> degrees;
    public static Array<Float> healths;
    private Array <ParticleEffect> explosions;
    private Array <ParticleEffect> fires;
    private Sprite meteorite;

    private Sound explosion;

    private boolean sound;

    public static int meteoritesDestroyed;

    private Bonus bonus;
    private Random random;

    private static Array<Boolean> explosionQueue, remove_Meteorite;

    private UraniumCell uraniumCell;

    private Preferences prefs;

    public Meteorite(UraniumCell uraniumCell, AssetManager assetManager, Bonus bonus, boolean newGame) {
        meteorite = new Sprite((Texture)assetManager.get("Meteo.png"));

        prefs = Gdx.app.getPreferences("Preferences");

        meteorites = new Array<>();
        radiuses = new Array<>();
        degrees = new Array<>();
        healths = new Array<>();

        explosions = new Array<>();
        fires = new Array<>();
        explosionQueue = new Array<>();
        remove_Meteorite = new Array<>();

        if(!newGame){
            meteoritesDestroyed = prefs.getInteger("meteoritesDestroyed");
        }else {
            meteoritesDestroyed = 0;
        }
            random = new Random();
            sound = MenuScreen.Sound;
            explosion = Gdx.audio.newSound(Gdx.files.internal("music/explosion.ogg"));

        this.bonus = bonus;

        this.uraniumCell = uraniumCell;
    }

    public void Spawn(float x, float degree, float radius) {

            Rectangle meteorite = new Rectangle();
            meteorite.x = x;
            meteorite.y = 480;
            meteorite.setSize(radius*2, radius*2);
            meteorites.add(meteorite);
            radiuses.add(radius);
            degrees.add(degree);
            healths.add(radius*3);

            ParticleEffect fire = new ParticleEffect();
            fire.load(Gdx.files.internal("particles/particle_nowind.p"), Gdx.files.internal("particles"));
            fire.start();

            fires.add(fire);
            explosionQueue.add(false);
            remove_Meteorite.add(false);
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
                    Meteorite.removeMeteorite(i, false);
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
        for(int i4 = 0; i4 < meteorites.size; i4++){
            if(explosionQueue.get(i4)) {
                ParticleEffect explosionEffect = new ParticleEffect();
                explosionEffect.load(Gdx.files.internal("particles/explosion.p"), Gdx.files.internal("particles"));
                explosionEffect.setPosition(meteorites.get(i4).x + meteorites.get(i4).width / 2, meteorites.get(i4).y + meteorites.get(i4).height / 2);
                explosionEffect.start();
                if(random.nextBoolean()) {
                    bonus.Spawn((int) (random.nextFloat() + 0.4) + 2, 1, meteorites.get(i4));
                    if(random.nextBoolean() && random.nextFloat()>0.5) {
                        bonus.Spawn(5, 1, meteorites.get(i4));
                    }
                }
                uraniumCell.Spawn(meteorites.get(i4), random.nextInt((int)(radiuses.get(i4)/17.5f))+2, 1, 2);
                explosions.add(explosionEffect);
                explosionQueue.removeIndex(i4);
                meteorites.removeIndex(i4);
                healths.removeIndex(i4);
                radiuses.removeIndex(i4);
                fires.get(i4).dispose();
                fires.removeIndex(i4);
                remove_Meteorite.removeIndex(i4);
                degrees.removeIndex(i4);
                if(sound) {
                    explosion.play(MenuScreen.SoundVolume/100);
                }
            }else if (remove_Meteorite.get(i4)){
                explosionQueue.removeIndex(i4);
                meteorites.removeIndex(i4);
                healths.removeIndex(i4);
                radiuses.removeIndex(i4);
                fires.get(i4).dispose();
                fires.removeIndex(i4);
                degrees.removeIndex(i4);
                remove_Meteorite.removeIndex(i4);
            }
        }

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
        meteorites.clear();
        degrees.clear();
        healths.clear();
        radiuses.clear();
        explosionQueue.clear();
        remove_Meteorite.clear();
    }

    public static void removeMeteorite(int i, boolean explode){
        if(explode){
            explosionQueue.set(i, true);
            remove_Meteorite.set(i, true);
        }else{
            explosionQueue.set(i, false);
            remove_Meteorite.set(i, true);
        }
    }

}


