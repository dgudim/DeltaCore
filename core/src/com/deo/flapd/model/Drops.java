package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class Drops {

    private Sprite ironPlate, bolt, ore, glassShard, coloringCrystal, cog, warpShard, warpShardMK2, warpShardMK3, wire, energyCell, coreShard, redCrystal, ironShard, plastic, rubber, craftingCard;
    private static Array<Float> timers;
    private static Array <Rectangle> drops;
    private static Array <Float> degrees;
    private static Array <Integer> types;
    private static float maxSize;
    private static Random random;
    private static int currentType;
    private float uiScaling;
    private TextureAtlas itemAtlas;

    public Drops(AssetManager assetManager, float maxSize, float uiScaling) {

        this.uiScaling = uiScaling;

        itemAtlas = assetManager.get("items/items.atlas");

        coloringCrystal = new Sprite(itemAtlas.findRegion("crystal"));
        redCrystal = new Sprite(itemAtlas.findRegion("redCrystal"));
        warpShard = new Sprite(itemAtlas.findRegion("bonus_warp"));
        ore = new Sprite(itemAtlas.findRegion("ore"));
        ironPlate = new Sprite(itemAtlas.findRegion("ironPlate"));
        bolt = new Sprite(itemAtlas.findRegion("bolt"));
        glassShard = new Sprite(itemAtlas.findRegion("glassShard"));
        energyCell = new Sprite(itemAtlas.findRegion("energyCell"));
        warpShardMK2 = new Sprite(itemAtlas.findRegion("bonus_warp2"));
        warpShardMK3 = new Sprite(itemAtlas.findRegion("bonus_warp3"));
        cog = new Sprite(itemAtlas.findRegion("cog"));
        wire = new Sprite(itemAtlas.findRegion("wire"));
        coreShard = new Sprite(itemAtlas.findRegion("fragment_core"));
        plastic = new Sprite(itemAtlas.findRegion("plastic"));
        ironShard = new Sprite(itemAtlas.findRegion("ironShard"));
        rubber = new Sprite(itemAtlas.findRegion("rubber"));
        craftingCard = new Sprite(itemAtlas.findRegion("craftingCard"));

        timers = new Array<>();
        drops = new Array<>();
        degrees = new Array<>();
        types = new Array<>();
        Drops.maxSize = maxSize;
        random = new Random();

        coloringCrystal.setScale(maxSize/Math.max(coloringCrystal.getHeight(), coloringCrystal.getWidth()));
        redCrystal.setScale(maxSize/Math.max(redCrystal.getHeight(), redCrystal.getWidth()));
        warpShard.setScale(maxSize/Math.max(warpShard.getHeight(), warpShard.getWidth()));
        ore.setScale(maxSize/Math.max(ore.getHeight(), ore.getWidth()));
        ironPlate.setScale(maxSize/Math.max(ironPlate.getHeight(), ironPlate.getWidth()));
        bolt.setScale(maxSize/Math.max(bolt.getHeight(), bolt.getWidth()));
        glassShard.setScale(maxSize/Math.max(glassShard.getHeight(), glassShard.getWidth()));
        energyCell.setScale(maxSize/Math.max(energyCell.getHeight(), energyCell.getWidth()));
        warpShardMK2.setScale(maxSize/Math.max(warpShardMK2.getHeight(), warpShardMK2.getWidth()));
        cog.setScale(maxSize/Math.max(cog.getHeight(), cog.getWidth()));
        wire.setScale(maxSize/Math.max(wire.getHeight(), wire.getWidth()));
        coreShard.setScale(maxSize/Math.max(coreShard.getHeight(), coreShard.getWidth()));
        ironShard.setScale(maxSize/Math.max(ironShard.getHeight(), ironShard.getWidth()));
        plastic.setScale(maxSize/Math.max(plastic.getHeight(), plastic.getWidth()));
        rubber.setScale(maxSize/Math.max(rubber.getHeight(), rubber.getWidth()));
        warpShardMK3.setScale(maxSize/Math.max(warpShardMK3.getHeight(), warpShardMK3.getWidth()));
    }
    public static void drop(Rectangle originEnemy, int count, float timer, int rarity){
        drop(originEnemy.getX() + originEnemy.width / 2 - maxSize/2, originEnemy.getY() + originEnemy.height / 2 - maxSize/2, count, timer, rarity);
    }

    public static void drop(float x, float y, int count, float timer, int rarity){
        count+=MathUtils.clamp((int)(random.nextFloat()*4-2), 0, 100);
        for(int i = 0; i<count; i++) {
            Rectangle drop = new Rectangle();

            drop.x = x;
            drop.y = y;

            currentType = MathUtils.clamp(getRandomInRange(rarity/2+1, rarity+10), 1, 14);
            if(random.nextFloat()>0.85f*(getInteger("item_craftingCard")/4f+1) && getInteger("c_limit")<3){
                currentType = 0;
                addInteger("c_limit", 1);
            }
            types.add(currentType);

            drops.add(drop);
            timers.add(timer * (random.nextFloat() + 1));
            degrees.add(random.nextFloat() * 360);
        }
    }

    private Sprite getSpriteByType(int type){
        Sprite item = new Sprite();
        switch (type){
            case(0):
                item = craftingCard;
                break;
            case(1):
                item = coloringCrystal;
                break;
            case(2):
                item = ore;
                break;
            case(3):
                item = ironShard;
                break;
            case(4):
                item = plastic;
                break;
            case(5):
                item = rubber;
                break;
            case(6):
                item = cog;
                break;
            case(7):
                item = wire;
                break;
            case(8):
                item = bolt;
                break;
            case(9):
                item = ironPlate;
                break;
            case(10):
                item = glassShard;
                break;
            case(11):
                item = warpShard;
                break;
            case(12):
                item = warpShardMK2;
                break;
            case(13):
                item = warpShardMK3;
                break;
            case(14):
                item = redCrystal;
                break;
            case(15):
                item = energyCell;
                break;
            case(16):
                item = coreShard;
                break;
        }
        return item;
    }

    private String getDropCodeNameByType(int type){
        String item = "";
        switch (type){
            case(0):
                item = "craftingCard";
                break;
            case(1):
                item = "crystal";
                break;
            case(2):
                item = "ore";
                break;
            case(3):
                item = "ironShard";
                break;
            case(4):
                item = "plastic";
                break;
            case(5):
                item = "rubber";
                break;
            case(6):
                item = "cog";
                break;
            case(7):
                item = "wire";
                break;
            case(8):
                item = "bolt";
                break;
            case(9):
                item = "ironPlate";
                break;
            case(10):
                item = "glassShard";
                break;
            case(11):
                item = "bonus_warp";
                break;
            case(12):
                item = "bonus_warp2";
                break;
            case(13):
                item = "bonus_warp3";
                break;
            case(14):
                item = "redCrystal";
                break;
            case(15):
                item = "energyCell";
                break;
            case(16):
                item = "fragment_core";
                break;
        }
        return item;
    }

    public void draw(SpriteBatch batch, boolean is_paused) {

        for (int i = 0; i < drops.size; i++) {
            Rectangle drop = drops.get(i);
            int type = types.get(i);
            Sprite target = getSpriteByType(type);
            drop.setSize(maxSize, maxSize);
            float degree = degrees.get(i);
            float timer = timers.get(i);

            target.setPosition(drop.x+drop.width/2-target.getWidth()/2, drop.y+drop.height/2-target.getHeight()/2);
            target.setOrigin(target.getWidth() / 2f, target.getHeight() / 2f);
            target.draw(batch);

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
    }

    private void removeDrop(int i){
        addInteger("item_"+getDropCodeNameByType(types.get(i)), 1);
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
