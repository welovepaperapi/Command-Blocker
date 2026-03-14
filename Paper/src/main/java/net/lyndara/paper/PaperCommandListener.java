package net.lyndara.paper;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.lyndara.core.CommandEngine;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PaperCommandListener implements Listener {
    private final CommandEngine engine;

    public PaperCommandListener(CommandEngine engine) {
        this.engine = engine;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        PaperUser user = new PaperUser(event.getPlayer());
        if (!engine.isAllowed(event.getMessage(), user)) {
            event.setCancelled(true);
            user.sendMessage("Dieser Befehl ist blockiert!");
        }
    }

    @EventHandler
    public void onTab(AsyncTabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player)) return;

        PaperUser user = new PaperUser(player);
        event.getCompletions().removeIf(completion -> !engine.isAllowed(completion, user));
    }
}