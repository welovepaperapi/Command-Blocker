package net.lyndara.velocity;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.lyndara.core.UserContext;

public record VelocityUser(Player player) implements UserContext {
    @Override
    public boolean hasPermission(String permission) { return player.hasPermission(permission); }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }
}