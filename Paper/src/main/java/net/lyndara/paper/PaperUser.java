package net.lyndara.paper;

import net.kyori.adventure.text.Component;
import net.lyndara.core.UserContext;
import org.bukkit.entity.Player;

public record PaperUser(Player player) implements UserContext {

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    public void sendMessage(Component component) {
        player.sendMessage(component);
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(Component.text(message));
    }

}
