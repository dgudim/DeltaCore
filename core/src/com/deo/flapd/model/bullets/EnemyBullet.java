package com.deo.flapd.model.bullets;

import static com.deo.flapd.utils.DUtils.getDistanceBetweenTwoPoints;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.model.Player;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.DUtils;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.view.screens.GameScreen;

public class EnemyBullet extends Bullet {
   
    private final Player player;
    private final PlayerBullet playerBullet;
    
    public EnemyBullet(CompositeManager compositeManager, JsonEntry bulletData, Player player) {
        super(compositeManager, bulletData);
        this.player = player;
        playerBullet = this.player.bullet;
    }
    
    @Override
    void loadBulletData(JsonEntry bulletData) {
        data.texture = bulletData.getString("noTexture", "texture");
        
        data.isLaser = bulletData.getBoolean(false, false, "isLaser");
        data.isBeam = bulletData.getBoolean(false, false, "isBeam");
        if (data.isLaser) {
            data.fadeOutTimer = bulletData.getFloat(3, "fadeOutTimer");
            data.maxFadeOutTimer = bulletData.getFloat(3, "fadeOutTimer");
            color = Color.valueOf(bulletData.getString("ffffffff", "color"));
        } else {
            color = Color.WHITE;
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
        if (data.hasCollisionWithEnemyBullets) {
            for (int i = 0; i < playerBullet.bullets.size; i++) {
                if (overlaps(playerBullet.bullets.get(i)) && !playerBullet.remove_Bullet.get(i)) {
                    float playerBulletHealth = playerBullet.damages.get(i);
                    playerBullet.damages.set(i, playerBulletHealth - health);
                    if (playerBulletHealth - health <= 0) {
                        playerBullet.removeBullet(i, true);
                    }
                    health -= playerBulletHealth;
                    if (health <= 0) {
                        explode();
                    }
                }
            }
        }
        
        if (overlaps(player.entityHitBox)) {
            player.takeDamage(health * (data.isLaser ? data.fadeOutTimer / data.maxFadeOutTimer : 1) * (data.isLaser ? delta * 1000 : 1));
            GameScreen.screenShake(data.screenShakeIntensity * (data.isLaser ? data.fadeOutTimer / data.maxFadeOutTimer : 1), data.screenShakeDuration);
            explode();
        }
    }
    
    @Override
    public void updateHomingLogic(float delta) {
        rotation = DUtils.lerpAngleWithConstantSpeed(rotation,
                MathUtils.radiansToDegrees * MathUtils.atan2(
                        y - (player.y + player.height / 2f),
                        x - (player.x + player.width / 2f)),
                data.homingSpeed, delta);
        data.explosionTimer -= delta;
    }
}