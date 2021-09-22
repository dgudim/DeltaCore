package com.deo.flapd.view.dialogues;

import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.utils.JsonEntry;

public abstract class MoneyDialogue extends Dialogue{
    public static int[] getPrice(String result, JsonEntry treeJson, float priceCoefficient) {
        JsonEntry price = treeJson.get(result, "price");
        int[] priceArray = new int[]{0, 0};
        if (price.asString().equals("auto")) {
            String[] items = treeJson.getStringArray(new String[]{}, result, "items");
            int[] itemCounts = treeJson.getIntArray(new int[]{}, result, "itemCounts");
            for (int i = 0; i < items.length; i++) {
                int[] buffer = getPrice(items[i], treeJson, priceCoefficient);
                priceArray[0] += Math.ceil(buffer[0] / treeJson.getFloat(1, result, "resultCount") * itemCounts[i] / 3f);
                priceArray[1] += buffer[1] + 1;
            }
        } else {
            return new int[]{price.asInt(), 0};
        }
        priceArray[1] = (int) MathUtils.clamp((Math.ceil(priceArray[1] / 2f) - 1) * priceCoefficient, 0, 100);
        return priceArray;
    }
}
