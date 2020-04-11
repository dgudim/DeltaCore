package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.model.Bullet;

public class Enemy {

    EnemyData data;
    private Sprite enemy;

    Enemy(AssetManager assetManager, EnemyData enemyData){
        data = enemyData.clone();
        enemy = new Sprite((Texture)assetManager.get(enemyData.texture));
        enemy.setSize(data.width, data.height);
        enemy.setOrigin(data.width/2, data.height/2);
        for(int i = 0; i<data.fireEffects.length; i++){
            ParticleEffect fire = new ParticleEffect();
            fire.load(Gdx.files.internal(data.fireEffects[i]), Gdx.files.internal("particles"));
            fire.scaleEffect(data.fireScales[i]);
            data.fireParticleEffects.set(i, fire);
            fire.start();
        }
    }

    void draw(SpriteBatch batch){
        enemy.draw(batch);
    }

    void drawEffects(SpriteBatch batch){
        for(int i = 0; i<data.fireParticleEffects.size; i++){
            data.fireParticleEffects.get(i).draw(batch);
        }
    }

    void update(float delta){
        enemy.setPosition(data.x, data.y);
        enemy.setColor(data.currentColor);
        data.x -= data.speed*delta;
        for(int i = 0; i<data.fireParticleEffects.size; i++){
            data.fireParticleEffects.get(i).update(delta);
            data.fireParticleEffects.get(i).setPosition(data.x+data.fireOffsetsX[i], data.y+data.fireOffsetsY[i]);
        }
        if(data.currentColor.r<1){
            data.currentColor.r = MathUtils.clamp(data.currentColor.r+0.07f, 0, 1);
        }
        if(data.currentColor.g<1){
            data.currentColor.g = MathUtils.clamp(data.currentColor.g+0.07f, 0, 1);
        }
        if(data.currentColor.b<1){
            data.currentColor.b = MathUtils.clamp(data.currentColor.b+0.07f, 0, 1);
        }
        for(int i = 0; i<Bullet.bullets.size; i++){
            if(Bullet.bullets.get(i).overlaps(enemy.getBoundingRectangle())){
                data.health -= Bullet.damages.get(i);
                data.currentColor = Color.valueOf(data.hitColor);
                Bullet.removeBullet(i, true);
            }
        }
    }

    void dispose(){
        for(int i = 0; i<data.fireParticleEffects.size; i++){
            data.fireParticleEffects.get(i).dispose();
        }
    }

}
