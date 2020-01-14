package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.view.GameUi;
import com.deo.flapd.view.MenuScreen;

import java.util.Random;

public class Bullet {

    private Polygon bounds;
    public static Array<Rectangle> bullets;
    public static Array<Float> damages;
    private Array<Float> degrees;
    private Array <ParticleEffect> explosions;
    private Array <Boolean> types;
    private Sprite bullet;

    private Sound shot;

    private boolean sound;

    public static int bulletsShot;

    private Random random;

    private float spread;

    private static Array<Boolean> explosionQueue, remove_Bullet;

    private Preferences prefs;

    private float shootingSpeedMultiplier;

    private Sprite laser;

    private int offset;

    public static Rectangle laserBounds;

    public Bullet(Texture bulletTexture, Texture laser, float spread, float shootingSpeedMultiplier, Polygon shipBounds, boolean newGame) {
        bounds = shipBounds;
        this.spread = spread;
        bullet = new Sprite(bulletTexture);

        prefs = Gdx.app.getPreferences("Preferences");

        random = new Random();

        bullets = new Array<>();
        damages = new Array<>();
        degrees = new Array<>();
        explosions = new Array<>();
        explosionQueue = new Array<>();
        remove_Bullet = new Array<>();
        types = new Array<>();

        if(!newGame){
            bulletsShot = prefs.getInteger(" bulletsShot");
        }else {
            bulletsShot = 0;
        }

        this.shootingSpeedMultiplier = shootingSpeedMultiplier;

        this.laser = new Sprite(laser);

        sound = MenuScreen.Sound;
        shot = Gdx.audio.newSound(Gdx.files.internal("music/gun4.ogg"));

        laserBounds = new Rectangle();
    }

    public void Spawn(float damage, float scale, boolean is_uranium) {

            Rectangle bullet = new Rectangle();

            bullet.x = bounds.getX() + 68;
            bullet.y = bounds.getY() + 10 - 10*(scale-1);

            bullet.setSize(75*scale, 20*scale);

            bullets.add(bullet);
            damages.add(damage);
            explosionQueue.add(false);
            remove_Bullet.add(false);
            if(GameUi.money > 0 && is_uranium) {
                types.add(true);
                GameUi.money--;
            }else{
                types.add(false);
            }

            degrees.add((random.nextFloat()-0.5f)*spread+bounds.getRotation()/20);

            bullet.x+=MathUtils.cosDeg(bounds.getRotation())*6;
            bullet.y+=MathUtils.cosDeg(bounds.getRotation());

            bulletsShot++;

            if(sound) {
                shot.play(MenuScreen.SoundVolume/100);
            }

    }

    public void draw(SpriteBatch batch, boolean is_paused) {

        /*
        int gradient = 0;
        for(float i = bounds.getX(); i < 800; i += 3){
            laser.setSize(3, 9);
            laser.setPosition(i+72, bounds.getY()+16);
            laser.setColor(new Color().fromHsv(gradient+offset, 1, 1).add(0,0,0,1));
            laserBounds.setSize(800, 9).setPosition(72, bounds.getY()+16);
            laser.draw(batch);
            gradient+=1;
        }
        offset+=10;
         */

        for (int i = 0; i < bullets.size; i ++) {

            Rectangle bullet = bullets.get(i);
            float angle = degrees.get(i);

            this.bullet.setPosition(bullet.x, bullet.y);
            this.bullet.setSize(bullet.width, bullet.height);
            this.bullet.setOrigin(bullet.width / 2f, bullet.height / 2f);
            this.bullet.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(300*angle, 1500));
            if(types.get(i)){
                this.bullet.setColor(Color.GREEN);
            }else{
                this.bullet.setColor(Color.WHITE);
            }
            this.bullet.draw(batch);

            if (!is_paused){
                bullet.x += 1500 * Gdx.graphics.getDeltaTime();
                bullet.y += 300 * angle * Gdx.graphics.getDeltaTime();

                if (bullet.x > 800) {
                    Bullet.removeBullet(i, false);
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
        for(int i4 = 0; i4 < bullets.size; i4++){
            if(explosionQueue.get(i4)) {
                ParticleEffect explosionEffect = new ParticleEffect();
                if(types.get(i4)){
                    explosionEffect.load(Gdx.files.internal("particles/explosion3_4.p"), Gdx.files.internal("particles"));
                }else{
                    explosionEffect.load(Gdx.files.internal("particles/explosion3_2.p"), Gdx.files.internal("particles"));
                }
                explosionEffect.setPosition(bullets.get(i4).x + bullets.get(i4).width / 2, bullets.get(i4).y + bullets.get(i4).height / 2);
                explosionEffect.start();
                explosions.add(explosionEffect);
                explosionQueue.removeIndex(i4);
                bullets.removeIndex(i4);
                degrees.removeIndex(i4);
                damages.removeIndex(i4);
                remove_Bullet.removeIndex(i4);
                types.removeIndex(i4);
            }else if (remove_Bullet.get(i4)){
                explosionQueue.removeIndex(i4);
                bullets.removeIndex(i4);
                degrees.removeIndex(i4);
                damages.removeIndex(i4);
                remove_Bullet.removeIndex(i4);
                types.removeIndex(i4);
            }
        }
    }

    public void dispose(){
        shot.dispose();
        bullets.clear();
        damages.clear();
        degrees.clear();
        for(int i3 = 0; i3 < explosions.size; i3 ++){
            explosions.get(i3).dispose();
            explosions.removeIndex(i3);
        }
        explosionQueue.clear();
        remove_Bullet.clear();
        types.clear();
    }

    public static void removeBullet(int i, boolean explode){
        if(explode){
            explosionQueue.set(i, true);
            remove_Bullet.set(i, true);
        }else{
            explosionQueue.set(i, false);
            remove_Bullet.set(i, true);
        }
    }

    public float getShootingSpeed(){
        return shootingSpeedMultiplier;
    }

}
