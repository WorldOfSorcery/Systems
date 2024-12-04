package me.hektortm.woSSystems.utils.dataclasses;

import org.bukkit.inventory.Recipe;
import org.json.simple.JSONObject;

public class RecipeData {
    private final Recipe recipe;
    private final JSONObject conditions;
    private final String successId;

    public RecipeData(Recipe recipe, JSONObject conditions, String successId) {
        this.recipe = recipe;
        this.conditions = conditions;
        this.successId = successId;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public JSONObject getConditions() {
        return conditions;
    }

    public String getSuccessId() {
        return successId;
    }
}

