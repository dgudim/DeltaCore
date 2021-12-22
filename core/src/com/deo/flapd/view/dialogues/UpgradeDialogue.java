package com.deo.flapd.view.dialogues;

import static com.deo.flapd.utils.DUtils.getBoolean;

import com.badlogic.gdx.utils.Array;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;

public class UpgradeDialogue extends Dialogue{

    UpgradeDialogue(CompositeManager compositeManager){
        super(compositeManager, "upgradeDialog");
    }

}

class Node{
    
    String name;
    String parameter;
    float value;
    String type;
    boolean unlocked;
    
    Node(JsonEntry nodeInfo, String partName, Array<Node> nodes){
        name = nodeInfo.parent().name + ":" + nodeInfo.name;
        type = nodeInfo.getString(false, "upgrade", "type");
        parameter = nodeInfo.getString("", "upgradeParameter");
        value = Float.parseFloat(nodeInfo.getString("upgradeValue").replace("X", ""));
        
        unlocked = getBoolean("upgrade_" + partName + "_" + name + "_unlocked");
    }

}