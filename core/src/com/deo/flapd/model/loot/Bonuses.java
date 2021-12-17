package com.deo.flapd.model.loot;

import static com.deo.flapd.utils.DUtils.getFloat;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.control.GameVariables;
import com.deo.flapd.model.Player;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.utils.particles.ParticleEffectPoolLoader;

public class Bonuses {
    
    enum BonusType {HEALTH, SHIELD, ENERGY, PART, BULLETS}
    
    private final TextureAtlas bonusesAtlas;
    private final ParticleEffectPoolLoader particleEffectPool;
    
    private final Player player;
    private static Array<Bonus> bonuses;
    private final Array<ParticleEffectPool.PooledEffect> explosions;
    
    private final BitmapFont font_text;
    
    private final float uiScale;
    
    private final Image bonus_bullets_t;
    
    private static float size;
    
    public Bonuses(CompositeManager compositeManager, float maxSize, Player player) {
        
        this.player = player;
        
        uiScale = getFloat(Keys.uiScale);
        
        AssetManager assetManager = compositeManager.getAssetManager();
        particleEffectPool = compositeManager.getParticleEffectPool();
        
        bonusesAtlas = assetManager.get("bonuses.atlas");
        
        bonus_bullets_t = new Image(bonusesAtlas.findRegion("bonus_bullets"));
        bonus_bullets_t.setBounds(319 - 475 * (uiScale - 1), 475 - 50 * uiScale, 50 * uiScale, 50 * uiScale);
        
        size = maxSize;
        
        bonuses = new Array<>();
        explosions = new Array<>();
        
        font_text = assetManager.get("fonts/pixel.ttf");
    }
    
    public void drop(int type, Rectangle enemy) {
        bonuses.add(new Bonus(size, bonusesAtlas, player, this, type, enemy));
    }
    
    public void draw(SpriteBatch batch, float delta) {
        
        for (int i = 0; i < bonuses.size; i++) {
            bonuses.get(i).update(delta);
            bonuses.get(i).draw(batch);
            if (bonuses.get(i).isDead) {
                removeBonus(i, !(bonuses.get(i).y < -bonuses.get(i).height) && !(bonuses.get(i).y > 480) && !(bonuses.get(i).x < -bonuses.get(i).width) && !(bonuses.get(i).x > 800));
            }
        }
        if (GameVariables.bonuses_collected > 0) {
            font_text.setColor(Color.WHITE);
            font_text.getData().setScale(0.3f * uiScale);
            font_text.draw(batch, "X" + GameVariables.bonuses_collected, 333 - 463 * (uiScale - 1), 425 - 55 * (uiScale - 1), 24 * uiScale, 1, false);
            bonus_bullets_t.draw(batch, 1);
            font_text.setColor(Color.BLACK);
        }
        for (int i3 = 0; i3 < explosions.size; i3++) {
            explosions.get(i3).draw(batch, delta);
            if (explosions.get(i3).isComplete()) {
                explosions.get(i3).free();
                explosions.removeIndex(i3);
            }
        }
    }
    
    public void drawDebug(ShapeRenderer shapeRenderer){
        for (int i = 0; i < bonuses.size; i++) {
            bonuses.get(i).drawDebug(shapeRenderer);
        }
    }
    
    public void dispose() {
        bonuses.clear();
        for (int i3 = 0; i3 < explosions.size; i3++) {
            explosions.get(i3).free();
        }
        explosions.clear();
    }
    
    private void removeBonus(int i, boolean explode) {
        if (explode) {
            String path;
            switch (bonuses.get(i).bonusType) {
                case PART:
                    path = "particles/explosion4.p";
                    break;
                case HEALTH:
                    path = "particles/explosion4_1.p";
                    break;
                case ENERGY:
                case BULLETS:
                default:
                    path = "particles/explosion4_2.p";
                    break;
                case SHIELD:
                    path = "particles/explosion4_3.p";
                    break;
            }
            ParticleEffectPool.PooledEffect explosionEffect = particleEffectPool.getParticleEffectByPath(path);
            explosionEffect.setPosition(bonuses.get(i).x + bonuses.get(i).width / 2, bonuses.get(i).y + bonuses.get(i).height / 2);
            explosions.add(explosionEffect);
        }
        bonuses.removeIndex(i);
    }
}
