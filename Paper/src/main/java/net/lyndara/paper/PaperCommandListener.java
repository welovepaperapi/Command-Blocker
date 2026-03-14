package net.lyndara.paper;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.lyndara.core.CommandEngine;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

public record PaperCommandListener(CommandEngine engine, PaperLauncher plugin) implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        PaperUser user = new PaperUser(event.getPlayer());
        if (engine.isAllowed(event.getMessage(), user)) {
            event.setCancelled(true);
            String rawMsg = plugin.getConfig().getString("settings.message", "&cBlockiert!");
            MiniMessage mm = MiniMessage.miniMessage();
            user.sendMessage(mm.deserialize(rawMsg));        }
    }

    @EventHandler
    public void onCommandListSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("commandblocker.bypass.*")) return;

        event.getCommands().removeIf(command -> {
            if (command.contains(":")) return true;

            return engine.isAllowed("/" + command, new PaperUser(player));
        });
    }

    @EventHandler
    public void onTab(AsyncTabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player)) return;
        if (player.hasPermission("commandblocker.bypass.*")) return;

        PaperUser user = new PaperUser(player);
        event.getCompletions().removeIf(completion -> {
            String test = completion.startsWith("/") ? completion : "/" + completion;
            return engine.isAllowed(test, user);
        });
    }
}