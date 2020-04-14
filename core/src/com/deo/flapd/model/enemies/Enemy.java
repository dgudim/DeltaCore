package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Drops;
import com.deo.flapd.model.SpaceShip;
import com.deo.flapd.model.UraniumCell;
import com.deo.flapd.model.bullets.BulletData;
import com.deo.flapd.model.bullets.EnemyBullet;

import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class Enemy {

    EnemyData data;
    private BulletData bulletData;
    private Sprite enemy;
    private Array<EnemyBullet> bullets;
    private AssetManager assetManager;
    boolean isDead = false;
    boolean queuedForDeletion = false;
    private boolean explosionFinished = false;

    Enemy(AssetManager assetManager, EnemyData enemyData){
        this.assetManager = assetManager;
        data = enemyData.clone();
        bulletData = new BulletData(data.enemyInfo, data.type);
        bullets = new Array<>();
        enemy = new Sprite((Texture)assetManager.get(enemyData.texture));
        enemy.setSize(data.width, data.height);
        enemy.setPosition(data.x, data.y);
        enemy.setOrigin(data.width/2, data.height/2);
        for(int i = 0; i<data.fireEffects.length; i++){
            ParticleEffect fire = new ParticleEffect();
            fire.load(Gdx.files.internal(data.fireEffects[i]), Gdx.files.internal("particles"));
            fire.scaleEffect(data.fireScales[i]);
            data.fireParticleEffects.set(i, fire);
            fire.start();
        }
        data.explosionParticleEffect = new ParticleEffect();
        data.explosionParticleEffect.load(Gdx.files.internal(data.explosion), Gdx.files.internal("particles"));
        data.explosionParticleEffect.scaleEffect(data.explosionScale);
    }

    void draw(SpriteBatch batch){
        if(!isDead) {
            enemy.draw(batch);
        }
    }

    void drawEffects(SpriteBatch batch){
        if(!isDead) {
            for (int i = 0; i < data.fireParticleEffects.size; i++) {
                data.fireParticleEffects.get(i).draw(batch);
            }
        }else{
            data.explosionParticleEffect.draw(batch);
        }
        for(int i = 0; i<bullets.size; i++){
            bullets.get(i).draw(batch);
        }
    }

    void update(float delta){
        if(!isDead) {
            enemy.setPosition(data.x, data.y);
            enemy.setColor(data.currentColor);
            data.x -= data.speed * delta;
            for (int i = 0; i < data.fireParticleEffects.size; i++) {
                data.fireParticleEffects.get(i).update(delta);
                data.fireParticleEffects.get(i).setPosition(data.x + data.fireOffsetsX[i], data.y + data.fireOffsetsY[i]);
            }

            if (data.currentColor.r < 1) {
                data.currentColor.r = MathUtils.clamp(data.currentColor.r + delta*2.5f, 0, 1);
            }
            if (data.currentColor.g < 1) {
                data.currentColor.g = MathUtils.clamp(data.currentColor.g + delta*2.5f, 0, 1);
            }
            if (data.currentColor.b < 1) {
                data.currentColor.b = MathUtils.clamp(data.currentColor.b + delta*2.5f, 0, 1);
            }

            if (data.millis > data.shootingDelay * 100) {
                for(int i = 0; i<bulletData.bulletsPerShot; i++) {
                    shoot();
                }
            }
            data.millis += delta * 20;

            if (data.x < -data.width - data.fireParticleEffects.get(0).getBoundingBox().getWidth() - 20) {
                isDead = true;
                explosionFinished = true;
            }

        }else{
            data.explosionParticleEffect.update(delta);
        }

        for(int i = 0; i<bullets.size; i++){
            bullets.get(i).update(delta);
            if(bullets.get(i).queuedForDeletion){
                bullets.get(i).dispose();
                bullets.removeIndex(i);
            }
        }

        for(int i = 0; i < Bullet.bullets.size; i++){
            if(Bullet.bullets.get(i).overlaps(enemy.getBoundingRectangle())){
                data.currentColor = Color.valueOf(data.hitColor);
                data.health -= Bullet.damages.get(i);
                GameLogic.Score += 30 + 10 * (Bullet.damages.get(i) / 50 - 1);
                Bullet.removeBullet(i, true);
            }
        }

        if(SpaceShip.bounds.getBoundingRectangle().overlaps(enemy.getBoundingRectangle())){
            kill();
            SpaceShip.Health -= data.health/3f;
        }

        if(data.health<=0 && !isDead){
            kill();
        }

        queuedForDeletion = isDead && !(bullets.size > 0) && (data.explosionParticleEffect.isComplete() || explosionFinished);
    }

    private void shoot(){
        bullets.add(new EnemyBullet(assetManager, bulletData.clone(data)));
        data.millis = 0;
    }

    void dispose(){
        for(int i = 0; i<data.fireParticleEffects.size; i++){
            data.fireParticleEffects.get(i).dispose();
        }
        data.fireParticleEffects.clear();
        data.explosionParticleEffect.dispose();
    }

    private void kill(){
        for(int i = 0; i<data.fireParticleEffects.size; i++){
            data.fireParticleEffects.get(i).dispose();
        }
        data.fireParticleEffects.clear();
        data.explosionParticleEffect.setPosition(data.x + data.width/2, data.y + data.height/2);
        data.explosionParticleEffect.start();
        isDead = true;

        GameLogic.enemiesKilled ++;

        UraniumCell.Spawn(enemy.getBoundingRectangle(), getRandomInRange(data.moneyCount[0], data.moneyCount[1]), 1, data.moneyTimer);
        if(getRandomInRange(0, 99) < data.bonusChance) {
            Bonus.Spawn(getRandomInRange(data.bonusType[0], data.bonusType[1]), 1, enemy.getBoundingRectangle());
        }

        Drops.drop(enemy.getBoundingRectangle(), getRandomInRange(data.dropCount[0], data.dropCount[1]), data.dropTimer, getRandomInRange(data.dropRarity[0], data.dropRarity[1]));

        enemy.setPosition(-100, -100);
    }

}
