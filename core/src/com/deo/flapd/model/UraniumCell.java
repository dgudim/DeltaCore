package com.deo.flapd.model;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.control.GameLogic;

import java.util.Random;

import static com.deo.flapd.utils.DUtils.getFloat;

public class UraniumCell {

    private Sprite cell;
    private static Array<Float> timers;
    private static Array<Rectangle> cells;
    private static Array<Float> degrees;
    private static Array<Integer> values;
    private static Random random;
    private float uiScaling;

    public UraniumCell(AssetManager assetManager) {

        random = new Random();

        cell = new Sprite((Texture) assetManager.get("uraniumCell.png"));
        cells = new Array<>();
        timers = new Array<>();
        degrees = new Array<>();
        values = new Array<>();

        uiScaling = getFloat("ui");
    }

    public static void Spawn(Rectangle originEnemy, int count, float scale, float timer) {
        Spawn(originEnemy.getX() + originEnemy.width / 2 - 19, originEnemy.getY() + originEnemy.height / 2 - 22, count, scale, timer);
    }

    public static void Spawn(float x, float y, int count, float scale, float timer) {
        for (int i = 0; i < count; i++) {
            Rectangle cell = new Rectangle();

            cell.x = x;
            cell.y = y;

            cell.setSize(39 * scale, 45 * scale);

            cells.add(cell);
            timers.add(timer * (random.nextFloat() + 1));
            degrees.add(random.nextFloat() * 360);
            values.add((int) MathUtils.clamp((random.nextInt(3) + 1) * scale, 1, 4));
        }
    }

    public void draw(SpriteBatch batch, float delta) {
        for (int i = 0; i < cells.size; i++) {
            Rectangle cell = cells.get(i);
            float degree = degrees.get(i);
            float timer = timers.get(i);
            float pack_level = values.get(i);

            this.cell.setPosition(cell.x, cell.y);
            this.cell.setSize(cell.width, cell.height);
            this.cell.setOrigin(cell.width / 2f, cell.height / 2f);
            this.cell.setColor(1 - (pack_level - 1) / 4, 1 - (pack_level - 1) / 4, 1, 1);
            this.cell.draw(batch);

            if (timer > 0) {
                cell.x -= MathUtils.cosDeg(degree) * timer * 30 * delta;
                cell.y -= MathUtils.sinDeg(degree) * timer * 30 * delta;
            } else {
                Vector2 pos1 = new Vector2();
                pos1.set(cell.x, cell.y);
                Vector2 pos2 = new Vector2();
                pos2.set(376 - 400 * (uiScaling - 1), 435 - 20 * (uiScaling - 1));
                pos1.lerp(pos2, 5*delta);

                cell.x = pos1.x;
                cell.y = pos1.y;
            }
            timer = timer - delta;
            timers.set(i, timer);

            if (cell.y > 435 - 20 * (uiScaling - 1) - 2 * uiScaling && cell.x > 376 - 400 * (uiScaling - 1) - 10 * uiScaling && cell.x < 376 - 400 * (uiScaling - 1) + 10 * uiScaling) {
                removeCell(i);
            }

        }
    }

    private void removeCell(int i) {
        timers.removeIndex(i);
        cells.removeIndex(i);
        degrees.removeIndex(i);
        GameLogic.money += values.get(i);
        GameLogic.moneyEarned += values.get(i);
        values.removeIndex(i);
    }

    public void dispose() {
        timers.clear();
        cells.clear();
        degrees.clear();
        values.clear();
    }

}
