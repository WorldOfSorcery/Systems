package me.hektortm.woSSystems.utils.dataclasses;
import java.util.Map;

public class CRecipe {

    private final String id;
    private final boolean shaped;
    private final String shape; // 9 characters if shaped, null if shapeless
    private final Map<Character, CIngredient> ingredients;
    private final String resultCItemId;
    private final int resultAmount;

    public CRecipe(String id, boolean shaped, String shape,
                   Map<Character, CIngredient> ingredients,
                   String resultCItemId, int resultAmount) {

        this.id = id;
        this.shaped = shaped;
        this.shape = shape;
        this.ingredients = ingredients;
        this.resultCItemId = resultCItemId;
        this.resultAmount = resultAmount;
    }

    public String id() { return id; }
    public boolean shaped() { return shaped; }
    public String shape() { return shape; }
    public Map<Character, CIngredient> ingredients() { return ingredients; }
    public String resultCItemId() { return resultCItemId; }
    public int resultAmount() { return resultAmount; }
}