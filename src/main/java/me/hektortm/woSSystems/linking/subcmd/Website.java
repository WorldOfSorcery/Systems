package me.hektortm.woSSystems.linking.subcmd;

import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Website extends SubCommand {
    @Override
    public String getName() {
        return "website";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage("Usage: /link website <code>");
            return;
        }


        String code = args[0];
        UUID uuid = p.getUniqueId();
        String username = p.getName();

        try {
            URL url = new URL("http://localhost:3001/api/minecraft/link/confirm");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String payload = String.format("{\"code\":\"%s\",\"mc_uuid\":\"%s\",\"mc_username\":\"%s\"}",
                    code, uuid, username);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                p.sendMessage("§aYour account has been successfully linked!");
            } else {
                p.sendMessage("§cLink failed. Please check the code and try again.");
            }

        } catch (Exception e) {
            p.sendMessage("§cAn error occurred while linking: " + e.getMessage());
        }
    }
}
