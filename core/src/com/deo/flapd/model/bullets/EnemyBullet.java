package com.deo.flapd.model.bullets;

import static com.deo.flapd.utils.DUtils.getDistanceBetweenTwoPoints;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.model.Player;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.view.screens.GameScreen;

public class EnemyBullet extends Bullet {
   
    private final Player player;
    
    public EnemyBullet(CompositeManager compositeManager, JsonEntry bulletData, Player player) {
        super(compositeManager, bulletData);
        setHomingTarget(player);
        this.player = player;
    }
    
    @Override
    void loadBulletData(JsonEntry bulletData) {
        data.texture = bulletData.getString("noTexture", "texture");
        
        data.isLaser = bulletData.getBoolean(false, false, "isLaser");
        data.isBeam = bulletData.getBoolean(false, false, "isBeam");
        if (data.isLaser) {
            data.fadeOutTimer = bulletData.getFloat(3, "fadeOutTimer");
            data.maxFadeOutTimer = data.fadeOutTimer;
            color = Color.valueOf(bulletData.getString("ffffffff", "color"));
        } else {
            width = bulletData.getFloat(1, "width");
            
            speed = bulletData.getFloat(130, "speed");
            
            data.trail = bulletData.getString("particles/bullet_trail_left.p", "trail");
            data.trailScale = bulletData.getFloat(1, "trailScale");
            data.drawTrailOnTop = bulletData.getBoolean(false, false, "drawTrailOnTop");
            float[] trailOffset = bulletData.getFloatArray(new float[]{0, 0}, "trailOffset");
            data.trailOffsetAngle = MathUtils.atan2(trailOffset[1], trailOffset[0]) * MathUtils.radiansToDegrees;
            data.trailOffsetDistance = getDistanceBetweenTwoPoints(0, 0, trailOffset[0], trailOffset[1]);
            
            data.explosion = bulletData.getString("particles/explosion2.p", "explosionEffect");
            data.explosionScale = bulletData.getFloat(1, "explosionScale");
            
            data.isHoming = bulletData.getBoolean(false, false, "homing");
            if (data.isHoming) {
                data.explosionTimer = bulletData.getFloat(3, "explosionTimer");
                data.homingSpeed = bulletData.getFloat(5, "homingSpeed");
            }
        }
        
        height = bulletData.getFloat(1, "height");
        
        data.hasCollisionWithEnemyBullets = bulletData.getBoolean(false, false, "hasCollisionWithPlayerBullets");
        
        health = bulletData.getFloat(1, "damage");
        
        data.screenShakeIntensity = bulletData.getFloat(false, 0, "screenShakeOnHit");
        data.screenShakeDuration = bulletData.getFloat(false, 0, "screenShakeDuration");
    }
    
    @Override
    public void checkCollisions(float delta) {
        if(data.hasCollisionWithEnemyBullets){
            player.collideWithBullet(this, true);
        }
        if (overlaps(player)) {
            player.takeDamage(health * (data.isLaser ? data.fadeOutTimer / data.maxFadeOutTimer : 1) * (data.isLaser ? delta * 1000 : 1));
            GameScreen.screenShake(data.screenShakeIntensity * (data.isLaser ? data.fadeOutTimer / data.maxFadeOutTimer : 1), data.screenShakeDuration);
            explode();
        }
    }
}