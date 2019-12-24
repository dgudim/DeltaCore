package com.deo.flapd.model.bullets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.view.GameScreen;
import com.deo.flapd.view.MenuScreen;

import java.util.Random;

public class EnemyBullet_sniper {

    public static Array<Rectangle> bullets;
    public static Array<Float> damages;
    private Array <Float> degrees;
    private Array <ParticleEffect> explosions;
    private Sprite bullet;
    private float millis;

    private Random random;

    private Sound shot;

    private boolean sound;

    private float Boffset_x, Boffset_y, width, height, spread;

    private static Array<Boolean> explosionQueue, remove_Bullet;

    public EnemyBullet_sniper(Texture bulletTexture, float width, float height, float Boffset_x,float Boffset_y, float Bspread) {
        this.Boffset_x = Boffset_x;
        this.Boffset_y = Boffset_y;
        this.width = width;
        this.height = height;
        spread = Bspread;
        bullet = new Sprite(bulletTexture);

        bullets = new Array<>();
        damages = new Array<>();
        degrees = new Array<>();
        explosions = new Array<>();
        explosionQueue = new Array<>();
        remove_Bullet = new Array<>();

        random = new Random();

        sound = MenuScreen.Sound;
        shot = Gdx.audio.newSound(Gdx.files.internal("music/gun2.ogg"));
    }

    public void Spawn(float damage,  Rectangle enemyBounds, float scale) {
        if(millis > 10 && !GameScreen.is_paused) {

            Rectangle bullet = new Rectangle();

            bullet.x = enemyBounds.getX()+Boffset_x;
            bullet.y = enemyBounds.getY()+Boffset_y;

            bullet.setSize(width*scale, height*scale);

            bullets.add(bullet);
            damages.add(damage);
            degrees.add((random.nextFloat()-0.5f)*spread);
            explosionQueue.add(false);
            remove_Bullet.add(false);

            if(sound) {
                shot.play(MenuScreen.SoundVolume/100);
            }

            millis = 0;
        }
        millis=millis+50*Gdx.graphics.getDeltaTime();
    }

    public void draw(SpriteBatch batch, boolean is_paused) {

        for (int i = 0; i < bullets.size; i ++) {

            Rectangle bullet = bullets.get(i);
            float angle = degrees.get(i);

            this.bullet.setPosition(bullet.x, bullet.y);
            this.bullet.setSize(bullet.width, bullet.height);
            this.bullet.setOrigin(bullet.width / 2f, bullet.height / 2f);
            this.bullet.draw(batch);

            if (!is_paused){
                bullet.x -= 1100 * Gdx.graphics.getDeltaTime();
                bullet.y -= 70 * angle * Gdx.graphics.getDeltaTime();

                if (bullet.x < -32) {
                    removeBullet(i, false);
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
                explosionEffect.load(Gdx.files.internal("particles/explosion3.p"), Gdx.files.internal("particles"));
                explosionEffect.setPosition(bullets.get(i4).x + bullets.get(i4).width / 2, bullets.get(i4).y + bullets.get(i4).height / 2);
                explosionEffect.start();
                explosions.add(explosionEffect);
                explosionQueue.removeIndex(i4);
                bullets.removeIndex(i4);
                degrees.removeIndex(i4);
                damages.removeIndex(i4);
                remove_Bullet.removeIndex(i4);
            }else if (remove_Bullet.get(i4)){
                explosionQueue.removeIndex(i4);
                bullets.removeIndex(i4);
                degrees.removeIndex(i4);
                damages.removeIndex(i4);
                remove_Bullet.removeIndex(i4);
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

}
