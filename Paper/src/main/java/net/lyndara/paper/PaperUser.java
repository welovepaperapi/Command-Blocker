package net.lyndara.paper;

import net.lyndara.core.UserContext;
import org.bukkit.entity.Player;

public record PaperUser(Player player) implements UserContext {
    @Override
    public boolean hasPermission(String permission) { return player.hasPermission(permission); }

    @Override
    public void sendMessage(String message) { player.sendMessage(message.replace("&", "§")); }
}