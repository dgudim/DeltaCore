package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Drops;
import com.deo.flapd.model.SpaceShip;
import com.deo.flapd.model.UraniumCell;
import com.deo.flapd.view.GameUi;

import java.util.Random;

import static com.deo.flapd.utils.DUtils.getFloat;

public class Boss_evilEye {
    private Sprite body;
    private Rectangle bodyBounds;

    private Sprite cannon;
    private Array<Rectangle> cannonBounds;

    private Sprite bullet;
    private Array<Rectangle> bullets;

    private Sprite shield, laser;

    private Image up_right, up_left, down_right, down_left, left, right, down, up, center;

    private Array<Float> degrees, degrees2, health;

    private Array<ParticleEffect> explosions;

    private float rotation, posX, millis, shieldSize;

    private long soundId;

    private Polygon shipBounds;

    private Random random;

    private boolean is_spawned, is_in_position, stage2, is_laserFire, animation;

    private Array<ProgressBar> healthBars;

    private ProgressBar.ProgressBarStyle healthBarStyle, healthBarStyle2;

    private Sound explosion, shot, laserSaw;

    private Rectangle laserTip;

    private ParticleEffect fire;

    private float soundVolume;

    public Boss_evilEye(AssetManager assetManager, Polygon shipBounds){

        soundVolume = getFloat("soundVolume");

        TextureAtlas bossAtlas = assetManager.get("boss_evil/bossEvil.atlas");

        up_right = new Image(bossAtlas.createSprite("evil_up_right"));
        up_left = new Image(bossAtlas.createSprite("evil_up_left"));
        down_right = new Image(bossAtlas.createSprite("evil_down_right"));
        down_left = new Image(bossAtlas.createSprite("evil_down_left"));
        left = new Image(bossAtlas.createSprite("evil_left"));
        right = new Image(bossAtlas.createSprite("evil_right"));
        up = new Image(bossAtlas.createSprite("evil_up"));
        down = new Image(bossAtlas.createSprite("evil_down"));
        center = new Image(bossAtlas.createSprite("evil_center"));
        laser = new Sprite((Texture)assetManager.get("laser.png"));

        cannon = new Sprite(bossAtlas.findRegion("evil_cannon"));
        body = new Sprite(bossAtlas.findRegion("evil_base"));
        bullet = new Sprite((Texture)assetManager.get("pew2.png"));
        shield = new Sprite((Texture)assetManager.get("HotShield.png"));

        this.shipBounds = shipBounds;

        cannonBounds = new Array<>();
        degrees = new Array<>();
        degrees2 = new Array<>();
        bullets = new Array<>();
        health = new Array<>();
        healthBars = new Array<>();
        explosions = new Array<>();
        random = new Random();

        bodyBounds = new Rectangle().setSize(128).setPosition(1000, 176);
        posX = 320;
        shieldSize = 0;
        soundId = 0;
        is_spawned = false;
        is_in_position = false;
        stage2 = false;
        is_laserFire = false;
        animation = true;

        healthBarStyle = new ProgressBar.ProgressBarStyle();
        healthBarStyle2 = new ProgressBar.ProgressBarStyle();

        laserTip = new Rectangle();

        Pixmap pixmap2 = new Pixmap(0, 6, Pixmap.Format.RGBA8888);
        pixmap2.setColor(Color.RED);
        pixmap2.fill();
        TextureRegionDrawable BarForeground1 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap2)));
        pixmap2.dispose();

        Pixmap pixmap3 = new Pixmap(100, 6, Pixmap.Format.RGBA8888);
        pixmap3.setColor(Color.RED);
        pixmap3.fill();
        TextureRegionDrawable BarForeground2 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap3)));
        pixmap3.dispose();

        Pixmap pixmap4 = new Pixmap(0, 14, Pixmap.Format.RGBA8888);
        pixmap4.setColor(Color.rgba8888(1, 0.5f, 0, 1));
        pixmap4.fill();
        TextureRegionDrawable BarForeground3 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap4)));
        pixmap4.dispose();

        Pixmap pixmap45 = new Pixmap(100, 14, Pixmap.Format.RGBA8888);
        pixmap45.setColor(Color.rgba8888(1, 0.3f, 0, 1));
        pixmap45.fill();
        TextureRegionDrawable BarForeground4 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap45)));
        pixmap45.dispose();

        Pixmap pixmap = new Pixmap(100, 6, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        TextureRegionDrawable BarBackground = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();

        Pixmap pixmap0 = new Pixmap(100, 14, Pixmap.Format.RGBA8888);
        pixmap0.setColor(Color.WHITE);
        pixmap0.fill();
        TextureRegionDrawable BarBackground2 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap0)));
        pixmap0.dispose();

        healthBarStyle.knob = BarForeground1;
        healthBarStyle.knobBefore = BarForeground2;
        healthBarStyle.background = BarBackground;

        healthBarStyle2.knob = BarForeground3;
        healthBarStyle2.knobBefore = BarForeground4;
        healthBarStyle2.background = BarBackground2;

        for(int i = 0; i < 10; i++){
            ProgressBar healthBar = new ProgressBar(0, 500, 0.01f, false, healthBarStyle);
            healthBar.setValue(500);
            healthBar.setSize(30, 14);
            healthBars.add(healthBar);
        }
        ProgressBar healthBar2 = new ProgressBar(0, 2000, 0.01f, false, healthBarStyle2);
        healthBar2.setValue(2000);
        healthBar2.setSize(80, 14);
        healthBars.add(healthBar2);

        shot = Gdx.audio.newSound(Gdx.files.internal("sfx/gun3.ogg"));
        explosion = Gdx.audio.newSound(Gdx.files.internal("sfx/explosion.ogg"));
        laserSaw = Gdx.audio.newSound(Gdx.files.internal("sfx/laserSaw.ogg"));

        fire = new ParticleEffect();
        fire.load(Gdx.files.internal("particles/fire2.p"), Gdx.files.internal("particles"));
    }

    public void Spawn(){
        float rotation = 0;
        bodyBounds.setPosition(1000, 176);
        posX = 320;
        is_in_position = false;
        is_spawned = true;
        stage2 = false;
        is_laserFire = false;
        animation = true;
        shieldSize = 0;
        soundId = 0;
        health.setSize(12);
        cannonBounds.setSize(10);
        degrees.setSize(10);
        for(int i = 0; i<10; i++) {
            Rectangle cannon = new Rectangle().setSize(60);
            rotation += 36;
            cannon.setPosition(bodyBounds.getX()+34-MathUtils.cosDeg(rotation) * 130, bodyBounds.getY()+34-MathUtils.sinDeg(rotation) * 130);
            cannonBounds.set(i, cannon);
            degrees.set(i, rotation);
            health.set(i, 500f);
        }
        health.set(10, 2000f);
        health.set(11, 1000f);
    }

    public void draw(SpriteBatch batch, boolean is_paused, float delta){
        if(is_spawned) {
            body.setPosition(bodyBounds.getX(), bodyBounds.getY());
            body.draw(batch);
            if(!stage2) {
                rotation = MathUtils.atan2(bodyBounds.getY() - shipBounds.getY() + 25, bodyBounds.getX() - shipBounds.getX()) * MathUtils.radiansToDegrees;
                if (rotation >= -22.5 && rotation <= 22.5) {
                    left.setPosition(bodyBounds.getX(), bodyBounds.getY());
                    left.draw(batch, 1);
                } else if (rotation >= 22.5 && rotation <= 67.5) {
                    down_left.setPosition(bodyBounds.getX(), bodyBounds.getY());
                    down_left.draw(batch, 1);
                } else if (rotation >= 67.5 && rotation <= 112.5) {
                    down.setPosition(bodyBounds.getX(), bodyBounds.getY());
                    down.draw(batch, 1);
                } else if (rotation >= 112.5 && rotation <= 157.5) {
                    down_right.setPosition(bodyBounds.getX(), bodyBounds.getY());
                    down_right.draw(batch, 1);
                } else if ((rotation >= 157.5 && rotation <= 180) || (rotation >= -180 && rotation <= -157.5)) {
                    right.setPosition(bodyBounds.getX(), bodyBounds.getY());
                    right.draw(batch, 1);
                } else if (rotation >= -157.5 && rotation <= -112.5) {
                    up_right.setPosition(bodyBounds.getX(), bodyBounds.getY());
                    up_right.draw(batch, 1);
                } else if (rotation >= -112.5 && rotation <= -67.5) {
                    up.setPosition(bodyBounds.getX(), bodyBounds.getY());
                    up.draw(batch, 1);
                } else if (rotation >= -67.5 && rotation <= -22.5) {
                    up_left.setPosition(bodyBounds.getX(), bodyBounds.getY());
                    up_left.draw(batch, 1);
                }
            }else{
                center.setPosition(bodyBounds.getX(), bodyBounds.getY());
                center.draw(batch, 1);
            }

            for (int i = 0; i < 10; i++) {
                if(health.get(i)>0) {
                    cannon.setPosition(bodyBounds.getX() + 34 - MathUtils.cosDeg(degrees.get(i)) * 130, bodyBounds.getY() + 34 - MathUtils.sinDeg(degrees.get(i)) * 130);
                    cannonBounds.get(i).setPosition(bodyBounds.getX() + 34 - MathUtils.cosDeg(degrees.get(i)) * 130, bodyBounds.getY() + 34 - MathUtils.sinDeg(degrees.get(i)) * 130);
                    cannon.setSize(cannonBounds.get(i).getWidth(), cannonBounds.get(i).getHeight());
                    cannon.draw(batch);
                }
                if(!is_paused) {
                    degrees.set(i, degrees.get(i) - 30*delta);
                }
                if(health.get(i) > 0) {
                    healthBars.get(i).setPosition(bodyBounds.getX() + 34 - MathUtils.cosDeg(degrees.get(i)) * 130 + 14, bodyBounds.getY() + 34 - MathUtils.sinDeg(degrees.get(i)) * 130 - 10);
                    healthBars.get(i).setValue(health.get(i));
                    healthBars.get(i).draw(batch, 1);
                    healthBars.get(i).act(delta);
                }else if(health.get(i) > -100){
                    explode(i, false);
                    if(random.nextBoolean()) {
                        Bonus.Spawn(random.nextInt(5), 1, cannonBounds.get(i));
                    }
                    Drops.drop(cannonBounds.get(i), 1, 2, 3);
                    health.set(i, -100f);
                    cannonBounds.get(i).setSize(0).setPosition(-100, -100);
                    GameUi.Score += 100;
                }
            }

            if(stage2) {
                if(!is_laserFire){
                    is_laserFire = true;
                    fire.start();
                }
                if(animation && !is_paused){
                    shieldSize += 240 * delta;
                    laserSaw.setVolume(soundId, MathUtils.clamp(shieldSize/192, 0, 1));
                    if(shieldSize>=192){
                        animation = false;
                    }
                }
                rotation = MathUtils.atan2( bodyBounds.getY() - shipBounds.getY() + 35.2f,  bodyBounds.getX() - shipBounds.getX() + 25.6f) * MathUtils.radiansToDegrees;
                for(int i = 0; i < 300; i++){
                    laser.setPosition(bodyBounds.getX() + 64 - laser.getWidth()/2 - MathUtils.cosDeg(rotation) * 2 * i, bodyBounds.getY() + 64 - laser.getHeight()/2 - MathUtils.sinDeg(rotation) * 2*i);
                    laser.setColor(new Color().fromHsv(i/9f, 1, 1).add(0,0,0,1));
                    laser.setSize(2, 9*health.get(10)/2000);
                    laserTip.setSize(2, 9*health.get(10)/2000).setPosition(laser.getX(), laser.getY());
                    laser.setRotation(rotation);
                    laser.setOrigin(laser.getWidth()/2, laser.getHeight()/2);
                    laser.draw(batch);
                    if(laserTip.overlaps(shipBounds.getBoundingRectangle())){
                        for(int i2 = i+1; i2 < i + 10; i2++){
                            laser.setPosition(bodyBounds.getX() + 64 - laser.getWidth()/2 - MathUtils.cosDeg(rotation) * 2 * i2, bodyBounds.getY() + 64 - laser.getHeight()/2 - MathUtils.sinDeg(rotation) * 2 * i2);
                            laser.setColor(new Color().fromHsv(i2/9f, 1, 1).add(0,0,0,1));
                            laser.setSize(2, 9*health.get(10)/2000);
                            laserTip.setSize(2, 9*health.get(10)/2000).setPosition(laser.getX(), laser.getY());
                            fire.setPosition(laser.getX(), laser.getY()+4.5f);
                            fire.draw(batch);
                            if(is_paused){
                                fire.update(0);
                            }else{
                                fire.update(delta);
                            }
                            laser.setRotation(rotation);
                            laser.setOrigin(laser.getWidth()/2, laser.getHeight()/2);
                            laser.draw(batch);
                        }
                        if(!is_paused) {
                            float multiplier = laser.getWidth()/8;
                            if (GameUi.Shield >= 0.2f*multiplier) {
                                GameUi.Shield -= 0.2f*multiplier;
                                SpaceShip.set_color(1, 0, 1, true);
                            } else {
                                GameUi.Health = GameUi.Health - (0.2f*multiplier - GameUi.Shield) / 2;
                                GameUi.Shield = 0;
                                SpaceShip.set_color(1, 0, 1, false);
                            }
                        }
                        break;
                    }
                }
                shield.setPosition(bodyBounds.getX()+64-shieldSize/2, bodyBounds.getY()+64-shieldSize/2);
                shield.setSize(shieldSize, shieldSize);
                shield.setAlpha(MathUtils.clamp(health.get(11)/1000, 0,1));
                shield.draw(batch);
                healthBars.get(10).setPosition(bodyBounds.getX() + 24, bodyBounds.getY() - 15);
                healthBars.get(10).setValue(health.get(10));
                healthBars.get(10).draw(batch, 1);
                healthBars.get(10).act(delta);

                if(health.get(11) < 1000 && !is_paused){
                    health.set(11, health.get(11)+90*delta);
                }
            }

            if(health.get(0) < 0 && health.get(1) < 0 && health.get(2) < 0 && health.get(3) < 0 && health.get(4) < 0 && health.get(5) < 0 && health.get(6) < 0 && health.get(7) < 0 && health.get(8) < 0 && health.get(9) < 0 && !stage2){
                stage2 = true;
                soundId = laserSaw.loop(0);
            }

            if(is_in_position) {
                for (int i = 0; i < Bullet.bullets.size; i++) {
                    for (int i2 = 0; i2 < 10; i2++) {
                        if (Bullet.bullets.get(i).overlaps(cannonBounds.get(i2))) {
                            Bullet.removeBullet(i, true);
                            health.set(i2, health.get(i2) - Bullet.damages.get(i));
                        }
                    }
                    if(stage2){
                        if(health.get(11) > 0) {
                            if (Bullet.bullets.get(i).overlaps(bodyBounds)) {
                                Bullet.removeBullet(i, true);
                                health.set(11, health.get(11) - Bullet.damages.get(i));
                            }
                        }else{
                            if (Bullet.bullets.get(i).overlaps(bodyBounds)) {
                                Bullet.removeBullet(i, true);
                                health.set(10, health.get(10) - Bullet.damages.get(i));
                            }
                        }
                        if(health.get(10) > -100f && health.get(10) < 0){
                            health.set(10, -100f);
                            explode(0, true);
                            GameUi.Score += 3000;
                            UraniumCell.Spawn(bodyBounds, random.nextInt(20)+5, 1, 1);
                            for (int i3 = 0; i3<5; i3++) {
                                Bonus.Spawn(4, 1, bodyBounds.getX() + i*5, bodyBounds.getY() + i*5);
                            }
                            Drops.drop(bodyBounds, 5, 2, 4);
                            laserSaw.stop();
                            reset();
                        }
                    }
                }
            }

            if( bodyBounds.getX() > posX && !is_paused){
                bodyBounds.setX(bodyBounds.getX()-80*delta);
            }else if(!is_in_position && !is_paused){
                is_in_position = true;
            }

            if(!is_paused) {
                shoot(delta);
            }

            for(int i = 0; i<bullets.size; i++){
                Rectangle bullet = bullets.get(i);
                float degree = degrees2.get(i);

                this.bullet.setPosition(bullets.get(i).getX(), bullets.get(i).getY());
                this.bullet.setSize(10, 10);
                this.bullet.draw(batch);

                if(!is_paused){
                    bullet.x -= MathUtils.cosDeg(degree) * 300 * delta;
                    bullet.y -= MathUtils.sinDeg(degree) * 300 * delta;

                    if(bullet.overlaps(shipBounds.getBoundingRectangle())){
                        if (GameUi.Shield >= 1) {
                            GameUi.Shield -= 1;
                            SpaceShip.set_color(1, 0, 1, true);
                        } else {
                            GameUi.Health = GameUi.Health - (1 - GameUi.Shield)/3;
                            GameUi.Shield = 0;
                            SpaceShip.set_color(1, 0, 1, false);
                        }
                        removeBullet(i);
                    }

                    if(bullet.y<-bullet.height || bullet.y>480 || bullet.x<-bullet.width || bullet.x>800){
                        removeBullet(i);
                    }

                }
            }
            if(bodyBounds.overlaps(shipBounds.getBoundingRectangle())){
                if(shipBounds.getX()+76.8f>bodyBounds.getX() && shipBounds.getX()+76.8f<bodyBounds.getX()+30){
                    shipBounds.setPosition(bodyBounds.getX()-76.8f, shipBounds.getY());
                }
                if(shipBounds.getY()+57.6f>bodyBounds.getY() && shipBounds.getY()+57.6f<bodyBounds.getY()+30){
                    shipBounds.setPosition(shipBounds.getX(), bodyBounds.getY()-57.6f);
                }
                if(shipBounds.getY()<bodyBounds.getY()+128 && shipBounds.getY()>bodyBounds.getY()+98){
                    shipBounds.setPosition(shipBounds.getX(), bodyBounds.getY()+128);
                }
                if(shipBounds.getX()<bodyBounds.getX()+128 && shipBounds.getX()>bodyBounds.getX()+98){
                    shipBounds.setPosition(bodyBounds.getX()+128, shipBounds.getY());
                }
            }
        }
        for(int i3 = 0; i3 < explosions.size; i3 ++){
            explosions.get(i3).draw(batch);
            if(!is_paused) {
                explosions.get(i3).update(delta);
            }else{
                explosions.get(i3).update(0);
            }
            if(explosions.get(i3).isComplete()){
                explosions.get(i3).dispose();
                explosions.removeIndex(i3);
            }
        }
    }

    private void shoot(float delta){
        if(millis > 7 && is_in_position) {
            Rectangle bullet = new Rectangle();
            int num = random.nextInt(10);
            if(health.get(num) > 0) {
                bullet.setPosition(cannonBounds.get(num).getX() + 25, cannonBounds.get(num).getY() + 25);
                bullet.setSize(10);
                degrees2.add(MathUtils.radiansToDegrees * MathUtils.atan2(bullet.getY() - shipBounds.getY() - 30, bullet.getX() - shipBounds.getX() - 30));
                bullets.add(bullet);
                if (soundVolume>0) {
                    shot.play(soundVolume / 100);
                }
            }
            millis = 0;
        }
        millis=millis+60*delta;
    }

    private void removeBullet(int i){
        bullets.removeIndex(i);
        degrees2.removeIndex(i);
    }

    private void explode(int i, boolean explodeBody){
        ParticleEffect explosionEffect = new ParticleEffect();
        if(!explodeBody) {
            explosionEffect.load(Gdx.files.internal("particles/explosion_evil_small.p"), Gdx.files.internal("particles"));
            explosionEffect.setPosition(cannonBounds.get(i).getX() + 30, cannonBounds.get(i).getY() + 30);
            explosionEffect.start();
        }else{
            explosionEffect.load(Gdx.files.internal("particles/explosion_evil.p"), Gdx.files.internal("particles"));
            explosionEffect.setPosition(bodyBounds.getX() + 64, bodyBounds.getY() + 64);
            explosionEffect.start();
        }
        explosions.add(explosionEffect);
        if (soundVolume>0) {
            explosion.play(soundVolume / 100);
        }
    }

    private void reset(){
        Spawn();
        is_spawned = false;
        GameLogic.bossWave = false;
        fire.reset();
    }

    public void dispose(){
        bullets.clear();
        degrees.clear();
        degrees2.clear();
        healthBars.clear();
        cannonBounds.clear();
        shot.dispose();
        laserSaw.stop();
        laserSaw.dispose();
        explosion.dispose();
        for(int i3 = 0; i3 < explosions.size; i3 ++){
            explosions.get(i3).dispose();
            explosions.removeIndex(i3);
        }
        fire.dispose();
    }
}