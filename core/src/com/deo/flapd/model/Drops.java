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
import com.deo.flapd.utils.DUtils;

import java.util.Random;

public class Drops {

    private Sprite ironPlate, bolt, ore, glassShard, coloringCrystal, ironPlateMK2, prism, cable, fuelCell, energyCell, fuelCellMK2, laserEmitter, warpShard, warpShardMK2, warpOre, warpCore, gun, orangeCrystal, cyanCrystal, purpleCrystal, greenCrystal, redCrystal, sunCore, energyCrystal, memoryCard, screenCard, bulkCard, bulkCardMK2, processor, processorMK2, processorMK3, storageCell, storageCellMK2, craftingCard, aiCard, circuitBoard, advancedChip, aiChip, coolingUnit;
    private static Array<Float> timers;
    private static Array <Rectangle> drops;
    private static Array <Float> degrees;
    private static Array <Integer> types;
    private static float width, height;
    private static Random random;
    private float uiScaling;
    private static int currentType;

    private Preferences prefs;

    public Drops(AssetManager assetManager, float width, float height, float uiScaling) {
        coloringCrystal = new Sprite((Texture)assetManager.get("items/crystal.png"));
        redCrystal = new Sprite((Texture)assetManager.get("items/redCrystal.png"));
        warpShard = new Sprite((Texture)assetManager.get("items/bonus_warp.png"));
        laserEmitter = new Sprite((Texture)assetManager.get("items/bonus_laser.png"));
        circuitBoard = new Sprite((Texture)assetManager.get("items/Circuit_Board.png"));
        sunCore = new Sprite((Texture)assetManager.get("items/core_yellow.png"));
        ore = new Sprite((Texture)assetManager.get("items/ore.png"));
        prism = new Sprite((Texture)assetManager.get("items/prism.png"));

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
    public static void drop(Rectangle originEnemy, float scale, float timer, int rarity){
        drop(originEnemy.getX() + originEnemy.width / 2 - width/2, originEnemy.getY() + originEnemy.height / 2 - height/2, scale, timer, rarity);
    }

    public static void drop(float x, float y, float scale, float timer, int rarity){
            Rectangle drop = new Rectangle();

            drop.x = x;
            drop.y = y;

            drop.setSize(width*scale, height*scale);

            drops.add(drop);
            timers.add(timer*(random.nextFloat()+0.2f));
            degrees.add(random.nextFloat()*360);

            currentType = 0;
            if(random.nextInt(3) > 1) {
            if (random.nextInt(11) > 5 - rarity) {
            if (random.nextInt(11) > 5 - rarity) {
            if (random.nextInt(11) > 5 - rarity) {
            if (random.nextInt(11) > 5 - rarity) {
            if (random.nextInt(11) > 5 - rarity) {
            if (random.nextInt(11) > 5 - rarity) {
                if(random.nextInt(10)>5) {
                    currentType = DUtils.getRandomInRange(34, 39);
                }
            }else{
                if(random.nextInt(10)>5){
                    currentType = DUtils.getRandomInRange(28 ,29);
                }else if(random.nextInt(10)>3){
                    currentType = DUtils.getRandomInRange(30, 31);
                }else if(random.nextInt(10)>1){
                    currentType = DUtils.getRandomInRange(32 ,33);
                }
            }
            }else{
                if(random.nextInt(10)>5){
                    currentType = DUtils.getRandomInRange(23, 24);
                }else if(random.nextInt(10)>3){
                    currentType = DUtils.getRandomInRange(25 ,27);
                }
            }
            }else{
                if(random.nextInt(10)>5){
                    currentType = DUtils.getRandomInRange(17 ,21);
                }else if(random.nextInt(10)>3){
                    currentType = 22;
                }
            }
            }else{
                if(random.nextInt(10)>5){
                    currentType = DUtils.getRandomInRange(13 ,14);
                }else if(random.nextInt(10)>3){
                    currentType = DUtils.getRandomInRange(15 ,16);
                }
            }
            }else{
                if(random.nextInt(10)>5){
                    currentType = DUtils.getRandomInRange(9 ,10);
                }else if(random.nextInt(10)>3){
                    currentType = DUtils.getRandomInRange(11 ,12);
                }
            }
            }else{
                if(random.nextInt(10)>5){
                    currentType = DUtils.getRandomInRange(1 ,5);
                }else if(random.nextInt(10)>3){
                    currentType = DUtils.getRandomInRange(6 ,8);
                }
                }
            }
            types.add(currentType);
    }

    public void draw(SpriteBatch batch, boolean is_paused) {
        /*
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
                    this.ore.setPosition(drop.x, drop.y);
                    this.ore.setSize(drop.width, drop.height);
                    this.ore.setOrigin(drop.width / 2f, drop.height / 2f);
                    this.ore.draw(batch);
                    break;
                case(3):
                    this.board.setPosition(drop.x, drop.y);
                    this.board.setSize(drop.width, drop.height);
                    this.board.setOrigin(drop.width / 2f, drop.height / 2f);
                    this.board.draw(batch);
                    break;
                case(4):
                    this.laser.setPosition(drop.x, drop.y);
                    this.laser.setSize(drop.width, drop.height);
                    this.laser.setOrigin(drop.width / 2f, drop.height / 2f);
                    this.laser.draw(batch);
                    break;
                case(5):
                    this.redCrystal.setPosition(drop.x, drop.y);
                    this.redCrystal.setSize(drop.width, drop.height);
                    this.redCrystal.setOrigin(drop.width / 2f, drop.height / 2f);
                    this.redCrystal.draw(batch);
                    break;
                case(6):
                    this.prism.setPosition(drop.x, drop.y);
                    this.prism.setSize(drop.width, drop.height);
                    this.prism.setOrigin(drop.width / 2f, drop.height / 2f);
                    this.prism.draw(batch);
                    break;
                case(7):
                    this.warp.setPosition(drop.x, drop.y);
                    this.warp.setSize(drop.width, drop.height);
                    this.warp.setOrigin(drop.width / 2f, drop.height / 2f);
                    this.warp.draw(batch);
                    break;
                case(8):
                    this.sunCore.setPosition(drop.x, drop.y);
                    this.sunCore.setSize(drop.width, drop.height);
                    this.sunCore.setOrigin(drop.width / 2f, drop.height / 2f);
                    this.sunCore.draw(batch);
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
                    pos2.set(344+drop.width/2-400*(uiScaling-1), 403+drop.height/2-20*(uiScaling-1));
                    pos1.lerp(pos2, 0.05f);

                    drop.x = pos1.x;
                    drop.y = pos1.y;
                }
                timer = timer - 1 * Gdx.graphics.getDeltaTime();
                timers.set(i, timer);
            }
            if(drop.y - drop.height/2 > 403-20*(uiScaling-1)-2*uiScaling && drop.x - drop.width/2> 344-400*(uiScaling-1)-10*uiScaling && drop.x - drop.width/2< 344-400*(uiScaling-1)+10*uiScaling){
                removeDrop(i);
            }
        }
         */
    }

    private void removeDrop(int i){
        switch (types.get(i)){
            case(1):
                prefs.putInteger("crystal", prefs.getInteger("crystal")+1);
                prefs.flush();
                break;
            case(2):
                prefs.putInteger("ore", prefs.getInteger("ore")+1);
                prefs.flush();
                break;
            case(3):
                prefs.putInteger("board", prefs.getInteger("board")+1);
                prefs.flush();
                break;
            case(4):
                prefs.putInteger("laser", prefs.getInteger("laser")+1);
                prefs.flush();
                break;
            case(5):
                prefs.putInteger("prism", prefs.getInteger("prism")+1);
                prefs.flush();
                break;
            case(6):
                prefs.putInteger("warp", prefs.getInteger("warp")+1);
                prefs.flush();
                break;
            case(7):
                prefs.putInteger("core", prefs.getInteger("core")+1);
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
