package com.deo.flapd.control;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Entity;
import com.deo.flapd.model.Player;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.lerpWithConstantSpeed;
import static java.lang.StrictMath.abs;

public class EnemyAi {
    
    boolean dodgeBullets;
    float bulletDodgeSpeed;
    float[] XMovementBounds;
    float[] YMovementBounds;
    boolean followPlayer;
    float playerFollowSpeed;
    
    boolean playerFollowActive;
    boolean dodgeFlightActive;
    public boolean active;
    
    Player player;
    Bullet playerBullet;
    Entity targetEntity;
    Rectangle playerBounds;
    
    float playerLastHealth;
    float playerLastHealthDifference;
    float playerNotDamagedTimer;
    float playerNotDamagedMaxTime;
    
    Vector2 playerFollowTargetPosition;
    Vector2 dodgeTargetPosition;
    Vector2 basePosition;
    
    public EnemyAi() {
    }
    
    public void initialize(Player player, Entity targetEntity) {
        this.player = player;
        this.targetEntity = targetEntity;
        playerBullet = player.bullet;
        playerBounds = player.bounds;
    }
    
    public void update(float delta) {
        if (active) {
            if (followPlayer) {
                playerLastHealthDifference = (player.Health + player.Shield - playerLastHealth) / delta;
                playerLastHealth = player.Health + player.Shield;
                if (playerLastHealthDifference >= 0) {
                    playerNotDamagedTimer += delta;
                }
                if (playerNotDamagedTimer > playerNotDamagedMaxTime) {
                    playerNotDamagedTimer = 0;
                    float targetX = playerBounds.getX() + playerBounds.getWidth() / 2f - targetEntity.width / 2f;
                    float targetY = playerBounds.getY() + playerBounds.getHeight() / 2f - targetEntity.height / 2f;
                    setTargetPosition(targetX, targetY, false);
                }
            }
            if (dodgeBullets) {
                Rectangle nearestBullet = null;
                float nearestX = 0;
                for (int i = 0; i < playerBullet.bullets.size; i++) {
                    if (playerBullet.bullets.get(i).getX() > nearestX && playerBullet.bullets.get(i).getX() < targetEntity.x + targetEntity.width) {
                        nearestX = playerBullet.bullets.get(i).getX();
                        nearestBullet = playerBullet.bullets.get(i);
                    }
                }
                if (nearestBullet != null) {
                    float targetX = targetEntity.x, targetY = targetEntity.y;
                    if (nearestBullet.getY() > targetEntity.y + targetEntity.height / 2f && nearestBullet.getY() < targetEntity.y + targetEntity.height * 1.4f) {
                        targetY = targetEntity.y - targetEntity.height / 2f;
                    } else if (nearestBullet.getY() < targetEntity.y + targetEntity.height / 2f && nearestBullet.getY() > targetEntity.y - targetEntity.height * 0.4f) {
                        targetY = targetEntity.y + targetEntity.height / 2f;
                    }
                    if (targetEntity.x - nearestBullet.getX() < targetEntity.width / 2f && targetEntity.x - nearestBullet.getX() >= 0) {
                        targetX += targetEntity.width / 2f;
                    }
                    setTargetPosition(targetX, targetY, true);
                }
            }
            if ((!playerFollowActive && !playerFollowTargetPosition.equals(basePosition))) {
                playerFollowTargetPosition = basePosition;
                playerFollowActive = true;
            }
            if (dodgeFlightActive) {
                targetEntity.x = lerpWithConstantSpeed(targetEntity.x, dodgeTargetPosition.x, bulletDodgeSpeed, delta);
                targetEntity.y = lerpWithConstantSpeed(targetEntity.y, dodgeTargetPosition.y, bulletDodgeSpeed, delta);
                if (abs(targetEntity.x - dodgeTargetPosition.x) < 5 && abs(targetEntity.y - dodgeTargetPosition.y) < 5) {
                    dodgeFlightActive = false;
                }
            }
            if (playerFollowActive) {
                targetEntity.x = lerpWithConstantSpeed(targetEntity.x, playerFollowTargetPosition.x, playerFollowSpeed / (dodgeFlightActive ? 4 : 1), delta);
                targetEntity.y = lerpWithConstantSpeed(targetEntity.y, playerFollowTargetPosition.y, playerFollowSpeed / (dodgeFlightActive ? 4 : 1), delta);
                if (abs(targetEntity.x - playerFollowTargetPosition.x) < 5 && abs(targetEntity.y - playerFollowTargetPosition.y) < 5) {
                    playerFollowActive = false;
                }
            }
        }
    }
    
    private void setTargetPosition(float targetX, float targetY, boolean dodgePosition) {
        targetX = clamp(targetX, XMovementBounds[0], XMovementBounds[1] - targetEntity.width);
        targetY = clamp(targetY, YMovementBounds[0], YMovementBounds[1] - targetEntity.height);
        if (dodgePosition) {
            dodgeTargetPosition = new Vector2(targetX, targetY);
            dodgeFlightActive = true;
        } else {
            playerFollowTargetPosition = new Vector2(targetX, targetY);
            playerFollowActive = true;
        }
    }
    
    public void setSettings(boolean dodgeBullets, float bulletDodgeSpeed,
                            float[] XMovementBounds, float[] YMovementBounds,
                            boolean followPlayer, float playerFollowSpeed, float playerNotDamagedMaxTime, Vector2 basePosition) {
        this.dodgeBullets = dodgeBullets;
        this.bulletDodgeSpeed = bulletDodgeSpeed;
        this.XMovementBounds = XMovementBounds;
        this.YMovementBounds = YMovementBounds;
        this.followPlayer = followPlayer;
        this.playerNotDamagedMaxTime = playerNotDamagedMaxTime;
        this.playerFollowSpeed = playerFollowSpeed;
        this.basePosition = basePosition;
        playerFollowTargetPosition = basePosition;
    }
    
}
