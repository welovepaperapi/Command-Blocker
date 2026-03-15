package net.lyndara.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;
import net.lyndara.core.CommandEngine;

import java.util.ArrayList;
import java.util.List;

public record CommandListener(CommandEngine engine, VelocityLauncher plugin) {

    @Subscribe(priority = 100)
    public void onCommand(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) return;
        VelocityUser user = new VelocityUser(player);

        if (engine.isAllowed("/" + event.getCommand(), user)) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            user.sendMessage(plugin.getBlockMessage());
        }
    }

    @Subscribe(priority = -100)
    public void onTab(TabCompleteEvent event) {
        VelocityUser user = new VelocityUser(event.getPlayer());
        List<String> suggestions = new ArrayList<>(event.getSuggestions());

        suggestions.removeIf(suggestion -> {
            String test = suggestion.startsWith("/") ? suggestion : "/" + suggestion;
            return engine.isAllowed(test, user);
        });

        event.getSuggestions().clear();
        event.getSuggestions().addAll(suggestions);
    }
}