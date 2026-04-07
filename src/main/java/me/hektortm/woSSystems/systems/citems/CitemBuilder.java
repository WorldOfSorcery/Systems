package me.hektortm.woSSystems.systems.citems;

import com.google.gson.*;
import me.hektortm.woSSystems.utils.Keys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.attribute.*;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.components.*;
import org.bukkit.inventory.meta.trim.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.*;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import javax.naming.Name;
import java.net.URI;
import java.util.*;

/**
 * Converts a CitemWebData JSON object into a Bukkit ItemStack.
 * Targets Paper 1.21.x
 */
public final class CitemBuilder {

    private static final String NS = "wos";

    public static final NamespacedKey KEY_LORE_PAGES = new NamespacedKey(NS, "lore_pages");
    public static final NamespacedKey KEY_LORE_PAGE  = new NamespacedKey(NS, "lore_page");

    private static final MiniMessage MM = MiniMessage.miniMessage();

    // Maps legacy §/& color codes to <#HEX> MiniMessage equivalents
    private static final Map<Character, String> LEGACY_TO_MM;
    static {
        Map<Character, String> m = new HashMap<>();
        m.put('0', "<#000000>"); m.put('1', "<#0000AA>"); m.put('2', "<#00AA00>");
        m.put('3', "<#00AAAA>"); m.put('4', "<#AA0000>"); m.put('5', "<#AA00AA>");
        m.put('6', "<#FFAA00>"); m.put('7', "<#AAAAAA>"); m.put('8', "<#555555>");
        m.put('9', "<#5555FF>"); m.put('a', "<#55FF55>"); m.put('b', "<#55FFFF>");
        m.put('c', "<#FF5555>"); m.put('d', "<#FF55FF>"); m.put('e', "<#FFFF55>");
        m.put('f', "<#FFFFFF>");
        m.put('l', "<bold>"); m.put('o', "<italic>"); m.put('m', "<strikethrough>");
        m.put('n', "<underlined>"); m.put('r', "<reset>");
        LEGACY_TO_MM = Collections.unmodifiableMap(m);
    }

    private CitemBuilder() {}

    // ── Entry points ──────────────────────────────────────────────────────────

    public static ItemStack build(String id, String rawJson) {
        return build(id, JsonParser.parseString(rawJson).getAsJsonObject());
    }

    public static ItemStack build(String id, JsonObject data) {
        // ── Material ─────────────────────────────────────────────────────────
        String matName = getString(data, "material", "PAPER");
        Material material = Material.matchMaterial(matName);
        if (material == null || material == Material.AIR) {
            Bukkit.getLogger().warning("[CitemBuilder] Unknown material: " + matName + ", falling back to PAPER");
            material = Material.PAPER;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // ── Display name ─────────────────────────────────────────────────────
        if (data.has("display_name") && !data.get("display_name").isJsonNull()) {
            meta.displayName(parseText(data.get("display_name").getAsString()));
        }

        // ── Lore ─────────────────────────────────────────────────────────────
        // Prefer lore_pages; fall back to lore array
        List<String> loreLines = new ArrayList<>();
        if (data.has("lore_pages") && data.get("lore_pages").isJsonArray()) {
            JsonArray pages = data.getAsJsonArray("lore_pages");
            // Show only page 0 initially
            if (pages.size() > 0 && pages.get(0).isJsonArray()) {
                for (JsonElement line : pages.get(0).getAsJsonArray()) {
                    loreLines.add(line.getAsString());
                }
            }
            // Store all pages in PDC for F-key cycling
            if (pages.size() > 1) {
                meta.getPersistentDataContainer().set(KEY_LORE_PAGES, PersistentDataType.STRING, pages.toString());
                meta.getPersistentDataContainer().set(KEY_LORE_PAGE, PersistentDataType.INTEGER, 0);
            }
        } else if (data.has("lore") && data.get("lore").isJsonArray()) {
            for (JsonElement line : data.getAsJsonArray("lore")) {
                loreLines.add(line.getAsString());
            }
        }

        meta.getPersistentDataContainer().set(Keys.ID.get(), PersistentDataType.STRING, id);

        meta.getPersistentDataContainer().set(Keys.UUUID.get(), PersistentDataType.STRING, data.get("update_uuid").getAsString().trim());

        if (data.has("flags") && data.get("flags").isJsonObject()) {
            JsonObject flags = data.getAsJsonObject("flags");
            // placeable: "small" → 1, "large" → 2, absent/other → 0
            int placeableVal = 0;
            if (flags.has("placeable") && !flags.get("placeable").isJsonNull()) {
                String p = flags.get("placeable").getAsString().trim().toLowerCase();
                if (p.equals("small")) placeableVal = 1;
                else if (p.equals("large")) placeableVal = 2;
            }
            meta.getPersistentDataContainer().set(Keys.PLACEABLE.get(), PersistentDataType.INTEGER, placeableVal);

            if (flags.has("undroppable") && !flags.get("undroppable").isJsonNull()) {
                boolean undroppable = flags.get("undroppable").getAsBoolean();
                meta.getPersistentDataContainer().set(Keys.UNDROPPABLE.get(), PersistentDataType.BOOLEAN, undroppable);
            }

            if (flags.has("unusable") && !flags.get("unusable").isJsonNull()) {
                boolean unusable = flags.get("unusable").getAsBoolean();
                meta.getPersistentDataContainer().set(Keys.UNUSABLE.get(), PersistentDataType.BOOLEAN, unusable);
            }
        }

        if (!loreLines.isEmpty()) {
            List<Component> loreComponents = new ArrayList<>();
            for (String line : loreLines) {
                loreComponents.add(parseText(line));
            }
            meta.lore(loreComponents);
        }

        // ── Enchanted glow (no real enchantments — just visual shimmer) ──────
        if (getBoolean(data, "enchanted", false)) {
            meta.setEnchantmentGlintOverride(true);
        }

        // ── Custom model / item model ─────────────────────────────────────────
        // Paper 1.21.4+: meta.setItemModel(NamespacedKey)
        // Fall back to legacy CustomModelData int if the value looks numeric
        if (data.has("model") && !data.get("model").isJsonNull()) {
            String modelStr = data.get("model").getAsString().trim();
            if (!modelStr.isEmpty()) {
                String ns = modelStr.contains(":") ? modelStr : NS + ":" + modelStr;
                meta.setItemModel(NamespacedKey.fromString(ns));
            }
        }

        // ── Dye color (leather armor, etc.) ─────────────────────────────────
        if (data.has("dye_color") && !data.get("dye_color").isJsonNull()
                && meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(parseColor(data.get("dye_color").getAsString()));
        }

        // ── Skull texture ─────────────────────────────────────────────────────
        if (data.has("skull_texture") && !data.get("skull_texture").isJsonNull()
                && meta instanceof SkullMeta skullMeta) {
            applySkullTexture(skullMeta, data.get("skull_texture").getAsString());
        }

        // ── Custom tooltip style ─────────────────────────────────────────────
        // Stored in PDC so the resource pack / plugin logic can read it
        if (data.has("tooltip") && !data.get("tooltip").isJsonNull()) {

            NamespacedKey tooltip = new NamespacedKey("minecraft", data.get("tooltip").getAsString());
            meta.setTooltipStyle(tooltip);
        }

        // ── Click interactions ────────────────────────────────────────────────
        if (data.has("action-left") && !data.get("action-left").isJsonNull()) {
            meta.getPersistentDataContainer().set(
                    Keys.LEFT_ACTION.get(), PersistentDataType.STRING, data.get("action-left").getAsString()
            );
        }
        if (data.has("action-right") && !data.get("action-right").isJsonNull()) {
            meta.getPersistentDataContainer().set(
                    Keys.RIGHT_ACTION.get(), PersistentDataType.STRING, data.get("action-right").getAsString()
            );
        }
        if (data.has("action-placed") && !data.get("action-placed").isJsonNull()) {
            meta.getPersistentDataContainer().set(
                    Keys.PLACED_ACTION.get(), PersistentDataType.STRING, data.get("action-placed").getAsString()
            );
        }

        // ── Attributes ────────────────────────────────────────────────────────
        if (data.has("attributes") && data.get("attributes").isJsonArray()) {
            applyAttributes(meta, data.getAsJsonArray("attributes"));
        }

        // ── Components ───────────────────────────────────────────────────────
        if (data.has("components") && data.get("components").isJsonObject()) {
            applyComponents(meta, item.getType(), data.getAsJsonObject("components"));
        }

        item.setItemMeta(meta);
        return item;
    }

    // ── Components ────────────────────────────────────────────────────────────

    private static void applyComponents(ItemMeta meta, Material mat, JsonObject components) {
        for (Map.Entry<String, JsonElement> entry : components.entrySet()) {
            String id = entry.getKey();
            JsonElement val = entry.getValue();

            switch (id) {
                // ── Display ───────────────────────────────────────────────────
                case "minecraft:rarity" -> {
                    try {
                        meta.setRarity(ItemRarity.valueOf(val.getAsString().toUpperCase()));
                    } catch (IllegalArgumentException ignored) {}
                }
                case "minecraft:enchantment_glint_override" ->
                        meta.setEnchantmentGlintOverride(
                                val.isJsonPrimitive() && !val.getAsString().equals("false")
                        );
                // TODO: Components?
                case "minecraft:hide_tooltip" ->
                        meta.setHideTooltip(true);
                case "minecraft:hide_additional_tooltip" ->
                        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                case "minecraft:item_name" -> {
                    // item_name stores a raw text component JSON string
                    String nameStr = val.getAsString();
                    meta.itemName(parseText(nameStr));
                }
                case "minecraft:tooltip_style" -> {
                    // Resource pack tooltip style key — store in PDC
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(NS, "mc_tooltip_style"),
                            PersistentDataType.STRING,
                            val.getAsString()
                    );
                }

                // ── Durability ────────────────────────────────────────────────
                case "minecraft:damage" -> {
                    if (meta instanceof Damageable d) d.setDamage(val.getAsInt());
                }
                case "minecraft:max_damage" -> {
                    if (meta instanceof Damageable d) d.setMaxDamage(val.getAsInt());
                }
                case "minecraft:unbreakable" -> {
                    meta.setUnbreakable(true);
                    // hide tooltip if show_in_tooltip is false
                    if (val.isJsonObject()) {
                        boolean show = getBooleanFromObj(val.getAsJsonObject(), "show_in_tooltip", true);
                        if (!show) meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    }
                }
                case "minecraft:repair_cost" -> {
                    if (meta instanceof Repairable r) r.setRepairCost(val.getAsInt());
                }

                // ── Stack & Use ───────────────────────────────────────────────
                case "minecraft:max_stack_size" ->
                        meta.setMaxStackSize(val.getAsInt());
                case "minecraft:use_cooldown" -> {
                    // Paper 1.21.2+ — store in PDC as fallback
                    if (val.isJsonObject()) {
                        JsonObject cd = val.getAsJsonObject();
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(NS, "use_cooldown_seconds"),
                                PersistentDataType.FLOAT,
                                getFloat(cd, "seconds", 1f)
                        );
                        if (cd.has("cooldown_group")) {
                            meta.getPersistentDataContainer().set(
                                    new NamespacedKey(NS, "use_cooldown_group"),
                                    PersistentDataType.STRING,
                                    cd.get("cooldown_group").getAsString()
                            );
                        }
                    }
                }

                // ── Enchantments ──────────────────────────────────────────────
                case "minecraft:enchantments" -> {
                    if (val.isJsonObject()) applyEnchantments(meta, val.getAsJsonObject(), false);
                }
                case "minecraft:stored_enchantments" -> {
                    if (val.isJsonObject() && meta instanceof EnchantmentStorageMeta esm) {
                        applyStoredEnchantments(esm, val.getAsJsonObject());
                    }
                }

                // ── Food ──────────────────────────────────────────────────────
                case "minecraft:food" -> {
                    if (val.isJsonObject()) applyFood(meta, val.getAsJsonObject());
                }

                // ── Tool ──────────────────────────────────────────────────────
                case "minecraft:tool" -> {
                    if (val.isJsonObject()) applyTool(meta, val.getAsJsonObject());
                }

                // ── Potion ────────────────────────────────────────────────────
                case "minecraft:potion_contents" -> {
                    if (val.isJsonObject() && meta instanceof PotionMeta pm) {
                        applyPotion(pm, val.getAsJsonObject());
                    }
                }

                // ── Trim ──────────────────────────────────────────────────────
                case "minecraft:trim" -> {
                    if (val.isJsonObject() && meta instanceof ArmorMeta am) {
                        applyTrim(am, val.getAsJsonObject());
                    }
                }

                // ── Banner / Shield ───────────────────────────────────────────
                case "minecraft:base_color" -> {
                    // setBaseColor removed in 1.20.5+; color is determined by material.
                    // Store in PDC so plugin logic can reference it if needed.
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(NS, "base_color"),
                            PersistentDataType.STRING,
                            val.getAsString()
                    );
                }
                case "minecraft:banner_patterns" -> {
                    if (val.isJsonArray() && meta instanceof BannerMeta bm) {
                        applyBannerPatterns(bm, val.getAsJsonArray());
                    }
                }

                // ── Map ───────────────────────────────────────────────────────
                case "minecraft:map_color" -> {
                    if (meta instanceof MapMeta mm2) {
                        mm2.setColor(Color.fromRGB(val.getAsInt()));
                    }
                }
                case "minecraft:map_id" -> {
                    if (meta instanceof MapMeta mm2) mm2.setMapId(val.getAsInt());
                }

                // ── Written book ──────────────────────────────────────────────
                case "minecraft:written_book_content" -> {
                    if (val.isJsonObject() && meta instanceof BookMeta bm) {
                        applyBook(bm, val.getAsJsonObject());
                    }
                }

                // ── Misc ──────────────────────────────────────────────────────
                case "minecraft:fire_resistant" ->
                        meta.setFireResistant(true);
                case "minecraft:glider" -> {
                    // Paper 1.21.2+ — no direct API yet in all versions; store in PDC
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(NS, "is_glider"),
                            PersistentDataType.BOOLEAN,
                            true
                    );
                }

                // ── Unknown — store raw JSON in PDC ───────────────────────────
                default -> {
                    String safeKey = id.replace(":", "_").replace("/", "_");
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(NS, "component_" + safeKey),
                            PersistentDataType.STRING,
                            val.toString()
                    );
                }
            }
        }
    }

    // ── Sub-appliers ──────────────────────────────────────────────────────────

    private static void applyAttributes(ItemMeta meta, JsonArray attrs) {
        for (JsonElement el : attrs) {
            if (!el.isJsonObject()) continue;
            JsonObject a = el.getAsJsonObject();

            String attrId  = getString(a, "attribute", "");
            String slotStr = getString(a, "slot", "any");
            double amount  = a.has("amount") ? a.get("amount").getAsDouble() : 0;
            String opStr   = getString(a, "operation", "add_value");
            String modId   = getString(a, "id", attrId + "_modifier");

            Attribute attribute = Registry.ATTRIBUTE.get(NamespacedKey.fromString(attrId));
            if (attribute == null) continue;

            AttributeModifier.Operation op = switch (opStr) {
                case "add_multiplied_base"  -> AttributeModifier.Operation.ADD_SCALAR;
                case "add_multiplied_total" -> AttributeModifier.Operation.MULTIPLY_SCALAR_1;
                default                     -> AttributeModifier.Operation.ADD_NUMBER;
            };

            EquipmentSlotGroup slot = switch (slotStr) {
                case "mainhand" -> EquipmentSlotGroup.MAINHAND;
                case "offhand"  -> EquipmentSlotGroup.OFFHAND;
                case "head"     -> EquipmentSlotGroup.HEAD;
                case "chest"    -> EquipmentSlotGroup.CHEST;
                case "legs"     -> EquipmentSlotGroup.LEGS;
                case "feet"     -> EquipmentSlotGroup.FEET;
                case "body"     -> EquipmentSlotGroup.BODY;
                default         -> EquipmentSlotGroup.ANY;
            };

            NamespacedKey modKey = NamespacedKey.fromString(modId.contains(":") ? modId : NS + ":" + modId);
            meta.addAttributeModifier(attribute,
                    new AttributeModifier(modKey, amount, op, slot));
        }
    }

    private static void applyEnchantments(ItemMeta meta, JsonObject enchants, boolean ignoreLevelRestriction) {
        for (Map.Entry<String, JsonElement> e : enchants.entrySet()) {
            Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.fromString(e.getKey()));
            if (ench != null) meta.addEnchant(ench, e.getValue().getAsInt(), true);
        }
    }

    private static void applyStoredEnchantments(EnchantmentStorageMeta meta, JsonObject enchants) {
        for (Map.Entry<String, JsonElement> e : enchants.entrySet()) {
            Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.fromString(e.getKey()));
            if (ench != null) meta.addStoredEnchant(ench, e.getValue().getAsInt(), true);
        }
    }

    private static void applyFood(ItemMeta meta, JsonObject food) {
        FoodComponent f = meta.getFood();
        f.setNutrition(getInt(food, "nutrition", 4));
        f.setSaturation(getFloat(food, "saturation", 2.4f));
        f.setCanAlwaysEat(getBoolean(food, "can_always_eat", false));
        meta.setFood(f);
    }

    private static void applyTool(ItemMeta meta, JsonObject tool) {
        ToolComponent t = meta.getTool();
        t.setDefaultMiningSpeed(getFloat(tool, "default_mining_speed", 1f));
        t.setDamagePerBlock(getInt(tool, "damage_per_block", 1));

        if (tool.has("rules") && tool.get("rules").isJsonArray()) {
            for (JsonElement ruleEl : tool.getAsJsonArray("rules")) {
                if (!ruleEl.isJsonObject()) continue;
                JsonObject rule = ruleEl.getAsJsonObject();
                String blocks = getString(rule, "blocks", "");
                float speed   = getFloat(rule, "speed", 1f);
                boolean drops = getBoolean(rule, "correct_for_drops", false);
                if (blocks.startsWith("#")) {
                    // Tag — e.g. #minecraft:mineable/pickaxe
                    String tagStr = blocks.substring(1);
                    NamespacedKey tagKey = NamespacedKey.fromString(tagStr);
                    if (tagKey != null) {
                        Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, tagKey, Material.class);
                        if (tag != null) {
                            t.addRule(tag, speed, drops);
                        }
                    }
                } else if (!blocks.isEmpty()) {
                    // Single block
                    Material mat = Material.matchMaterial(blocks);
                    if (mat != null) {
                        t.addRule(List.of(mat), speed, drops);
                    }
                }
            }
        }
        meta.setTool(t);
    }

    private static void applyPotion(PotionMeta pm, JsonObject pc) {
        if (pc.has("potion") && !pc.get("potion").isJsonNull()) {
            PotionType pt = Registry.POTION.get(NamespacedKey.fromString(pc.get("potion").getAsString()));
            if (pt != null) pm.setBasePotionType(pt);
        }
        if (pc.has("custom_color") && !pc.get("custom_color").isJsonNull()) {
            pm.setColor(parseColor(pc.get("custom_color").getAsString()));
        }
    }

    private static void applyTrim(ArmorMeta am, JsonObject trim) {
        TrimMaterial mat = Registry.TRIM_MATERIAL.get(
                NamespacedKey.fromString(getString(trim, "material", "minecraft:iron")));
        TrimPattern pat = Registry.TRIM_PATTERN.get(
                NamespacedKey.fromString(getString(trim, "pattern", "minecraft:sentry")));
        if (mat != null && pat != null) am.setTrim(new ArmorTrim(mat, pat));
    }

    private static void applyBannerPatterns(BannerMeta bm, JsonArray layers) {
        for (JsonElement el : layers) {
            if (!el.isJsonObject()) continue;
            JsonObject layer = el.getAsJsonObject();
            PatternType pt = Registry.BANNER_PATTERN.get(
                    NamespacedKey.fromString(getString(layer, "pattern", "minecraft:base")));
            DyeColor color;
            try { color = DyeColor.valueOf(getString(layer, "color", "white").toUpperCase()); }
            catch (IllegalArgumentException e) { color = DyeColor.WHITE; }
            if (pt != null) bm.addPattern(new org.bukkit.block.banner.Pattern(color, pt));
        }
    }

    private static void applyBook(BookMeta bm, JsonObject book) {
        if (book.has("title")) bm.setTitle(book.get("title").getAsString());
        if (book.has("author")) bm.setAuthor(book.get("author").getAsString());
        if (book.has("pages") && book.get("pages").isJsonArray()) {
            List<Component> pages = new ArrayList<>();
            for (JsonElement p : book.getAsJsonArray("pages")) {
                pages.add(parseText(p.getAsString()));
            }
            bm.pages(pages);
        }
        bm.setGeneration(BookMeta.Generation.ORIGINAL);
    }

    private static void applySkullTexture(SkullMeta meta, String textureUrl) {
        try {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(URI.create(textureUrl).toURL());
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CitemBuilder] Invalid skull texture URL: " + textureUrl);
        }
    }

    // ── Text parsing ──────────────────────────────────────────────────────────

    /**
     * Parses a string that may contain:
     *   - Legacy §/& color codes
     *   - <#RRGGBB> hex color tags
     *   - <gradient:#FROM:#TO>text</gradient>
     * All three are normalised to MiniMessage before parsing.
     */
    public static Component parseText(String raw) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        return MM.deserialize("<!italic>" + preprocessLegacy(raw));
    }

    /**
     * Converts §x / &x legacy codes to their MiniMessage equivalents so that
     * MiniMessage.deserialize() can handle everything in one pass.
     */
    private static String preprocessLegacy(String raw) {
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c == '§' || c == '&') && i + 1 < raw.length()) {
                char code = Character.toLowerCase(raw.charAt(i + 1));
                String mm = LEGACY_TO_MM.get(code);
                if (mm != null) {
                    sb.append(mm);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Color parseColor(String s) {
        if (s == null || s.isBlank()) return Color.WHITE;
        try {
            if (s.startsWith("#")) return Color.fromRGB(Integer.parseInt(s.substring(1), 16));
            return Color.fromRGB(Integer.parseInt(s)); // decimal
        } catch (NumberFormatException e) {
            return Color.WHITE;
        }
    }

    private static String getString(JsonObject o, String key, String def) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : def;
    }

    private static int getInt(JsonObject o, String key, int def) {
        return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsInt() : def;
    }

    private static float getFloat(JsonObject o, String key, float def) {
        return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsFloat() : def;
    }

    private static boolean getBoolean(JsonObject o, String key, boolean def) {
        return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsBoolean() : def;
    }

    private static boolean getBooleanFromObj(JsonObject o, String key, boolean def) {
        return getBoolean(o, key, def);
    }
}