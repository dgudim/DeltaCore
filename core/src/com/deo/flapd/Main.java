package com.deo.flapd;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.deo.flapd.utils.ShaderLoader;
import com.deo.flapd.utils.postprocessing.PostProcessor;
import com.deo.flapd.utils.postprocessing.effects.Bloom;
import com.deo.flapd.view.LoadingScreen;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;

import static com.deo.flapd.utils.DUtils.clearLog;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;
import static com.deo.flapd.utils.DUtils.getPrefs;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putFloat;
import static com.deo.flapd.utils.DUtils.putString;

public class Main extends Game {

    private SpriteBatch batch;
    private PostProcessor blurProcessor;

    private AssetManager assetManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();

        clearLog();
        Date date = new Date();
        log("\n\n|-new session-|"+"  "+DateFormat.getDateTimeInstance().format(date)+"\n");

        ShaderLoader.BasePath = "shaders/";
        blurProcessor = new PostProcessor( false, false, Gdx.app.getType() == Application.ApplicationType.Desktop );
        Bloom bloom = new Bloom( (int)(Gdx.graphics.getWidth() * 0.25f), (int)(Gdx.graphics.getHeight() * 0.25f) );
        bloom.setBlurPasses(2);
        bloom.setBloomIntesity(1.3f);
        blurProcessor.addEffect(bloom);

        FileHandleResolver resolver = new InternalFileHandleResolver();
        assetManager.setLoader(BitmapFont.class,".fnt", new BitmapFontLoader(resolver));

        assetManager.load("fonts/font.fnt", BitmapFont.class);
        assetManager.load("fonts/font_white.fnt", BitmapFont.class);
        assetManager.load("fonts/font2.fnt", BitmapFont.class);
        assetManager.load("fonts/font2(old).fnt", BitmapFont.class);

        while (!assetManager.isFinished()) {
            assetManager.update();
        }

        if (getFloat("ui")<=0) {
            putFloat("ui", 1);
            putFloat("soundVolume", 100);
            putFloat("musicVolume", 100);
            putFloat("difficulty", 1);
            putBoolean("transparency", true);
            putBoolean("bloom", true);
            JsonValue tree = new JsonReader().parse(Gdx.files.internal("items/tree.json"));
            for(int i = 0; i<tree.size; i++){
                if(tree.get(i).get("type").asString().equals("baseCategory")){
                    putBoolean("unlocked_"+getItemCodeNameByName(tree.get(i).name), true);
                    putString(tree.get(i).get("saveTo").asString(), tree.get(i).name);
                }
            }
            log("\n------------first launch------------"+"\n");
        }

        this.setScreen(new LoadingScreen(this, batch, assetManager, blurProcessor));
    }

    @Override
    public void render(){
        try{
            super.render();
        }catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String fullStackTrace = sw.toString();
            log("\n"+fullStackTrace + "\n");
            log("dump pf preferences "+getPrefs()+"\n");
            log("force exiting");
            System.exit(1);
        }
    }

    @Override
    public void dispose(){
        batch.dispose();
        assetManager.dispose();
        blurProcessor.dispose();
    }

    private String getDropCodeNameByType(int type){
        String item = "";
        switch (type){
            case(0):
                item = "craftingCard";
                break;
            case(1):
                item = "crystal";
                break;
            case(2):
                item = "ore";
                break;
            case(3):
                item = "ironShard";
                break;
            case(4):
                item = "plastic";
                break;
            case(5):
                item = "rubber";
                break;
            case(6):
                item = "cog";
                break;
            case(7):
                item = "wire";
                break;
            case(8):
                item = "bolt";
                break;
            case(9):
                item = "ironPlate";
                break;
            case(10):
                item = "glassShard";
                break;
            case(11):
                item = "bonus_warp";
                break;
            case(12):
                item = "bonus_warp2";
                break;
            case(13):
                item = "bonus_warp3";
                break;
            case(14):
                item = "redCrystal";
                break;
            case(15):
                item = "energyCell";
                break;
            case(16):
                item = "fragment_core";
                break;
        }
        return item;
    }

}
