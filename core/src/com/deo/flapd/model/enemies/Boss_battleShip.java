package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class Boss_battleShip {

    private Sprite main;
    private Sprite cannon_small;
    private Sprite cannon_small2;
    private Sprite cannon_stage2;
    private Sprite cannon_homing_part1;
    private Sprite cannon_homing_part2;
    private Sprite bullet, bullet2, bullet3;

    private Array <Rectangle> bullets_blue;
    private Array <Rectangle> bullets_red;
    private Array <Rectangle> bullets_red_big;
    private Array <Float> healths;

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

    private boolean animation, animation2;

    private float offset;

    private boolean is_spawned;

    private float posX, posY;

    private Polygon shipBounds, bounds_body, bounds_cannon, bounds_cannon2, bounds_cannon3, bounds_cannon4, bounds_cannon5, bounds_cannon6, bounds_cannon_front, bounds_cannon_front_big, bounds_homing1, bounds_homing2;

    public Boss_battleShip(AssetManager assetManager, float posX, float posY, Polygon shipBounds){

        this.shipBounds = shipBounds;

        main = new Sprite((Texture)assetManager.get("boss_ship/boss.png"));
        cannon_small = new Sprite((Texture)assetManager.get("boss_ship/cannon2.png"));
        cannon_small2 = new Sprite((Texture)assetManager.get("boss_ship/cannon1.png"));
        cannon_stage2 = new Sprite((Texture)assetManager.get("boss_ship/bigCannon.png"));
        cannon_homing_part1 = new Sprite((Texture)assetManager.get("boss_ship/upperCannon_part1.png"));
        cannon_homing_part2 = new Sprite((Texture)assetManager.get("boss_ship/upperCannon_part2.png"));
        bullet = new Sprite((Texture)assetManager.get("boss_ship/bullet_blue.png"));
        bullet2 = new Sprite((Texture)assetManager.get("boss_ship/bullet_red.png"));
        bullet3 = new Sprite((Texture)assetManager.get("boss_ship/bullet_red_thick.png"));

        bullets_blue = new Array<>();
        bullets_red = new Array<>();
        bullets_red_big = new Array<>();
        healths = new Array<>();

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
        bullet.setOrigin(14, 14);
        bullet2.setOrigin(26, 6);
        bullet3.setOrigin(14, 14);

        is_spawned = false;
        animation = true;
        animation2 = false;
        offset = 0;

        this.posX = posX;
        this.posY = posY;

        random = new Random();

        healths.setSize(11);
        //cannons
        healths.set(1, 1000f);
        healths.set(2, 1000f);
        healths.set(3, 1000f);
        healths.set(4, 1000f);
        healths.set(5, 1000f);
        healths.set(6, 1000f);
        //body
        healths.set(7, 7000f);
        //big Gun
        healths.set(8, 3000f);
        //stage2 Gun
        healths.set(9, 4000f);
        //homing Gun
        healths.set(10, 2000f);

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

        health_cannon1 = new ProgressBar(0, 1000, 0.01f, false, healthBarStyle);
        health_cannon2 = new ProgressBar(0, 1000, 0.01f, false, healthBarStyle);
        health_cannon3 = new ProgressBar(0, 1000, 0.01f, false, healthBarStyle);
        health_cannon4 = new ProgressBar(0, 1000, 0.01f, false, healthBarStyle);
        health_cannon5 = new ProgressBar(0, 1000, 0.01f, false, healthBarStyle);
        health_cannon6 = new ProgressBar(0, 1000, 0.01f, false, healthBarStyle);
        health_body = new ProgressBar(0, 7000, 0.01f, false, healthBarStyle2);
        health_cannon_front = new ProgressBar(0, 3000, 0.01f, false, healthBarStyle);
        health_cannon_homing = new ProgressBar(0, 2000, 0.01f, false, healthBarStyle);
        health_cannon_stage2 = new ProgressBar(0, 4000, 0.01f, false, healthBarStyle);

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
    }

    public void Spawn(){
        is_spawned = true;
        stage2 = false;
    }

    public void draw(SpriteBatch batch, boolean is_paused) {

        if (is_spawned) {
            bounds_body.setPosition(posX, posY);
            bounds_cannon.setPosition(posX + 224, posY + 40);
            bounds_cannon2.setPosition(posX + 256, posY + 40);
            bounds_cannon3.setPosition(posX + 288, posY + 40);
            bounds_cannon4.setPosition(posX + 376, posY + 40);
            bounds_cannon5.setPosition(posX + 408, posY + 40);
            bounds_cannon6.setPosition(posX + 440, posY + 40);
            bounds_cannon_front.setPosition(posX + 60, posY + 58);
            bounds_homing1.setPosition(posX + 94, posY + 116);
            bounds_homing2.setPosition(posX + 67, posY + 126);
            bounds_cannon_front_big.setPosition(posX+40, posY + 48);

            bounds_cannon.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon.getY()-shipBounds.getY(), bounds_cannon.getX()-shipBounds.getX()));
            bounds_cannon2.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon2.getY()-shipBounds.getY(), bounds_cannon2.getX()-shipBounds.getX()));
            bounds_cannon3.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon3.getY()-shipBounds.getY(), bounds_cannon3.getX()-shipBounds.getX()));
            bounds_cannon4.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon4.getY()-shipBounds.getY(), bounds_cannon4.getX()-shipBounds.getX()));
            bounds_cannon5.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon5.getY()-shipBounds.getY(), bounds_cannon5.getX()-shipBounds.getX()));
            bounds_cannon6.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon6.getY()-shipBounds.getY(), bounds_cannon6.getX()-shipBounds.getX()));
            bounds_cannon_front.setRotation(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon_front.getY()-shipBounds.getY(), bounds_cannon_front.getX()-shipBounds.getX()));
            bounds_cannon_front_big.setRotation(MathUtils.clamp(MathUtils.radiansToDegrees*MathUtils.atan2(bounds_cannon_front_big.getY()-shipBounds.getY(), bounds_cannon_front_big.getX()-shipBounds.getX()), -20, 20));

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

            if(healths.get(9)>0) {
                health_cannon_stage2.draw(batch, 1);
                health_cannon_stage2.act(Gdx.graphics.getDeltaTime());

                cannon_stage2.setPosition(bounds_cannon_front_big.getX(), bounds_cannon_front_big.getY());
                cannon_stage2.setRotation(bounds_cannon_front_big.getRotation());
                cannon_stage2.draw(batch);
            }

            if(healths.get(7)>0) {
                main.setPosition(bounds_body.getX(), bounds_body.getY());
                main.setRotation(bounds_body.getRotation());
                main.draw(batch);

                if(stage2) {
                    health_body.draw(batch, 1);
                    health_body.act(Gdx.graphics.getDeltaTime());
                }

            }

            if(healths.get(1)>0) {
                cannon_small.setPosition(bounds_cannon.getX(), bounds_cannon.getY());
                cannon_small.setRotation(bounds_cannon.getRotation());
                cannon_small.draw(batch);

                health_cannon1.draw(batch, 1);
                health_cannon1.act(Gdx.graphics.getDeltaTime());
            }

            if(healths.get(2)>0) {
                cannon_small.setPosition(bounds_cannon2.getX(), bounds_cannon2.getY());
                cannon_small.setRotation(bounds_cannon2.getRotation());
                cannon_small.draw(batch);

                health_cannon2.draw(batch, 1);
                health_cannon2.act(Gdx.graphics.getDeltaTime());
            }

            if(healths.get(3)>0) {
                cannon_small.setPosition(bounds_cannon3.getX(), bounds_cannon3.getY());
                cannon_small.setRotation(bounds_cannon3.getRotation());
                cannon_small.draw(batch);

                health_cannon3.draw(batch, 1);
                health_cannon3.act(Gdx.graphics.getDeltaTime());
            }

            if(healths.get(4)>0) {
                cannon_small.setPosition(bounds_cannon4.getX(), bounds_cannon4.getY());
                cannon_small.setRotation(bounds_cannon4.getRotation());
                cannon_small.draw(batch);

                health_cannon4.draw(batch, 1);
                health_cannon4.act(Gdx.graphics.getDeltaTime());
            }

            if(healths.get(5)>0) {
                cannon_small.setPosition(bounds_cannon5.getX(), bounds_cannon5.getY());
                cannon_small.setRotation(bounds_cannon5.getRotation());
                cannon_small.draw(batch);

                health_cannon5.draw(batch, 1);
                health_cannon5.act(Gdx.graphics.getDeltaTime());
            }

            if(healths.get(6)>0) {
                cannon_small.setPosition(bounds_cannon6.getX(), bounds_cannon6.getY());
                cannon_small.setRotation(bounds_cannon6.getRotation());
                cannon_small.draw(batch);

                health_cannon6.draw(batch, 1);
                health_cannon6.act(Gdx.graphics.getDeltaTime());
            }

            if(healths.get(8)>0) {
                cannon_small2.setPosition(bounds_cannon_front.getX(), bounds_cannon_front.getY());
                cannon_small2.setRotation(bounds_cannon_front.getRotation());
                cannon_small2.draw(batch);

                health_cannon_front.draw(batch, 1);
                health_cannon_front.act(Gdx.graphics.getDeltaTime());
            }

            if(healths.get(10)>0) {
                cannon_homing_part2.setPosition(bounds_homing2.getX(), bounds_homing2.getY());
                cannon_homing_part2.setRotation(bounds_homing2.getRotation());
                cannon_homing_part2.draw(batch);

                cannon_homing_part1.setPosition(bounds_homing1.getX(), bounds_homing1.getY());
                cannon_homing_part1.setRotation(bounds_homing1.getRotation());
                cannon_homing_part1.draw(batch);

                health_cannon_homing.draw(batch, 1);
                health_cannon_homing.act(Gdx.graphics.getDeltaTime());
            }

            if(!is_paused){

                if(animation){
                    posX--;
                    if(posX<245){
                        animation = false;
                    }
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

                posY = posY + offset;

                if(bounds_body.getBoundingRectangle().overlaps(shipBounds.getBoundingRectangle())){
                    if(shipBounds.getX()+76.8f>bounds_body.getX() && shipBounds.getX()+76.8f<bounds_body.getX()+30){
                        shipBounds.setPosition(bounds_body.getX()-76.8f, shipBounds.getY());
                    }
                    if(shipBounds.getY()+57.6f>bounds_body.getY() && shipBounds.getY()+57.6f<bounds_body.getY()+30){
                        shipBounds.setPosition(shipBounds.getX(), bounds_body.getY()-57.6f);
                    }
                    if(shipBounds.getY()<bounds_body.getY()+172 && shipBounds.getY()>bounds_body.getY()+142){
                        shipBounds.setPosition(shipBounds.getX(), bounds_body.getY()+172);
                    }
                }
            }

            for (int i = 0; i<bullets_blue.size; i++){

                Rectangle bullet = bullets_blue.get(i);

                this.bullet.setPosition(bullet.x, bullet.y);
                this.bullet.setSize(bullet.width, bullet.height);
                this.bullet.setOrigin(bullet.width / 2f, bullet.height / 2f);
                this.bullet.draw(batch);

                if(!is_paused){
                    bullet.x -= 300 * Gdx.graphics.getDeltaTime();
                    bullet.y -= 300 * Gdx.graphics.getDeltaTime();
                }
            }

            for (int i = 0; i<bullets_red.size; i++){

                Rectangle bullet = bullets_red.get(i);

                this.bullet2.setPosition(bullet.x, bullet.y);
                this.bullet2.setSize(bullet.width, bullet.height);
                this.bullet2.setOrigin(bullet.width / 2f, bullet.height / 2f);
                this.bullet2.draw(batch);

                if(!is_paused){
                    bullet.x -= 300 * Gdx.graphics.getDeltaTime();
                    bullet.y -= 300 * Gdx.graphics.getDeltaTime();
                }
            }

            for (int i = 0; i<bullets_red_big.size; i++){

                Rectangle bullet = bullets_red_big.get(i);

                this.bullet3.setPosition(bullet.x, bullet.y);
                this.bullet3.setSize(bullet.width, bullet.height);
                this.bullet3.setOrigin(bullet.width / 2f, bullet.height / 2f);
                this.bullet3.draw(batch);

                if(!is_paused){
                    bullet.x -= 300 * Gdx.graphics.getDeltaTime();
                    bullet.y -= 300 * Gdx.graphics.getDeltaTime();
                }
            }
            shoot();
        }
    }

    public void shoot(){
        if (!stage2){
            int cannon = random.nextInt(6)+1;

            Rectangle bullet = new Rectangle();

            switch (cannon){
                case(1): bullet.setSize(28, 28);
                    bullet.x = bounds_cannon.getX() + bounds_cannon.getBoundingRectangle().width/2 - 14;
                    bullet.y = bounds_cannon.getY() + bounds_cannon.getBoundingRectangle().height/2 - 14;
                    bullets_blue.add(bullet);
                    break;
                case(2):bullet.setSize(28, 28);
                    bullet.x = bounds_cannon2.getX() + bounds_cannon2.getBoundingRectangle().width/2 - 14;
                    bullet.y = bounds_cannon2.getY() + bounds_cannon2.getBoundingRectangle().height/2 - 14;
                    bullets_blue.add(bullet);
                    break;
                case(3):
                    bullet.setSize(28, 28);
                    bullet.x = bounds_cannon3.getX() + bounds_cannon3.getBoundingRectangle().width/2 - 14;
                    bullet.y = bounds_cannon3.getY() + bounds_cannon3.getBoundingRectangle().height/2 - 14;
                    bullets_blue.add(bullet);
                    break;
                case(4):
                    bullet.setSize(28, 28);
                    bullet.x = bounds_cannon4.getX() + bounds_cannon4.getBoundingRectangle().width/2 - 14;
                    bullet.y = bounds_cannon4.getY() + bounds_cannon4.getBoundingRectangle().height/2 - 14;
                    bullets_blue.add(bullet);
                    break;
                case(5):
                    bullet.setSize(28, 28);
                    bullet.x = bounds_cannon5.getX() + bounds_cannon5.getBoundingRectangle().width/2 - 14;
                    bullet.y = bounds_cannon5.getY() + bounds_cannon5.getBoundingRectangle().height/2 - 14;
                    bullets_blue.add(bullet);
                    break;
                case(6):
                    bullet.setSize(28, 28);
                    bullet.x = bounds_cannon6.getX() + bounds_cannon6.getBoundingRectangle().width/2 - 14;
                    bullet.y = bounds_cannon6.getY() + bounds_cannon6.getBoundingRectangle().height/2 - 14;
                    bullets_blue.add(bullet);
                    break;
            }
        }
    }
}
