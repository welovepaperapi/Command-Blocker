package net.lyndara.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.lyndara.core.CommandEngine;

import java.util.List;

public record CommandListener(CommandEngine engine, VelocityLauncher plugin) {

    @Subscribe(priority = 100)
    public void onCommand(CommandExecuteEvent event) {

        if (!(event.getCommandSource() instanceof Player player)) return;

        String label = event.getCommand().split(" ")[0].toLowerCase();
        VelocityUser user = new VelocityUser(player);

        if (!engine.isAllowed("/" + label, user)) {

            event.setResult(CommandExecuteEvent.CommandResult.denied());

            player.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand()
                            .deserialize(plugin.getBlockMessage())
            );
        }
    }

    @Subscribe(priority = -100)
    public void onTab(TabCompleteEvent event) {

        VelocityUser user = new VelocityUser(event.getPlayer());

        List<String> suggestions = event.getSuggestions();
        if (suggestions.isEmpty()) return;

        suggestions.removeIf(suggestion -> {

            String test = suggestion.startsWith("/") ? suggestion : "/" + suggestion;
            return !engine.isAllowed(test, user);

        });
    }
}
