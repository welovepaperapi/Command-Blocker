package net.lyndara.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import net.lyndara.core.CommandEngine;

public class CommandListener {
    private final CommandEngine engine;

    public CommandListener(CommandEngine engine) { this.engine = engine; }

    @Subscribe
    public void onCommand(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof com.velocitypowered.api.proxy.Player player)) return;

        if (!engine.isAllowed(event.getCommand(), new VelocityUser(player))) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            new VelocityUser(player).sendMessage("Dieser Befehl ist hier blockiert.");
        }
    }

    @Subscribe
    public void onTab(TabCompleteEvent event) {
        VelocityUser user = new VelocityUser(event.getPlayer());
        event.getSuggestions().removeIf(suggestion -> !engine.isAllowed(suggestion, user));
    }
}