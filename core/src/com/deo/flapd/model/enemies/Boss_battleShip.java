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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.SpaceShip;
import com.deo.flapd.model.UraniumCell;
import com.deo.flapd.view.GameUi;
import com.deo.flapd.view.MenuScreen;

import java.util.Random;

public class Boss_battleShip {

    private Sprite main;
    private Sprite main_wrecked;
    private Sprite main_not_wrecked;
    private Sprite cannon_small;
    private Sprite cannon_small2;
    private Sprite cannon_stage2;
    private Sprite cannon_homing_part1;
    private Sprite cannon_homing_part2;
    private Sprite bullet, bullet2, bullet3, bullet4;

    private Array <Rectangle> bullets_blue;
    private Array <Rectangle> bullets_red;
    private Array <Rectangle> bullets_red_big;
    private Array <Rectangle> bullets_red_long;
    private Array <Float> healths;
    private Array <Float> degrees_blue;
    private Array <Float> degrees_red;
    private Array <Float> bullets_red_big_timers;
    private Array <Float> bullets_red_big_timers_ideal;
    private Array <Float> degrees_red_long;
    private Array <Boolean> explodedCannons;
    private Array <ParticleEffect> fires;
    private Array <ParticleEffect> explosions;
    private float millis;
    private float laserOffset, bodyOffset;
    private float dX, dY;

    private ProgressBar health_cannon1;
    private ProgressBar health_cannon2;
    private ProgressBar health_cannon3;
    private ProgressBar health_cannon4;
    private ProgressBar health_cannon5;
    private ProgressBar health_cannon6;

    private ProgressBar health_cannon_stage2;
    private ProgressBar health_cannon_front;
    private ProgressBar health_body;
    private ProgressBar health_cannon_homing;

    private ProgressBar.ProgressBarStyle healthBarStyle, healthBarStyle2;

    private boolean stage2;

    private Random random;

    private boolean animation2, animation3;

    private float offset, millis2;

    private boolean is_spawned, deathAnimation;

    private float posX, posY, posX_original, posY_original;

    private Polygon shipBounds, bounds_body, bounds_cannon, bounds_cannon2, bounds_cannon3, bounds_cannon4, bounds_cannon5, bounds_cannon6, bounds_cannon_front, bounds_cannon_front_big, bounds_homing1, bounds_homing2;

    private Sound shot, shot2, shot3, shot4, explosion;

    private boolean sound;

    private UraniumCell uraniumCell;
    private Bonus bonus;

    public Boss_battleShip(AssetManager assetManager, float posX, float posY, Polygon shipBounds, UraniumCell cell){

        this.shipBounds = shipBounds;

        uraniumCell = cell;

        main = new Sprite();
        main_not_wrecked = new Sprite((Texture)assetManager.get("boss_ship/boss.png"));
        main_wrecked = new Sprite((Texture)assetManager.get("boss_ship/boss_dead.png"));
        cannon_small = new Sprite((Texture)assetManager.get("boss_ship/cannon2.png"));
        cannon_small2 = new Sprite((Texture)assetManager.get("boss_ship/cannon1.png"));
        cannon_stage2 = new Sprite((Texture)assetManager.get("boss_ship/bigCannon.png"));
        cannon_homing_part1 = new Sprite((Texture)assetManager.get("boss_ship/upperCannon_part1.png"));
        cannon_homing_part2 = new Sprite((Texture)assetManager.get("boss_ship/upperCannon_part2.png"));
        bullet = new Sprite((Texture)assetManager.get("boss_ship/bullet_blue.png"));
        bullet2 = new Sprite((Texture)assetManager.get("pew2.png"));
        bullet3 = new Sprite((Texture)assetManager.get("boss_ship/bullet_red_thick.png"));
        bullet4 = new Sprite((Texture)assetManager.get("boss_ship/bullet_red.png"));
        main.set(main_not_wrecked);

        bullets_blue = new Array<>();
        bullets_red = new Array<>();
        bullets_red_big = new Array<>();
        bullets_red_long = new Array<>();
        healths = new Array<>();
        degrees_blue = new Array<>();
        degrees_red = new Array<>();
        degrees_red_long = new Array<>();
        bullets_red_big_timers = new Array<>();
        bullets_red_big_timers_ideal = new Array<>();
        explodedCannons = new Array<>();
        fires = new Array<>();
        explosions = new Array<>();

        bounds_body = new Polygon(new float[]{0f, 0f, 556, 0f, 556, 172, 0f, 172});
        bounds_cannon = new Polygon(new float[]{0f, 0f, 32, 0f, 32, 28, 0f, 28});
        bounds_cannon2 = new Polygon(new float[]{0f, 0f, 32, 0f, 32, 28, 0f, 28});
        bounds_cannon3 = new Polygon(new float[]{0f, 0f, 32, 0f, 32, 28, 0f, 28});
        bounds_cannon4 = new Polygon(new float[]{0f, 0f, 32, 0f, 32, 28, 0f, 28});
        bounds_cannon5 = new Polygon(new float[]{0f, 0f, 32, 0f, 32, 28, 0f, 28});
        bounds_cannon6 = new Polygon(new float[]{0f, 0f, 32, 0f, 32, 28, 0f, 28});
        bounds_cannon_front = new Polygon(new float[]{0f, 0f, 44, 0f, 44, 32, 0f, 32});
        bounds_homing1 = new Polygon(new float[]{0f, 0f, 100, 0f, 100, 28, 0f, 28});
        bounds_homing2 = new Polygon(new float[]{0f, 0f, 36, 0f, 36, 8, 0f, 8});
        bounds_cannon_front_big = new Polygon(new float[]{0f, 0f, 120, 0f, 120, 32, 0f, 32});

        cannon_homing_part2.setOrigin(36,4);
        cannon_homing_part1.setOrigin(50, 14);
        cannon_small.setOrigin(18,14);
        cannon_small2.setOrigin(28, 16);
        cannon_stage2.setOrigin(120, 16);
        main.setOrigin(278, 86);
        bullet.setOrigin(7, 7);
        bullet2.setOrigin(12, 12);
        bullet3.setOrigin(14, 14);
        bullet4.setOrigin(26, 6);

        is_spawned = false;
        animation2 = false;
        animation3 = false;
        stage2 = false;
        offset = 0;
        laserOffset = 0;
        bodyOffset = 0;
        millis2 = 0;

        this.posX = posX;
        this.posY = posY;

        posX_original = posX;
        posY_original = posY;

        random = new Random();

        healths.setSize(11);
        //cannons
        healths.set(1, 333f);
        healths.set(2, 333f);
        healths.set(3, 333f);
        healths.set(4, 333f);
        healths.set(5, 333f);
        healths.set(6, 333f);
        //body
        healths.set(7, 2333f);
        //big Gun
        healths.set(8, 1000f);
        //stage2 Gun
        healths.set(9, 1333f);
        //homing Gun
        healths.set(10, 666f);

        explodedCannons.setSize(10);

        //body
        explodedCannons.set(0, false);
        //cannons
        explodedCannons.set(1, false);
        explodedCannons.set(2, false);
        explodedCannons.set(3, false);
        explodedCannons.set(4, false);
        explodedCannons.set(5, false);
        explodedCannons.set(6, false);
        //big Gun
        explodedCannons.set(7, false);
        //stage2 Gun
        explodedCannons.set(8, false);
        //homing Gun
        explodedCannons.set(9, false);

        healthBarStyle = new ProgressBar.ProgressBarStyle();
        healthBarStyle2 = new ProgressBar.ProgressBarStyle();

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
        pixmap4.setColor(Color.RED);
        pixmap4.fill();
        TextureRegionDrawable BarForeground3 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap4)));
        pixmap4.dispose();

        Pixmap pixmap45 = new Pixmap(100, 14, Pixmap.Format.RGBA8888);
        pixmap45.setColor(Color.RED);
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

        health_cannon1 = new ProgressBar(0, 333, 0.01f, false, healthBarStyle);
        health_cannon2 = new ProgressBar(0, 333, 0.01f, false, healthBarStyle);
        health_cannon3 = new ProgressBar(0, 333, 0.01f, false, healthBarStyle);
        health_cannon4 = new ProgressBar(0, 333, 0.01f, false, healthBarStyle);
        health_cannon5 = new ProgressBar(0, 333, 0.01f, false, healthBarStyle);
        health_cannon6 = new ProgressBar(0, 333, 0.01f, false, healthBarStyle);
        health_body = new ProgressBar(0, 2333, 0.01f, false, healthBarStyle2);
        health_cannon_front = new ProgressBar(0, 1000, 0.01f, false, healthBarStyle);
        health_cannon_homing = new ProgressBar(0, 666, 0.01f, false, healthBarStyle);
        health_cannon_stage2 = new ProgressBar(0, 1333, 0.01f, false, healthBarStyle);

        health_cannon1.setSize(25, 6);
        health_cannon2.setSize(25, 6);
        health_cannon3.setSize(25, 6);
        health_cannon4.setSize(25, 6);
        health_cannon5.setSize(25, 6);
        health_cannon6.setSize(25, 6);
        health_body.setSize(56, 14);
        health_cannon_front.setSize(44, 10);
        health_cannon_homing.setSize(100, 10);
        health_cannon_stage2.setSize(120, 10);

        health_cannon1.setAnimateDuration(0.25f);
        health_cannon2.setAnimateDuration(0.25f);
        health_cannon3.setAnimateDuration(0.25f);
        health_cannon4.setAnimateDuration(0.25f);
        health_cannon5.setAnimateDuration(0.25f);
        health_cannon6.setAnimateDuration(0.25f);
        health_body.setAnimateDuration(0.25f);
        health_cannon_front.setAnimateDuration(0.25f);
        health_cannon_homing.setAnimateDuration(0.25f);
        health_cannon_stage2.setAnimateDuration(0.25f);

        sound = MenuScreen.Sound;
        shot = Gdx.audio.newSound(Gdx.files.internal("music/gun1.ogg"));
        shot2 = Gdx.audio.newSound(Gdx.files.internal("music/gun2.ogg"));
        shot3 = Gdx.audio.newSound(Gdx.files.internal("music/gun3.ogg"));
        shot4 = Gdx.audio.newSound(Gdx.files.internal("music/gun4.ogg"));
        explosion = Gdx.audio.newSound(Gdx.files.internal("music/explosion.ogg"));
    }

    public void Spawn(){
        is_spawned = true;
        animation2 = false;
        animation3 = false;
        stage2 = false;
        offset = 0;
        laserOffset = 0;
        bodyOffset = 0;
        millis2 = 0;
        deathAnimation = false;
        bounds_body.setRotation(0);

        main.set(main_not_wrecked);

        healths.setSize(11);
        //cannons
        healths.set(1, 333f);
        healths.set(2, 333f);
        healths.set(3, 333f);
        healths.set(4, 333f);
        healths.set(5, 333f);
        healths.set(6, 333f);
        //body
        healths.set(7, 2333f);
        //big Gun
        healths.set(8, 1000f);
        //stage2 Gun
        healths.set(9, 1333f);
        //homing Gun
        healths.set(10, 666f);

        explodedCannons.setSize(10);

        //body
        explodedCannons.set(0, false);
        //cannons
        explodedCannons.set(1, false);
        explodedCannons.set(2, false);
        explodedCannons.set(3, false);
        explodedCannons.set(4, false);
        explodedCannons.set(5, false);
        explodedCannons.set(6, false);
        //big Gun
        explodedCannons.set(7, false);
        //stage2 Gun
        explodedCannons.set(8, false);
        //homing Gun
        explodedCannons.set(9, false);

        posX = posX_original;
        posY = posY_original;
    }

    public void draw(SpriteBatch batch, boolean is_paused) {

        if (is_spawned || deathAnimation) {

            bounds_cannon.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon.getY()-shipBounds.getY(), bounds_cannon.getX()-shipBounds.getX()));
            bounds_cannon2.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon2.getY()-shipBounds.getY(), bounds_cannon2.getX()-shipBounds.getX()));
            bounds_cannon3.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon3.getY()-shipBounds.getY(), bounds_cannon3.getX()-shipBounds.getX()));
            bounds_cannon4.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon4.getY()-shipBounds.getY(), bounds_cannon4.getX()-shipBounds.getX()));
            bounds_cannon5.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon5.getY()-shipBounds.getY(), bounds_cannon5.getX()-shipBounds.getX()));
            bounds_cannon6.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon6.getY()-shipBounds.getY(), bounds_cannon6.getX()-shipBounds.getX()));
            bounds_cannon_front.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon_front.getY()-shipBounds.getY(), bounds_cannon_front.getX()-shipBounds.getX()));
            bounds_cannon_front_big.setRotation(MathUtils.clamp(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon_front_big.getY()-shipBounds.getY(), bounds_cannon_front_big.getX()-shipBounds.getX()), -20, 20));
            bounds_homing2.setRotation(MathUtils.clamp(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_homing2.getY()-shipBounds.getY(), bounds_homing2.getX()-shipBounds.getX()), -30, 30));

            health_cannon1.setPosition(bounds_cannon.getX()+3, bounds_cannon.getY()-5);
            health_cannon2.setPosition(bounds_cannon2.getX()+3, bounds_cannon2.getY()-5);
            health_cannon3.setPosition(bounds_cannon3.getX()+3, bounds_cannon3.getY()-5);
            health_cannon4.setPosition(bounds_cannon4.getX()+3, bounds_cannon4.getY()-5);
            health_cannon5.setPosition(bounds_cannon5.getX()+3, bounds_cannon5.getY()-5);
            health_cannon6.setPosition(bounds_cannon6.getX()+3, bounds_cannon6.getY()-5);

            health_body.setPosition(bounds_body.getX()+ 250, bounds_body.getY() + 12);

            health_cannon_homing.setPosition(bounds_homing1.getX()-2, bounds_homing1.getY() - 7);

            health_cannon_front.setPosition(bounds_cannon_front.getX(), bounds_cannon_front.getY() - 9);

            health_cannon_stage2.setPosition(bounds_cannon_front_big.getX(), bounds_cannon_front_big.getY()-7);

            health_cannon1.setValue(healths.get(1));
            health_cannon2.setValue(healths.get(2));
            health_cannon3.setValue(healths.get(3));
            health_cannon4.setValue(healths.get(4));
            health_cannon5.setValue(healths.get(5));
            health_cannon6.setValue(healths.get(6));

            health_cannon_homing.setValue(healths.get(10));

            health_cannon_front.setValue(healths.get(8));

            health_body.setValue(healths.get(7));

            health_cannon_stage2.setValue(healths.get(9));

            if(healths.get(9)>0 && healths.get(7)>0) {
                bounds_cannon_front_big.setPosition(posX + 40 - laserOffset + bodyOffset, posY + 48);

                health_cannon_stage2.draw(batch, 1);
                health_cannon_stage2.act(Gdx.graphics.getDeltaTime());

                cannon_stage2.setPosition(bounds_cannon_front_big.getX(), bounds_cannon_front_big.getY());
                cannon_stage2.setRotation(bounds_cannon_front_big.getRotation());
                cannon_stage2.draw(batch);
            }else{
                if(!explodedCannons.get(8)){
                    explodedCannons.set(8, true);
                    play_shot_Sound(1, sound, true, 9);
                    GameUi.Score += 2000;
                }
                bounds_cannon_front_big.setPosition(-100, -100);
            }

            if(healths.get(7)>0) {
                bounds_body.setPosition(posX+bodyOffset, posY);

                main.setPosition(bounds_body.getX(), bounds_body.getY());
                main.setRotation(bounds_body.getRotation());
                main.draw(batch);

                if(stage2) {
                    health_body.draw(batch, 1);
                    health_body.act(Gdx.graphics.getDeltaTime());
                }

            }else{
                if(!animation3){
                    dX = posX;
                    dY = posY;
                    main.set(main_wrecked);
                    explodedCannons.set(0, true);
                    animation3 = true;
                    play_shot_Sound(2, sound, true, 0);
                    GameLogic.bossWave = false;
                    is_spawned = false;
                    stage2 = false;
                    deathAnimation = true;
                    bonus.Spawn(4, 1, posX + 278, posY + 86);
                    GameUi.Score += 3000;
                    uraniumCell.Spawn(posX + 278, posY + 86, random.nextInt(25)+10, 1, 1);
                }
                main.setPosition(bounds_body.getX(), bounds_body.getY());
                main.setRotation(bounds_body.getRotation());
                bounds_body.setPosition(dX+bodyOffset, dY);
                if(!is_paused) {
                    bounds_body.rotate(0.2f);
                    dX -= 0.5f;
                    dY -= 0.5f;
                }
                main.draw(batch);
            }

            if(healths.get(1)>0) {
                bounds_cannon.setPosition(posX + 224, posY + 40);

                cannon_small.setPosition(bounds_cannon.getX(), bounds_cannon.getY());
                cannon_small.setRotation(bounds_cannon.getRotation());
                cannon_small.draw(batch);

                health_cannon1.draw(batch, 1);
                health_cannon1.act(Gdx.graphics.getDeltaTime());
            }else{
                if(!explodedCannons.get(1)){
                    explodedCannons.set(1, true);
                    play_shot_Sound(1, sound, true, 1);
                    GameUi.Score += 1000;
                }
                bounds_cannon.setPosition(-100, -100);
            }

            if(healths.get(2)>0) {
                bounds_cannon2.setPosition(posX + 256, posY + 40);

                cannon_small.setPosition(bounds_cannon2.getX(), bounds_cannon2.getY());
                cannon_small.setRotation(bounds_cannon2.getRotation());
                cannon_small.draw(batch);

                health_cannon2.draw(batch, 1);
                health_cannon2.act(Gdx.graphics.getDeltaTime());
            }else{
                if(!explodedCannons.get(2)){
                    explodedCannons.set(2, true);
                    play_shot_Sound(1, sound, true, 2);
                    GameUi.Score += 1000;
                }
                bounds_cannon2.setPosition(-100, -100);
            }

            if(healths.get(3)>0) {
                bounds_cannon3.setPosition(posX + 288, posY + 40);

                cannon_small.setPosition(bounds_cannon3.getX(), bounds_cannon3.getY());
                cannon_small.setRotation(bounds_cannon3.getRotation());
                cannon_small.draw(batch);

                health_cannon3.draw(batch, 1);
                health_cannon3.act(Gdx.graphics.getDeltaTime());
            }else{
                if(!explodedCannons.get(3)){
                    explodedCannons.set(3, true);
                    play_shot_Sound(1, sound, true, 3);
                    GameUi.Score += 1000;
                }
                bounds_cannon3.setPosition(-100, -100);
            }

            if(healths.get(4)>0) {
                bounds_cannon4.setPosition(posX + 376, posY + 40);

                cannon_small.setPosition(bounds_cannon4.getX(), bounds_cannon4.getY());
                cannon_small.setRotation(bounds_cannon4.getRotation());
                cannon_small.draw(batch);

                health_cannon4.draw(batch, 1);
                health_cannon4.act(Gdx.graphics.getDeltaTime());
            }else{
                if(!explodedCannons.get(4)){
                    explodedCannons.set(4, true);
                    play_shot_Sound(1, sound, true, 4);
                    GameUi.Score += 1000;
                }
                bounds_cannon4.setPosition(-100, -100);
            }

            if(healths.get(5)>0) {
                bounds_cannon5.setPosition(posX + 408, posY + 40);

                cannon_small.setPosition(bounds_cannon5.getX(), bounds_cannon5.getY());
                cannon_small.setRotation(bounds_cannon5.getRotation());
                cannon_small.draw(batch);

                health_cannon5.draw(batch, 1);
                health_cannon5.act(Gdx.graphics.getDeltaTime());
            }else{
                if(!explodedCannons.get(5)){
                    explodedCannons.set(5, true);
                    play_shot_Sound(1, sound, true, 5);
                    GameUi.Score += 1000;
                }
                bounds_cannon5.setPosition(-100, -100);
            }

            if(healths.get(6)>0) {
                bounds_cannon6.setPosition(posX + 440, posY + 40);

                cannon_small.setPosition(bounds_cannon6.getX(), bounds_cannon6.getY());
                cannon_small.setRotation(bounds_cannon6.getRotation());
                cannon_small.draw(batch);

                health_cannon6.draw(batch, 1);
                health_cannon6.act(Gdx.graphics.getDeltaTime());
            }else{
                if(!explodedCannons.get(6)){
                    explodedCannons.set(6, true);
                    play_shot_Sound(1, sound, true, 6);
                    GameUi.Score += 1000;
                }
                bounds_cannon6.setPosition(-100, -100);
            }

            if(healths.get(8)>0) {
                bounds_cannon_front.setPosition(posX + 60, posY + 58);

                cannon_small2.setPosition(bounds_cannon_front.getX(), bounds_cannon_front.getY());
                cannon_small2.setRotation(bounds_cannon_front.getRotation());
                cannon_small2.draw(batch);

                health_cannon_front.draw(batch, 1);
                health_cannon_front.act(Gdx.graphics.getDeltaTime());
            }else{
                if(!explodedCannons.get(7)){
                    explodedCannons.set(7, true);
                    play_shot_Sound(1, sound, true, 7);
                    GameUi.Score += 1500;
                }
                bounds_cannon_front.setPosition(-100, -100);
            }

            if(healths.get(10)>0) {
                bounds_homing1.setPosition(posX + 94, posY + 116);
                bounds_homing2.setPosition(posX + 67, posY + 126);

                cannon_homing_part2.setPosition(bounds_homing2.getX(), bounds_homing2.getY());
                cannon_homing_part2.setRotation(bounds_homing2.getRotation());
                cannon_homing_part2.draw(batch);

                cannon_homing_part1.setPosition(bounds_homing1.getX(), bounds_homing1.getY());
                cannon_homing_part1.setRotation(bounds_homing1.getRotation());
                cannon_homing_part1.draw(batch);

                health_cannon_homing.draw(batch, 1);
                health_cannon_homing.act(Gdx.graphics.getDeltaTime());
            }else{
                if(!explodedCannons.get(9)){
                    explodedCannons.set(9, true);
                    play_shot_Sound(1, sound, true, 8);
                    GameUi.Score += 1500;
                }
                bounds_homing1.setPosition(-100, -100);
                bounds_homing2.setPosition(-100, -100);
            }

            if(!is_paused){

                if(posX>245){
                    posX--;
                }

                if(animation2){
                    offset = offset + 0.04f;
                }else{
                    offset = offset - 0.04f;
                }

                if(offset>1.3){
                    animation2 = false;
                }
                if(offset<-1.3){
                    animation2 = true;
                }

                if(!explodedCannons.get(0)){
                    posY = posY + offset;
                }

                if(!stage2 && explodedCannons.get(1) && explodedCannons.get(2) && explodedCannons.get(3) && explodedCannons.get(4) && explodedCannons.get(5) && explodedCannons.get(6) && explodedCannons.get(7) && explodedCannons.get(9)){
                    stage2 = true;
                }

                if(laserOffset<140 && stage2){
                    laserOffset++;
                }

                if(bodyOffset<170 && stage2){
                    bodyOffset++;
                }

                if(bounds_body.getBoundingRectangle().overlaps(shipBounds.getBoundingRectangle()) && !explodedCannons.get(0) && !stage2){
                    if(shipBounds.getX()+76.8f>bounds_body.getX() && shipBounds.getX()+76.8f<bounds_body.getX()+30){
                        shipBounds.setPosition(bounds_body.getX()-76.8f, shipBounds.getY());
                    }
                    if(shipBounds.getY()+57.6f>bounds_body.getY() && shipBounds.getY()+57.6f<bounds_body.getY()+30){
                        shipBounds.setPosition(shipBounds.getX(), bounds_body.getY()-57.6f);
                    }
                    if(shipBounds.getY()<bounds_body.getY()+152 && shipBounds.getY()>bounds_body.getY()+122){
                        shipBounds.setPosition(shipBounds.getX(), bounds_body.getY()+152);
                    }
                }
            }

            for (int i = 0; i<bullets_blue.size; i++){

                Rectangle bullet = bullets_blue.get(i);
                float degree = degrees_blue.get(i);

                this.bullet.setPosition(bullet.x, bullet.y);
                this.bullet.setSize(bullet.width, bullet.height);
                this.bullet.draw(batch);

                if(!is_paused){
                    bullet.x -= MathUtils.cosDeg(degree) * 300 * Gdx.graphics.getDeltaTime();
                    bullet.y -= MathUtils.sinDeg(degree) * 300 * Gdx.graphics.getDeltaTime();

                    if(bullet.overlaps(shipBounds.getBoundingRectangle())){
                        if (GameUi.Shield >= 5) {
                            GameUi.Shield -= 5;
                            SpaceShip.set_color(1, 0, 1, true);
                        } else {
                            GameUi.Health = GameUi.Health - (5 - GameUi.Shield) / 5;
                            GameUi.Shield = 0;
                            SpaceShip.set_color(1, 0, 0, false);
                        }
                        removeBullet(i, 1);
                    }

                    if(bullet.y<-bullet.height || bullet.y>480 || bullet.x<-bullet.width || bullet.x>800){
                        removeBullet(i, 1);
                    }

                }
            }

            for (int i = 0; i<bullets_red.size; i++){

                Rectangle bullet = bullets_red.get(i);
                float degree = degrees_red.get(i);

                this.bullet2.setPosition(bullet.x, bullet.y);
                this.bullet2.setSize(bullet.width, bullet.height);
                this.bullet2.draw(batch);

                if(!is_paused){
                    bullet.x -= MathUtils.cosDeg(degree) * 300 * Gdx.graphics.getDeltaTime();
                    bullet.y -= MathUtils.sinDeg(degree) * 300 * Gdx.graphics.getDeltaTime();

                    if(bullet.overlaps(shipBounds.getBoundingRectangle())){
                        if (GameUi.Shield >= 10) {
                            GameUi.Shield -= 10;
                            SpaceShip.set_color(1, 0, 1, true);
                        } else {
                            GameUi.Health = GameUi.Health - (10 - GameUi.Shield) / 5;
                            GameUi.Shield = 0;
                            SpaceShip.set_color(1, 0, 0, false);
                        }
                        removeBullet(i, 2);
                    }

                    if(bullet.y<-bullet.height || bullet.y>480 || bullet.x<-bullet.width || bullet.x>800){
                        removeBullet(i, 2);
                    }
                }
            }

            for (int i = 0; i<bullets_red_big.size; i++){

                Rectangle bullet = bullets_red_big.get(i);
                ParticleEffect fire = fires.get(i);
                float timer = bullets_red_big_timers.get(i);
                float ideal_timer = bullets_red_big_timers_ideal.get(i);

                this.bullet3.setPosition(bullet.x, bullet.y);
                this.bullet3.setSize(bullet.width, bullet.height);
                this.bullet3.draw(batch);
                this.bullet3.rotate(2);

                Vector2 pos1 = new Vector2();
                pos1.set(bullet.x, bullet.y);
                Vector2 pos2 = new Vector2();
                pos2.set(shipBounds.getX(), shipBounds.getY());
                pos1.lerp(pos2, 0.02f);

                fire.setPosition(bullet.getX() + bullet.width / 2, bullet.getY() + bullet.height / 2);
                fire.draw(batch);

                if(!is_paused) {
                    fire.update(Gdx.graphics.getDeltaTime());
                }else{
                    fire.update(0);
                }

                if(!is_paused){
                    bullet.x = pos1.x;
                    bullet.y = pos1.y;

                    timer = timer - 1*Gdx.graphics.getDeltaTime();
                    bullets_red_big_timers.set(i, timer);

                    if(bullet.overlaps(shipBounds.getBoundingRectangle())){
                        if (GameUi.Shield >= 15) {
                            GameUi.Shield -= 15;
                            SpaceShip.set_color(1, 0, 1, true);
                        } else {
                            GameUi.Health = GameUi.Health - (15 - GameUi.Shield) / 5;
                            GameUi.Shield = 0;
                            SpaceShip.set_color(1, 0, 0, false);
                        }
                        removeBullet(i, 3);
                    }

                    if(bullet.y<-bullet.height || bullet.y>480 || bullet.x<-bullet.width || bullet.x>800){
                        removeBullet(i, 3);
                    }
                }

                if (timer < ideal_timer/10) {
                    removeBullet(i, 3);
                }

            }

            for (int i = 0; i<bullets_red_long.size; i++){

                Rectangle bullet = bullets_red_long.get(i);
                float degree = degrees_red_long.get(i);

                this.bullet4.setPosition(bullet.x, bullet.y);
                this.bullet4.setSize(bullet.width, bullet.height);
                this.bullet4.draw(batch);
                this.bullet4.rotate(1);

                if(!is_paused){
                    bullet.x -= MathUtils.cosDeg(degree) * 300 * Gdx.graphics.getDeltaTime();
                    bullet.y -= MathUtils.sinDeg(degree) * 300 * Gdx.graphics.getDeltaTime();

                    if(bullet.overlaps(shipBounds.getBoundingRectangle())){
                        if (GameUi.Shield >= 7) {
                            GameUi.Shield -= 7;
                            SpaceShip.set_color(1, 0, 1, true);
                        } else {
                            GameUi.Health = GameUi.Health - (7 - GameUi.Shield) / 5;
                            GameUi.Shield = 0;
                            SpaceShip.set_color(1, 0, 0, false);
                        }
                        removeBullet(i, 4);
                    }

                    if(bullet.y<-bullet.height || bullet.y>480 || bullet.x<-bullet.width || bullet.x>800){
                        removeBullet(i, 4);
                    }
                }
            }

            if(posX<246) {

            for(int i = 0; i< Bullet.bullets.size; i++){

                    if (Bullet.bullets.get(i).overlaps(bounds_cannon.getBoundingRectangle())) {
                        healths.set(1, healths.get(1) - Bullet.damages.get(i));
                        Bullet.removeBullet(i, true);
                    } else if (Bullet.bullets.get(i).overlaps(bounds_cannon2.getBoundingRectangle())) {
                        healths.set(2, healths.get(2) - Bullet.damages.get(i));
                        Bullet.removeBullet(i, true);
                    } else if (Bullet.bullets.get(i).overlaps(bounds_cannon3.getBoundingRectangle())) {
                        healths.set(3, healths.get(3) - Bullet.damages.get(i));
                        Bullet.removeBullet(i, true);
                    } else if (Bullet.bullets.get(i).overlaps(bounds_cannon4.getBoundingRectangle())) {
                        healths.set(4, healths.get(4) - Bullet.damages.get(i));
                        Bullet.removeBullet(i, true);
                    } else if (Bullet.bullets.get(i).overlaps(bounds_cannon5.getBoundingRectangle())) {
                        healths.set(5, healths.get(5) - Bullet.damages.get(i));
                        Bullet.removeBullet(i, true);
                    } else if (Bullet.bullets.get(i).overlaps(bounds_cannon6.getBoundingRectangle())) {
                        healths.set(6, healths.get(6) - Bullet.damages.get(i));
                        Bullet.removeBullet(i, true);
                    } else if (Bullet.bullets.get(i).overlaps(bounds_cannon_front.getBoundingRectangle())) {
                        healths.set(8, healths.get(8) - Bullet.damages.get(i));
                        Bullet.removeBullet(i, true);
                    } else if (Bullet.bullets.get(i).overlaps(bounds_homing1.getBoundingRectangle()) || Bullet.bullets.get(i).overlaps(bounds_homing2.getBoundingRectangle())) {
                        healths.set(10, healths.get(10) - Bullet.damages.get(i));
                        Bullet.removeBullet(i, true);
                    } else if (Bullet.bullets.get(i).overlaps(bounds_body.getBoundingRectangle()) && stage2 && healths.get(7) > 0) {
                        healths.set(7, healths.get(7) - Bullet.damages.get(i));
                        Bullet.removeBullet(i, true);
                    } else if (Bullet.bullets.get(i).overlaps(bounds_cannon_front_big.getBoundingRectangle()) && stage2) {
                        healths.set(9, healths.get(9) - Bullet.damages.get(i));
                        Bullet.removeBullet(i, true);
                    }

                }
                shoot(is_paused);
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

        if(deathAnimation){
            millis2 += 3*Gdx.graphics.getDeltaTime();
            if(millis2>50){
                deathAnimation = false;
            }
        }
    }

    private void shoot(boolean is_paused){
        if (millis > 10 && !is_paused){
            int cannon = random.nextInt(9)+1;

            Rectangle bullet = new Rectangle();

            switch (cannon){
                case(1):
                    if(healths.get(1)>0) {
                        bullet.setSize(14, 14);
                        bullet.x = bounds_cannon.getX() + bounds_cannon.getBoundingRectangle().width / 2 - 7;
                        bullet.x -= MathUtils.cosDeg(bounds_cannon.getRotation()) * 1100 * Gdx.graphics.getDeltaTime();
                        bullet.y = bounds_cannon.getY() + bounds_cannon.getBoundingRectangle().height / 2 - 7;
                        bullet.y -= MathUtils.sinDeg(bounds_cannon.getRotation()) * 1100 * Gdx.graphics.getDeltaTime();
                        bullets_blue.add(bullet);
                        degrees_blue.add(bounds_cannon.getRotation());

                        play_shot_Sound(2, sound, false, 0);
                    }
                    break;
                case(2):
                    if(healths.get(2)>0) {
                        bullet.setSize(14, 14);
                        bullet.x = bounds_cannon2.getX() + bounds_cannon2.getBoundingRectangle().width / 2 - 7;
                        bullet.x -= MathUtils.cosDeg(bounds_cannon2.getRotation()) * 1100 * Gdx.graphics.getDeltaTime();
                        bullet.y = bounds_cannon2.getY() + bounds_cannon2.getBoundingRectangle().height / 2 - 7;
                        bullet.y -= MathUtils.sinDeg(bounds_cannon2.getRotation()) * 1100 * Gdx.graphics.getDeltaTime();
                        bullets_blue.add(bullet);
                        degrees_blue.add(bounds_cannon2.getRotation());

                        play_shot_Sound(2, sound, false, 0);
                    }
                    break;
                case(3):
                    if(healths.get(3)>0) {
                        bullet.setSize(14, 14);
                        bullet.x = bounds_cannon3.getX() + bounds_cannon3.getBoundingRectangle().width / 2 - 7;
                        bullet.x -= MathUtils.cosDeg(bounds_cannon3.getRotation()) * 1100 * Gdx.graphics.getDeltaTime();
                        bullet.y = bounds_cannon3.getY() + bounds_cannon3.getBoundingRectangle().height / 2 - 7;
                        bullet.y -= MathUtils.sinDeg(bounds_cannon3.getRotation()) * 1100 * Gdx.graphics.getDeltaTime();
                        bullets_blue.add(bullet);
                        degrees_blue.add(bounds_cannon3.getRotation());

                        play_shot_Sound(2, sound, false, 0);
                    }
                    break;
                case(4):
                    if(healths.get(4)>0) {
                        bullet.setSize(14, 14);
                        bullet.x = bounds_cannon4.getX() + bounds_cannon4.getBoundingRectangle().width / 2 - 7;
                        bullet.x -= MathUtils.cosDeg(bounds_cannon4.getRotation()) * 1100 * Gdx.graphics.getDeltaTime();
                        bullet.y = bounds_cannon4.getY() + bounds_cannon4.getBoundingRectangle().height / 2 - 7;
                        bullet.y -= MathUtils.sinDeg(bounds_cannon4.getRotation()) * 1100 * Gdx.graphics.getDeltaTime();
                        bullets_blue.add(bullet);
                        degrees_blue.add(bounds_cannon4.getRotation());

                        play_shot_Sound(2, sound, false, 0);
                    }
                    break;
                case(5):
                    if(healths.get(5)>0) {
                        bullet.setSize(14, 14);
                        bullet.x = bounds_cannon5.getX() + bounds_cannon5.getBoundingRectangle().width / 2 - 7;
                        bullet.x -= MathUtils.cosDeg(bounds_cannon5.getRotation()) * 1100 * Gdx.graphics.getDeltaTime();
                        bullet.y = bounds_cannon5.getY() + bounds_cannon5.getBoundingRectangle().height / 2 - 7;
                        bullet.y -= MathUtils.sinDeg(bounds_cannon5.getRotation()) * 1100 * Gdx.graphics.getDeltaTime();
                        bullets_blue.add(bullet);
                        degrees_blue.add(bounds_cannon5.getRotation());

                        play_shot_Sound(2, sound, false, 0);
                    }
                    break;
                case(6):
                    if(healths.get(6)>0) {
                        bullet.setSize(14, 14);
                        bullet.x = bounds_cannon6.getX() + bounds_cannon6.getBoundingRectangle().width / 2 - 7;
                        bullet.x -= MathUtils.cosDeg(bounds_cannon6.getRotation()) * 1100 * Gdx.graphics.getDeltaTime();
                        bullet.y = bounds_cannon6.getY() + bounds_cannon6.getBoundingRectangle().height / 2 - 7;
                        bullet.y -= MathUtils.sinDeg(bounds_cannon6.getRotation()) * 1100 * Gdx.graphics.getDeltaTime();
                        bullets_blue.add(bullet);
                        degrees_blue.add(bounds_cannon6.getRotation());

                        play_shot_Sound(2, sound, false, 0);
                    }
                    break;
                case(7): if(healths.get(8)>0) {
                    bullet.setSize(12, 12);
                    bullet.x = bounds_cannon_front.getX() + bounds_cannon_front.getBoundingRectangle().width / 2 - 3;
                    bullet.x -= MathUtils.cosDeg(bounds_cannon_front.getRotation()) * 1300 * Gdx.graphics.getDeltaTime();
                    bullet.y = bounds_cannon_front.getY() + bounds_cannon_front.getBoundingRectangle().height / 2 - 6;
                    bullet.y -= MathUtils.sinDeg(bounds_cannon_front.getRotation()) * 1300 * Gdx.graphics.getDeltaTime();
                    bullets_red.add(bullet);
                    degrees_red.add(bounds_cannon_front.getRotation());

                    play_shot_Sound(1, sound, false, 0);
                }
                    break;
                case(8): if(healths.get(9)>0 && stage2) {
                    bullet.setSize(28, 28);
                    bullet.x = bounds_cannon_front_big.getX() + bounds_cannon_front_big.getBoundingRectangle().width / 2 - 14;
                    bullet.x -= MathUtils.cosDeg(bounds_cannon_front_big.getRotation()) * 3100 * Gdx.graphics.getDeltaTime();
                    bullet.y = bounds_cannon_front_big.getY() + bounds_cannon_front_big.getBoundingRectangle().height / 2 - 14;
                    bullet.y -= MathUtils.sinDeg(bounds_cannon_front_big.getRotation()) * 3100 * Gdx.graphics.getDeltaTime();
                    bullets_red_big.add(bullet);
                    bullets_red_big_timers.add(3.5f);
                    bullets_red_big_timers_ideal.add(3.5f);

                    ParticleEffect fire = new ParticleEffect();
                    fire.load(Gdx.files.internal("particles/bullet_trail_left.p"), Gdx.files.internal("particles"));
                    fire.start();

                    fires.add(fire);

                    play_shot_Sound(4, sound, false, 0);
                }
                    break;
                case(9): if(healths.get(10)>0) {
                    bullet.setSize(54, 12);
                    bullet.x = bounds_homing2.getX() + bounds_homing2.getBoundingRectangle().width / 2 - 7;
                    bullet.x -= MathUtils.cosDeg(bounds_homing2.getRotation()) * 2100 * Gdx.graphics.getDeltaTime();
                    bullet.y = bounds_homing2.getY() + bounds_homing2.getBoundingRectangle().height / 2 - 7;
                    bullet.y -= MathUtils.sinDeg(bounds_homing2.getRotation()) * 2100 * Gdx.graphics.getDeltaTime();
                    bullets_red_long.add(bullet);
                    degrees_red_long.add(bounds_homing2.getRotation());

                    play_shot_Sound(3, sound, false, 0);
                }
                    break;
            }
            millis = 0;
        }
        millis=millis+60*Gdx.graphics.getDeltaTime();
    }

    private void removeBullet(int i, int type){
        switch (type){
            case(1):
                bullets_blue.removeIndex(i);
                degrees_blue.removeIndex(i);
                break;
            case(2):
                bullets_red.removeIndex(i);
                degrees_red.removeIndex(i);
                break;
            case(3):
                bullets_red_big.removeIndex(i);
                bullets_red_big_timers.removeIndex(i);
                bullets_red_big_timers_ideal.removeIndex(i);
                fires.get(i).dispose();
                fires.removeIndex(i);
                break;
            case(4):
                bullets_red_long.removeIndex(i);
                degrees_red_long.removeIndex(i);
                break;
        }
    }

    public void dispose(){
        is_spawned = false;
        stage2 = false;
        bullets_blue.clear();
        bullets_red.clear();
        bullets_red_big.clear();
        bullets_red_long.clear();
        healths.clear();
        degrees_blue.clear();
        degrees_red.clear();
        bullets_red_big_timers.clear();
        bullets_red_big_timers_ideal.clear();
        degrees_red_long.clear();
        explodedCannons.clear();

        shot.dispose();
        shot2.dispose();
        shot3.dispose();
        shot4.dispose();
        explosion.dispose();

        for(int i3 = 0; i3 < fires.size; i3 ++){
            fires.get(i3).dispose();
            fires.removeIndex(i3);
        }

        for(int i3 = 0; i3 < explosions.size; i3 ++){
            explosions.get(i3).dispose();
            explosions.removeIndex(i3);
        }

    }

    private void play_shot_Sound(int shotType, boolean sound, boolean explosion, int cannon) {
            if (!explosion) {
                if (sound) {
                    switch (shotType) {
                        case (1):
                            shot.play(MenuScreen.SoundVolume/100);
                            break;
                        case (2):
                            shot2.play(MenuScreen.SoundVolume/100);
                            break;
                        case (3):
                            shot3.play(MenuScreen.SoundVolume/100);
                            break;
                        case (4):
                            shot4.play(MenuScreen.SoundVolume/100);
                            break;
                    }
                }
            } else {
                if (sound) {
                    this.explosion.play(MenuScreen.SoundVolume/100);
                }
                switch (shotType) {
                    case (1):
                        ParticleEffect explosionEffect = new ParticleEffect();
                        explosionEffect.load(Gdx.files.internal("particles/explosion.p"), Gdx.files.internal("particles"));
                        switch (cannon) {
                            case (1):
                                explosionEffect.setPosition(bounds_cannon.getX(), bounds_cannon.getY());
                                break;
                            case (2):
                                explosionEffect.setPosition(bounds_cannon2.getX(), bounds_cannon2.getY());
                                break;
                            case (3):
                                explosionEffect.setPosition(bounds_cannon3.getX(), bounds_cannon3.getY());
                                break;
                            case (4):
                                explosionEffect.setPosition(bounds_cannon4.getX(), bounds_cannon4.getY());
                                break;
                            case (5):
                                explosionEffect.setPosition(bounds_cannon5.getX(), bounds_cannon5.getY());
                                break;
                            case (6):
                                explosionEffect.setPosition(bounds_cannon6.getX(), bounds_cannon6.getY());
                                break;
                            case (7):
                                explosionEffect.setPosition(bounds_cannon_front.getX(), bounds_cannon_front.getY());
                                break;
                            case (8):
                                explosionEffect.setPosition(bounds_homing1.getX(), bounds_homing1.getY());
                                break;
                            case (9):
                                explosionEffect.setPosition(bounds_cannon_front_big.getX(), bounds_cannon_front_big.getY());
                                break;
                        }
                        explosionEffect.start();
                        explosions.add(explosionEffect);
                        break;
                    case (2):
                        for (int i = 0; i < 5; i++) {
                            ParticleEffect explosionEffect2 = new ParticleEffect();
                            explosionEffect2.load(Gdx.files.internal("particles/explosion2.p"), Gdx.files.internal("particles"));
                            explosionEffect2.setPosition(posX + i * 100 + 100, posY + i * 10);
                            explosionEffect2.start();
                            explosions.add(explosionEffect2);
                        }
                        break;
                }
            }
    }

    public void postConstruct(Bonus bonus){
        this.bonus = bonus;
    }

}
