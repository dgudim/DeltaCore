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
import com.deo.flapd.view.GameUi;

import java.util.Random;

public class UraniumCell {

    private Sprite cell;
    private Array <Float> timers;
    private Array <Rectangle> cells;
    private Array <Float> degrees;
    private Array <Integer> values;
    private float width, height;
    private Random random;
    private float uiScaling;

    public UraniumCell(AssetManager assetManager, float width, float height, float uiScaling){

        random = new Random();

        cell = new Sprite((Texture)assetManager.get("uraniumCell.png"));
        cells = new Array<>();
        timers = new Array<>();
        degrees = new Array<>();
        values = new Array<>();

        this.width = width;
        this.height = height;
        this.uiScaling = uiScaling;
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
            values.add((random.nextInt(3))+1);
        }
    }

    public void Spawn (Float x, float y, int count, float scale, float timer){
        for(int i = 0; i<count; i++) {
            Rectangle cell = new Rectangle();

            cell.x = x;
            cell.y = y;

            cell.setSize(width*scale, height*scale);

            cells.add(cell);
            timers.add(timer*(random.nextFloat()+0.2f));
            degrees.add(random.nextFloat()*360);
            values.add((int)MathUtils.clamp((random.nextInt(3)+1)*scale, 1, 4));
        }
    }

    public void draw(SpriteBatch batch, boolean is_paused){
        for(int i = 0; i<cells.size; i++){
             Rectangle cell = cells.get(i);
             float degree = degrees.get(i);
             float timer = timers.get(i);
             float pack_level = values.get(i);

            this.cell.setPosition(cell.x, cell.y);
            this.cell.setSize(cell.width, cell.height);
            this.cell.setOrigin(cell.width / 2f, cell.height / 2f);
            this.cell.setColor(1-(pack_level-1)/4, 1-(pack_level-1)/4, 1, 1);
            this.cell.draw(batch);

            if(!is_paused){
                if(timer > 0) {
                    cell.x -= MathUtils.cosDeg(degree) * timer * 30 * Gdx.graphics.getDeltaTime();
                    cell.y -= MathUtils.sinDeg(degree) * timer * 30 * Gdx.graphics.getDeltaTime();
                }else{
                    Vector2 pos1 = new Vector2();
                    pos1.set(cell.x, cell.y);
                    Vector2 pos2 = new Vector2();
                    pos2.set(344-400*(uiScaling-1), 403-20*(uiScaling-1));
                    pos1.lerp(pos2, 0.05f);

                    cell.x = pos1.x;
                    cell.y = pos1.y;
                }
                timer = timer - 1 * Gdx.graphics.getDeltaTime();
                timers.set(i, timer);
            }

            if(cell.y > 403-20*(uiScaling-1)-2*uiScaling && cell.x > 344-400*(uiScaling-1)-10*uiScaling && cell.x < 344-400*(uiScaling-1)+10*uiScaling){
                removeCell(i);
            }

        }
    }

    private void removeCell(int i){
        timers.removeIndex(i);
        cells.removeIndex(i);
        degrees.removeIndex(i);
        GameUi.money += values.get(i);
        GameUi.moneyEarned += values.get(i);
        values.removeIndex(i);
    }

    public void dispose(){
        timers.clear();
        cells.clear();
        degrees.clear();
        values.clear();
    }

}
