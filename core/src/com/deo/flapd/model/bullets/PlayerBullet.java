package com.deo.flapd.model.bullets;

import static com.deo.flapd.control.GameVariables.score;
import static com.deo.flapd.utils.DUtils.getString;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.model.enemies.Enemies;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.Keys;

public class PlayerBullet extends Bullet {
    
    protected final Enemies enemies;
    
    public PlayerBullet(CompositeManager compositeManager, Enemies enemies) {
        super(compositeManager, new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json"))), 0);
        this.enemies = enemies;
        init();
        compositeManager.getSoundManager().playSound_noLink(data.isLaser ? "laser" : "gun4");
    }
    
    @Override
    void loadBulletData(JsonEntry treeJson) {
        
        JsonEntry currentWeapon = treeJson.get(getString(Keys.currentWeapon));
        JsonEntry params_weapon = currentWeapon.get("parameters");
        
        health = params_weapon.getFloat(1, "parameter.damage");
        
        if (params_weapon.getFloat(false, -1, "parameter.laser_beam_thickness") > 0) {
            height = params_weapon.getFloat(1, "parameter.laser_beam_thickness");
            
            data.fadeOutTimer = params_weapon.getFloat(1, "parameter.laser_pulse_duration");
            data.maxFadeOutTimer = data.fadeOutTimer;
            
            data.isLaser = true;
        } else {
            data.explosion = "particles/" + currentWeapon.getString("explosion3", "usesEffect") + ".p";
            data.explosionScale = 1;
            
            data.trail = "particles/" + currentWeapon.getString("bullet_trail_left", "trailEffect") + ".p";
            data.trailScale = 0.5f;
            speed = params_weapon.getFloat(1, "parameter.bullet_speed");
        }
        
        data.texture = data.isLaser ? "bullet_laser" : ("bullet_" + getString(Keys.currentWeapon));
        
        TextureAtlas.AtlasRegion bulletRegion = assetManager.get("bullets/bullets.atlas", TextureAtlas.class).findRegion(data.texture);
        if (!data.isLaser) {
            height = bulletRegion.originalHeight;
        }
        width = bulletRegion.originalWidth;
        
        if (data.isLaser) {
            color = Color.valueOf(currentWeapon.getString("#00FFFF", "laserBeamColor"));
        } else {
            float scale = 10 / height;
            width *= scale;
            height *= scale;
        }
        setSize(width, height);
        
        health *= treeJson.getFloat(false, 1, getString(Keys.currentCore), "parameters", "parameter.damage_multiplier");
        
        data.isHoming = currentWeapon.getBoolean(false, false, "homing");
        if (data.isHoming) {
            data.explosionTimer = currentWeapon.getFloat(3, "explosionTimer");
            data.homingSpeed = currentWeapon.getFloat(9, "homingSpeed");
        }
        
        data.hasCollisionWithEnemyBullets = !data.isLaser;
    }
    
    @Override
    public float getDamage(float delta) {
        float damage = super.getDamage(delta);
        score += damage / 7f;
        return damage;
    }
}
