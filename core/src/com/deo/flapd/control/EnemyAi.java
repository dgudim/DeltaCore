package com.deo.flapd.control;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.deo.flapd.model.Entity;
import com.deo.flapd.model.Player;
import com.deo.flapd.model.bullets.PlayerBullet;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.lerpWithConstantSpeed;
import static java.lang.StrictMath.abs;

public class EnemyAi {
    
    boolean dodgeBullets;
    float bulletDodgeSpeed;
    int[] XMovementBounds;
    int[] YMovementBounds;
    boolean followPlayer;
    float playerFollowSpeed;
    
    boolean playerFollowActive;
    boolean dodgeFlightActive;
    float playerInsideEntityTimer;
    float playerInsideEntityMaxTime;
    
    Player player;
    PlayerBullet playerBullet;
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
        playerBounds = player.entityHitBox;
    }
    
    public void update(float delta) {
        if (targetEntity.active) {
            if (followPlayer) {
                playerLastHealthDifference = (player.health + player.shieldCharge - playerLastHealth) / delta;
                playerLastHealth = player.health + player.shieldCharge;
                if (playerLastHealthDifference >= 0) {
                    playerNotDamagedTimer += delta;
                } else {
                    playerNotDamagedTimer = 0;
                }
                playerInsideEntityTimer = clamp(playerInsideEntityTimer - delta, 0, playerInsideEntityMaxTime);
                if (targetEntity.overlaps(playerBounds)) {
                    playerInsideEntityTimer += delta * 2;
                }
                if (playerInsideEntityTimer > playerInsideEntityMaxTime) {
                    playerNotDamagedTimer = 0;
                    playerInsideEntityTimer = 0;
                    setTargetPosition(
                            getRandomInRange(XMovementBounds[0], XMovementBounds[1]),
                            getRandomInRange(YMovementBounds[0], YMovementBounds[1]), false);
                }
                if (playerNotDamagedTimer > playerNotDamagedMaxTime) {
                    float targetX = playerBounds.getX() + playerBounds.getWidth() + 15;
                    float targetY = playerBounds.getY() + playerBounds.getHeight() / 2f - targetEntity.height / 2f;
                    setTargetPosition(targetX, targetY, false);
                }
            }
            if (dodgeBullets && playerInsideEntityTimer <= playerInsideEntityMaxTime / 2f) {
                Rectangle nearestBullet = null;
                float nearestX = 0;
                for (int i = 0; i < playerBullet.bullets.size; i++) {
                    if (playerBullet.bullets.get(i).getX() > nearestX && playerBullet.bullets.get(i).getX() < targetEntity.x + targetEntity.width && !playerBullet.remove_Bullet.get(i)) {
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
                            int[] XMovementBounds, int[] YMovementBounds,
                            boolean followPlayer, float playerFollowSpeed, float playerNotDamagedMaxTime, float playerInsideEntityMaxTime, Vector2 basePosition) {
        this.dodgeBullets = dodgeBullets;
        this.bulletDodgeSpeed = bulletDodgeSpeed;
        this.XMovementBounds = XMovementBounds;
        this.YMovementBounds = YMovementBounds;
        this.followPlayer = followPlayer;
        this.playerNotDamagedMaxTime = playerNotDamagedMaxTime;
        this.playerInsideEntityMaxTime = playerInsideEntityMaxTime;
        this.playerFollowSpeed = playerFollowSpeed;
        this.basePosition = basePosition;
        playerFollowTargetPosition = basePosition;
    }
    
}
