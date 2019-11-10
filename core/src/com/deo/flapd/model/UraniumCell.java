package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class UraniumCell {

    private Sprite cell;
    private Array <Float> timers;
    private Array <Rectangle> cells;
    private Array <Float> degrees;
    private float width, height;
    private Random random;

    public UraniumCell(AssetManager assetManager, float width, float height){

        cell = new Sprite((Texture)assetManager.get("uraniumCell.png"));
        cells = new Array<>();
        timers = new Array<>();
        degrees = new Array<>();

        this.width = width;
        this.height = height;

        random = new Random();
    }

    public void Spawn (Rectangle originEnemy, int count, float scale, float timer){
        for(int i = 0; i<count; i++) {
            Rectangle cell = new Rectangle();

            cell.x = originEnemy.getX() + originEnemy.width / 2 - width/2;
            cell.y = originEnemy.getY() + originEnemy.height / 2 - height/2;

            cell.setSize(width*scale, height*scale);

            cells.add(cell);
            timers.add(timer*(random.nextFloat()+0.2f));
            degrees.add(random.nextFloat()*360);
        }
    }

    public void draw(SpriteBatch batch, boolean is_paused){
        for(int i = 0; i<cells.size; i++){
             Rectangle cell = cells.get(i);
             float degree = degrees.get(i);
             float timer = timers.get(i);

            this.cell.setPosition(cell.x, cell.y);
            this.cell.setSize(cell.width, cell.height);
            this.cell.setOrigin(cell.width / 2f, cell.height / 2f);
            this.cell.draw(batch);

            if(!is_paused){
                if(timer > 0) {
                    cell.x -= MathUtils.cosDeg(degree) * timer * 30 * Gdx.graphics.getDeltaTime();
                    cell.y -= MathUtils.sinDeg(degree) * timer * 30 * Gdx.graphics.getDeltaTime();
                }else{
                    Vector2 pos1 = new Vector2();
                    pos1.set(cell.x, cell.y);
                    Vector2 pos2 = new Vector2();
                    pos2.set(240, 820);
                    pos1.lerp(pos2, 0.05f);

                    cell.x = pos1.x;
                    cell.y = pos1.y;
                }
                timer = timer - 1 * Gdx.graphics.getDeltaTime();
                timers.set(i, timer);
            }

            if(cell.y>800){
                removeCell(i);
            }

        }
    }

    public void removeCell(int i){
        timers.removeIndex(i);
        cells.removeIndex(i);
        degrees.removeIndex(i);
    }

}
