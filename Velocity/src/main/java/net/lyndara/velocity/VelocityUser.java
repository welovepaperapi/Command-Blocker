package net.lyndara.velocity;


import com.velocitypowered.api.proxy.Player;
import net.lyndara.core.UserContext;

public record VelocityUser(Player player) implements UserContext {
    @Override
    public boolean hasPermission(String permission) { return player.hasPermission(permission); }

}