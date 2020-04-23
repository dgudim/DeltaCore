package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.Random;

import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;
import static com.deo.flapd.utils.DUtils.getString;

public class Bullet {

    private Polygon bounds;
    public static Array<Rectangle> bullets;
    public static Array<Integer> damages;
    private Array<Float> degrees;
    private Array<ParticleEffect> explosions;
    private Array<Boolean> types;
    private Sprite bullet;

    private Sound shot;

    private float soundVolume;

    public static int bulletsShot;

    private Random random;

    private float spread;

    private static Array<Boolean> explosionQueue, remove_Bullet;

    private float shootingSpeedMultiplier;

    private float powerConsumption;

    private Sprite laser;

    private static Rectangle laserTip;

    private String effect;

    private float width, height;

    private int damage;

    private int bulletSpeed;

    public Bullet(AssetManager assetManager, Polygon shipBounds, boolean newGame) {
        bounds = shipBounds;

        TextureAtlas bullets = assetManager.get("bullets.atlas");

        bullet = new Sprite(bullets.findRegion("bullet_" + getItemCodeNameByName(getString("currentCannon"))));
        JsonValue treeJson = new JsonReader().parse(Gdx.files.internal("shop/tree.json"));
        effect = treeJson.get(getString("currentCannon")).get("usesEffect").asString();
        String[] params = treeJson.get(getString("currentCannon")).get("parameters").asStringArray();
        float[] paramValues = treeJson.get(getString("currentCannon")).get("parameterValues").asFloatArray();
        for (int i = 0; i < params.length; i++) {
            if (params[i].endsWith("damage")) {
                damage = (int) paramValues[i];
            }
            if (params[i].endsWith("shooting speed")) {
                shootingSpeedMultiplier = paramValues[i];
            }
            if (params[i].endsWith("spread")) {
                spread = paramValues[i];
            }
            if (params[i].endsWith("power consumption")) {
                powerConsumption = paramValues[i];
            }
            if (params[i].endsWith("bullet speed")) {
                bulletSpeed = (int) paramValues[i];
            }
        }
        params = treeJson.get(getString("currentCore")).get("parameters").asStringArray();
        paramValues = treeJson.get(getString("currentCore")).get("parameterValues").asFloatArray();
        for (int i = 0; i < params.length; i++) {
            if (params[i].endsWith("damage multiplier")) {
                damage *= paramValues[i];
            }
        }
        random = new Random();

        Bullet.bullets = new Array<>();
        damages = new Array<>();
        degrees = new Array<>();
        explosions = new Array<>();
        explosionQueue = new Array<>();
        remove_Bullet = new Array<>();
        types = new Array<>();

        if (!newGame) {
            bulletsShot = getInteger("bulletsShot");
        } else {
            bulletsShot = 0;
        }

        this.laser = new Sprite((Texture) assetManager.get("laser.png"));

        soundVolume = getFloat("soundVolume");
        shot = Gdx.audio.newSound(Gdx.files.internal("sfx/gun4.ogg"));

        laserTip = new Rectangle();

        width = bullet.getWidth();
        height = bullet.getHeight();

        float scale = 10 / height;
        width = width * scale;
        height = 10;

        bullet.setOrigin(0, 5);
    }

    public void Spawn(float damageMultiplier, boolean is_charged) {

        if (SpaceShip.Charge >= powerConsumption) {
            Rectangle bullet = new Rectangle();

            bullet.setSize(width, height);

            bullet.x = bounds.getX() + 68;
            bullet.y = bounds.getY() + 12.5f;

            bullets.add(bullet);
            explosionQueue.add(false);
            remove_Bullet.add(false);
            if (SpaceShip.Charge >= powerConsumption * damageMultiplier + 0.5f && is_charged) {
                types.add(true);
                damages.add((int) (damage * damageMultiplier));
                SpaceShip.Charge -= powerConsumption * damageMultiplier + 0.5f;
            } else {
                types.add(false);
                damages.add(damage);
                SpaceShip.Charge -= powerConsumption;
            }

            degrees.add((random.nextFloat() - 0.5f) * spread + bounds.getRotation() / 20);

            bullet.x += MathUtils.cosDeg(bounds.getRotation()) * 6;
            bullet.y += MathUtils.cosDeg(bounds.getRotation());

            bulletsShot++;

            if (soundVolume > 0) {
                shot.play(soundVolume / 100);
            }
        }
    }

    public void draw(SpriteBatch batch, float delta) {

        /*
        int gradient = 0;
        for(float i = bounds.getX(); i < 800; i += 3){
            laser.setSize(3, 9);
            laser.setPosition(i+72, bounds.getY()+16);
            laser.setColor(new Color().fromHsv(gradient+offset, 1, 1).add(0,0,0,1));
            laserBounds.setSize(800, 9).setPosition(72, bounds.getY()+16);
            laser.draw(batch);
            gradient+=1;
        }
        offset+=10;
         */

        for (int i = 0; i < bullets.size; i++) {

            Rectangle bullet = bullets.get(i);
            float angle = degrees.get(i);

            this.bullet.setPosition(bullet.x, bullet.y);
            this.bullet.setSize(bullet.width, bullet.height);
            this.bullet.setRotation(MathUtils.radiansToDegrees * MathUtils.atan2(300 * angle, 1500));
            if (types.get(i)) {
                this.bullet.setColor(Color.GREEN);
            } else {
                this.bullet.setColor(Color.WHITE);
            }
            this.bullet.draw(batch);

            bullet.x += bulletSpeed * delta;
            bullet.y += 300 * bulletSpeed/1500.0f * angle * delta;

            if (bullet.x > 800) {
                Bullet.removeBullet(i, false);
            }
        }
        for (int i3 = 0; i3 < explosions.size; i3++) {
            explosions.get(i3).draw(batch);
            explosions.get(i3).update(delta);
            if (explosions.get(i3).isComplete()) {
                explosions.get(i3).dispose();
                explosions.removeIndex(i3);
            }
        }
        for (int i4 = 0; i4 < bullets.size; i4++) {
            if (explosionQueue.get(i4)) {
                ParticleEffect explosionEffect = new ParticleEffect();
                if (types.get(i4)) {
                    explosionEffect.load(Gdx.files.internal("particles/explosion3_4.p"), Gdx.files.internal("particles"));
                } else {
                    explosionEffect.load(Gdx.files.internal("particles/" + effect + ".p"), Gdx.files.internal("particles"));
                }
                explosionEffect.setPosition(bullets.get(i4).x + bullets.get(i4).width / 2, bullets.get(i4).y + bullets.get(i4).height / 2);
                explosionEffect.start();
                explosions.add(explosionEffect);
                explosionQueue.removeIndex(i4);
                bullets.removeIndex(i4);
                degrees.removeIndex(i4);
                damages.removeIndex(i4);
                remove_Bullet.removeIndex(i4);
                types.removeIndex(i4);
            } else if (remove_Bullet.get(i4)) {
                explosionQueue.removeIndex(i4);
                bullets.removeIndex(i4);
                degrees.removeIndex(i4);
                damages.removeIndex(i4);
                remove_Bullet.removeIndex(i4);
                types.removeIndex(i4);
            }
        }
    }

    public void dispose() {
        shot.dispose();
        bullets.clear();
        damages.clear();
        degrees.clear();
        for (int i3 = 0; i3 < explosions.size; i3++) {
            explosions.get(i3).dispose();
            explosions.removeIndex(i3);
        }
        explosionQueue.clear();
        remove_Bullet.clear();
        types.clear();
    }

    public static void removeBullet(int i, boolean explode) {
        if (explode) {
            explosionQueue.set(i, true);
            remove_Bullet.set(i, true);
        } else {
            explosionQueue.set(i, false);
            remove_Bullet.set(i, true);
        }
    }

    public float getShootingSpeed() {
        return shootingSpeedMultiplier;
    }

}
