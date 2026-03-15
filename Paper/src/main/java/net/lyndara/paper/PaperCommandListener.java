package net.lyndara.paper;

import net.lyndara.core.CommandEngine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;


public record PaperCommandListener(CommandEngine engine, PaperLauncher plugin) implements Listener {


    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        PaperUser user = new PaperUser(event.getPlayer());
        if (engine.isAllowed(event.getMessage(), user)) {
            event.setCancelled(true);
            user.sendMessage("&cDazu hast du keine Rechte!");
        }
    }


    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        PaperUser user = new PaperUser(event.getPlayer());

        event.getCommands().removeIf(command -> engine.isAllowed(command, user));
    }


    @EventHandler
    public void onTab(AsyncTabCompleteEvent event) {
        if (!(event.getSender() instanceof org.bukkit.entity.Player player)) return;
        PaperUser user = new PaperUser(player);

        event.getCompletions().removeIf(suggestion -> {
            String test = suggestion.startsWith("/") ? suggestion : "/" + suggestion;
            return engine.isAllowed(test, user);
        });
    }
}