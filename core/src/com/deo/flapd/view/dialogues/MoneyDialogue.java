package com.deo.flapd.view.dialogues;

import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;

public class MoneyDialogue extends Dialogue {
    
    MoneyDialogue(CompositeManager compositeManager, String dialogTextureName) {
        super(compositeManager, dialogTextureName);
    }
    
    public static int[] getPrice(String result, JsonEntry treeJson, float priceCoefficient) {
        String type = treeJson.getString("item", result, "type");
        int price = treeJson.getInt(false, 0, result, "price");
        int[] priceArray = new int[]{0, 0};
        if (type.equals("item")) {
            JsonEntry items = treeJson.get(result, "items");
            for (int i = 0; i < items.size; i++) {
                int[] buffer = getPrice(items.get(i).name, treeJson, priceCoefficient);
                priceArray[0] += Math.ceil(buffer[0] / treeJson.getFloat(false, 1, result, "resultCount") * items.getInt(1, i) / 3f);
                priceArray[1] += buffer[1] + 1;
            }
        } else {
            return new int[]{price, 0};
        }
        priceArray[1] = (int) MathUtils.clamp((Math.ceil(priceArray[1] / 2f) - 1) * priceCoefficient, 0, 100);
        return priceArray;
    }
}
