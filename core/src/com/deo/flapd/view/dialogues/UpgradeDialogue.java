package com.deo.flapd.view.dialogues;

import static com.deo.flapd.utils.DUtils.getBoolean;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;

public class UpgradeDialogue extends Dialogue {
    
    public UpgradeDialogue(CompositeManager compositeManager, Stage stage) {
        super(compositeManager, "upgradeDialog");
        TextButton ok = uiComposer.addTextButton("workshopGreen", localeManager.get("general.yes"), 0.48f);
        ok.setBounds(180, 12, 152, 47);
        
        ok.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        
        Table holder = new Table();
        holder.setBounds(12, 71, 488, 367);
        
        
        
        dialog.addActor(holder);
        
        dialog.addActor(ok);
        dialog.setSize(512, 450);
        dialog.setPosition(15, 15);
        stage.addActor(dialog);
    }
    
}

class Node {
    
    String name;
    String parameter;
    float value;
    String type;
    boolean unlocked;
    
    Node(JsonEntry nodeInfo, String partName, Array<Node> nodes) {
        name = nodeInfo.parent().name + ":" + nodeInfo.name;
        type = nodeInfo.getString(false, "upgrade", "type");
        parameter = nodeInfo.getString("", "upgradeParameter");
        value = Float.parseFloat(nodeInfo.getString("upgradeValue").replace("X", ""));
        
        unlocked = getBoolean("upgrade_" + partName + "_" + name + "_unlocked");
    }
    
}