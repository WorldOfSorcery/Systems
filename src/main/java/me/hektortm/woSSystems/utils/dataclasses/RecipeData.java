package me.hektortm.woSSystems.utils.dataclasses;

import org.json.simple.JSONObject;

public class RecipeData {
    private final org.bukkit.inventory.Recipe recipe;
    private final JSONObject conditions;

    public RecipeData(org.bukkit.inventory.Recipe recipe, JSONObject conditions) {
        this.recipe = recipe;
        this.conditions = conditions;
    }

    public org.bukkit.inventory.Recipe getRecipe() {
        return recipe;
    }

    public JSONObject getConditions() {
        return conditions;
    }
}
