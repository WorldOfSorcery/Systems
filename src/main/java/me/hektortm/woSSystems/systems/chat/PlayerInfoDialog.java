package me.hektortm.woSSystems.systems.chat;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.regions.RegionHandler;
import me.hektortm.woSSystems.utils.types.CosmeticType;
import me.hektortm.wosCore.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The dialog shown when a player clicks another player's name in chat.
 *
 * <p><b>To change what the dialog shows, edit {@link #rows()}.</b>  Each row is a
 * label plus a function that turns the viewed player into the value; rows are
 * shown top to bottom in list order and a row whose value is {@code null} is skipped.
 * Title, head and buttons are in {@link #build(Player)}.</p>
 */
public class PlayerInfoDialog {

    /** One "Label: value" line.  Return {@code null} from {@code value} to hide the row. */
    private record Row(String label, Function<Player, Component> value) {}

    private final DAOHub hub;
    private final NicknameManager nickManager;

    public PlayerInfoDialog(DAOHub hub, NicknameManager nickManager) {
        this.hub = hub;
        this.nickManager = nickManager;
    }

    // ── Edit here ────────────────────────────────────────────────────────

    private List<Row> rows() {
        return List.of(
                new Row("Username", p -> Component.text(p.getName())),
                new Row("Nickname", p -> {
                    String nick = nickManager.getNickname(p);
                    return nick == null ? null : Component.text(nick.replace("_", " "));
                }),
                new Row("Location", p -> Utils.parseColorCodes(RegionHandler.getRegionDisplayName(p))),
                new Row("Title", p -> cosmetic(p, CosmeticType.TITLE)),
                new Row("Badge", p -> cosmetic(p, CosmeticType.BADGE)),
                new Row("Gold", p -> Component.text(hub.getEconomyDAO().getPlayerCurrency(p.getUniqueId(), "gold"), NamedTextColor.YELLOW))
        );
    }

    /** Label colour for every row. */
    private static final NamedTextColor LABEL_COLOR = NamedTextColor.GRAY;

    // ── Building ─────────────────────────────────────────────────────────

    public Dialog build(Player target) {
        String prefix = hub.getCosmeticsDAO().getCurrentCosmetic(target, CosmeticType.PREFIX);
        String nick = nickManager.getNickname(target);
        String name = nick != null ? nick.replace("_", " ") : target.getName();
        Component title = Utils.parseColorCodes(prefix != null ? prefix + " " + name : name);

        List<DialogBody> body = new ArrayList<>();
        body.add(DialogBody.item(head(target)).showTooltip(false).build());
        body.add(DialogBody.plainMessage(lines(target)));

        return Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .body(body)
                        .build())
                .type(DialogType.notice(ActionButton.builder(Component.text("Close")).build())));
    }

    private Component lines(Player target) {
        Component text = Component.empty();
        boolean first = true;
        for (Row row : rows()) {
            Component value = row.value().apply(target);
            if (value == null) continue;
            if (!first) text = text.append(Component.newline());
            text = text.append(Component.text(row.label() + ": ", LABEL_COLOR)).append(value);
            first = false;
        }
        return text;
    }

    private Component cosmetic(Player p, CosmeticType type) {
        String value = hub.getCosmeticsDAO().getCurrentCosmetic(p, type);
        return value == null ? null : Utils.parseColorCodes(value);
    }

    private static ItemStack head(Player target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(target));
        return head;
    }
}
