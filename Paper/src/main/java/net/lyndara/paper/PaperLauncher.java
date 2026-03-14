package net.lyndara.paper;

import net.lyndara.core.CommandEngine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class PaperLauncher extends JavaPlugin {

    private CommandEngine engine;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.engine = new CommandEngine();
        loadEngineSettings();

        getServer().getPluginManager().registerEvents(new PaperCommandListener(engine, this), this);
        getLogger().info("CmmandBlocker erfolgreich geladen!");
    }

    public void loadEngineSettings() {
        reloadConfig();
        List<String> filters = getConfig().getStringList("filters");
        boolean isWhitelist = getConfig().getBoolean("settings.whitelist-mode", false);
        engine.loadSettings(filters, isWhitelist);

        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            player.updateCommands();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (label.equalsIgnoreCase("cmreload")) {
            if (!sender.hasPermission("commandfilter.admin")) {
                sender.sendMessage("§cDazu hast du keine Rechte.");
                return true;
            }

            loadEngineSettings();
            sender.sendMessage("§a[CommandBlocker] Konfiguration wurde neu geladen!");
            return true;
        }
        return false;
    }
}