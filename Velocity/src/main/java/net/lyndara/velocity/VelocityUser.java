package net.lyndara.velocity;


import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.lyndara.core.UserContext;

import java.util.UUID;

public record VelocityUser(Player player) implements UserContext {
    @Override
    public boolean hasPermission(String permission) { return player.hasPermission(permission); }

    @Override
    public void sendMessage(String message) { player.sendMessage(Component.text(message, NamedTextColor.RED)); }

    @Override
    public UUID getUniqueId() { return player.getUniqueId(); }
}