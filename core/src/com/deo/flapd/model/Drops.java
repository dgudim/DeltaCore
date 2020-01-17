package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class Drops {

    private Sprite crystal, warp, laser, board;
    private static Array<Float> timers;
    private static Array <Rectangle> drops;
    private static Array <Float> degrees;
    private static Array <Integer> types;
    private static float width, height;
    private static Random random;
    private float uiScaling;

    private Preferences prefs;

    public Drops(AssetManager assetManager, float width, float height, float uiScaling) {
        crystal = new Sprite((Texture)assetManager.get("crystal.png"));
        warp = new Sprite((Texture)assetManager.get("bonus_warp.png"));
        laser = new Sprite((Texture)assetManager.get("bonus_laser.png"));
        board = new Sprite((Texture)assetManager.get("Circuit_Board.png"));

        prefs = Gdx.app.getPreferences("Preferences");

        timers = new Array<>();
        drops = new Array<>();
        degrees = new Array<>();
        types = new Array<>();
        this.width = width;
        this.height = height;
        this.uiScaling = uiScaling;
        random = new Random();
    }
    public static void drop(Rectangle originEnemy, float scale, float timer, int category){
        drop(originEnemy.getX() + originEnemy.width / 2 - width/2, originEnemy.getY() + originEnemy.height / 2 - height/2, scale, timer, category);
    }

    public static void drop(float x, float y, float scale, float timer, int category){
            Rectangle drop = new Rectangle();

            drop.x = x;
            drop.y = y;

            drop.setSize(width*scale, height*scale);

            drops.add(drop);
            timers.add(timer*(random.nextFloat()+0.2f));
            degrees.add(random.nextFloat()*360);
            switch (category){
                case(1):
                    if(random.nextInt(5) == 3) {
                        if (random.nextBoolean()) {
                            types.add(1);
                        } else if (random.nextBoolean()) {
                            types.add(2);
                        } else {
                            types.add(0);
                        }
                    }else{
                        types.add(0);
                    }
                    break;
                case(2):
                    if(random.nextInt(5) == 3) {
                        if (random.nextBoolean()) {
                            types.add(3);
                        } else if (random.nextBoolean()) {
                            types.add(4);
                        } else {
                            types.add(0);
                        }
                    }else{
                        types.add(0);
                    }
                    break;
                case(3):
                    break;
            }
    }
    public void draw(SpriteBatch batch, boolean is_paused) {
        for (int i = 0; i < drops.size; i++) {
            Rectangle drop = drops.get(i);
            float degree = degrees.get(i);
            float timer = timers.get(i);

            switch (types.get(i)){
                case(1):
                    this.crystal.setPosition(drop.x, drop.y);
                    this.crystal.setSize(drop.width, drop.height);
                    this.crystal.setOrigin(drop.width / 2f, drop.height / 2f);
                    this.crystal.draw(batch);
                    break;
                case(2):
                    this.board.setPosition(drop.x, drop.y);
                    this.board.setSize(drop.width, drop.height);
                    this.board.setOrigin(drop.width / 2f, drop.height / 2f);
                    this.board.draw(batch);
                    break;
                case(3):
                    this.laser.setPosition(drop.x, drop.y);
                    this.laser.setSize(drop.width, drop.height);
                    this.laser.setOrigin(drop.width / 2f, drop.height / 2f);
                    this.laser.draw(batch);
                    break;
                case(4):
                    this.warp.setPosition(drop.x, drop.y);
                    this.warp.setSize(drop.width, drop.height);
                    this.warp.setOrigin(drop.width / 2f, drop.height / 2f);
                    this.warp.draw(batch);
                    break;
            }
            if(!is_paused){
                if(timer > 0) {
                    drop.x -= MathUtils.cosDeg(degree) * timer * 30 * Gdx.graphics.getDeltaTime();
                    drop.y -= MathUtils.sinDeg(degree) * timer * 30 * Gdx.graphics.getDeltaTime();
                }else{
                    Vector2 pos1 = new Vector2();
                    pos1.set(drop.x, drop.y);
                    Vector2 pos2 = new Vector2();
                    pos2.set(344-400*(uiScaling-1), 403-20*(uiScaling-1));
                    pos1.lerp(pos2, 0.05f);

                    drop.x = pos1.x;
                    drop.y = pos1.y;
                }
                timer = timer - 1 * Gdx.graphics.getDeltaTime();
                timers.set(i, timer);
            }
            if(drop.y > 403-20*(uiScaling-1)-2*uiScaling && drop.x > 344-400*(uiScaling-1)-10*uiScaling && drop.x < 344-400*(uiScaling-1)+10*uiScaling){
                removeDrop(i);
            }
        }
    }

    private void removeDrop(int i){
        switch (types.get(i)){
            case(1):
                prefs.putInteger("crystal", prefs.getInteger("crystal")+1);
                prefs.flush();
                break;
            case(2):
                prefs.putInteger("board", prefs.getInteger("board")+1);
                prefs.flush();
                break;
            case(3):
                prefs.putInteger("laser", prefs.getInteger("laser")+1);
                prefs.flush();
                break;
            case(4):
                prefs.putInteger("warp", prefs.getInteger("warp")+1);
                prefs.flush();
                break;
        }
        timers.removeIndex(i);
        drops.removeIndex(i);
        degrees.removeIndex(i);
        types.removeIndex(i);
    }

    public void dispose(){
        timers.clear();
        drops.clear();
        degrees.clear();
        types.clear();
    }
}
