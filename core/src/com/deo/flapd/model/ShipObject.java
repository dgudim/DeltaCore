package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.math.Polygon;
import com.deo.flapd.view.MenuScreen;

abstract class ShipObject {

    private Polygon bounds;
    private Sprite ship;
    private Sprite shield;

    private ParticleEffect fire, fire2;

    private static float red;
    private static float green;
    private static float blue;
    private static float red2;
    private static float green2;
    private static float blue2;

    private boolean exploded;

    private Sound explosion;

    private boolean sound;

    ShipObject(Texture shipTexture, Texture ShieldTexture, float x, float y, float width, float height) {
        ship = new Sprite(shipTexture);
        shield = new Sprite(ShieldTexture);

        bounds = new Polygon(new float[]{0f, 0f, width, 0f, width, height, 0f, height});
        bounds.setPosition(x, y);
        ship.setOrigin(width / 2f, height / 2f);
        shield.setOrigin((width+30) / 2f, (height+30) / 2f);

        ship.setSize(width, height);
        ship.setPosition(x, y);
        shield.setSize(width+30, height+30);
        shield.setPosition(x, y-10);

        fire = new ParticleEffect();
        fire.load(Gdx.files.internal("particles/fire_engileleft_red_green.p"), Gdx.files.internal("particles"));
        fire.start();

        fire2 = new ParticleEffect();
        fire2.load(Gdx.files.internal("particles/fire_engileleft_red_green.p"), Gdx.files.internal("particles"));
        fire2.start();

        red = 1;
        green = 1;
        blue = 1;
        red2 = 1;
        green2 = 1;
        blue2 = 1;
        exploded = false;
        sound = MenuScreen.Sound;

        explosion = Gdx.audio.newSound(Gdx.files.internal("music/explosion.ogg"));
    }


    public void draw(SpriteBatch batch, boolean is_paused){
        if(!exploded) {
            ship.setPosition(bounds.getX(), bounds.getY());
            ship.setRotation(bounds.getRotation());
            fire.setPosition(bounds.getX() + 10, bounds.getY() + 18);
            fire.draw(batch);
            fire2.setPosition(bounds.getX() + 4, bounds.getY() + 40);
            fire2.draw(batch);

            if (!is_paused) {
                fire.update(Gdx.graphics.getDeltaTime());
                fire2.update(Gdx.graphics.getDeltaTime());
            } else {
                fire.update(0);
                fire2.update(0);
            }

            ship.setColor(red, green, blue, 1);

            if (!is_paused) {
                if (red < 1) {
                    red = red + 0.05f;
                }
                if (green < 1) {
                    green = green + 0.05f;
                }
                if (blue < 1) {
                    blue = blue + 0.05f;
                }
            }

            ship.draw(batch);
        }else{
            bounds.setPosition(-100, -100);
        }
    }

    public void drawShield(SpriteBatch batch, boolean is_paused, float alpha){
        if(!exploded) {
            shield.setPosition(bounds.getX() - 20, bounds.getY() - 15);
            shield.setRotation(bounds.getRotation());
            shield.setColor(red2, green2, blue2, alpha);

            if (!is_paused) {
                if (red2 < 1) {
                    red2 = red2 + 0.05f;
                }
                if (green2 < 1) {
                    green2 = green2 + 0.05f;
                }
                if (blue2 < 1) {
                    blue2 = blue2 + 0.05f;
                }
            }

            shield.draw(batch);
        }
    }

    public Polygon getBounds(){
        return bounds;
    }

    public void dispose(){
        fire.dispose();
        fire2.dispose();
        explosion.dispose();
    }

    public static void set_color(float red1, float green1, float blue1, boolean shield){
        if(!shield) {
            red = red1;
            green = green1;
            blue = blue1;
        }else{
            red2 = red1;
            green2 = green1;
            blue2 = blue1;
        }
    }

    public void explode(){
        exploded = true;
        if(sound){
            explosion.play(MenuScreen.SoundVolume/100);
        }
    }

    public boolean isExploded(){
        return exploded;
    }

}
