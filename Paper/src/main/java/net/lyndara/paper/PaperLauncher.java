package net.lyndara.paper;

import net.lyndara.core.CommandEngine;
import net.lyndara.core.DatabaseManager;
import net.lyndara.core.PluginConstants;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PaperLauncher extends JavaPlugin {

    private CommandEngine engine;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.engine = new CommandEngine();
        this.databaseManager = new DatabaseManager();

        loadData();

        getServer().getMessenger().registerIncomingPluginChannel(this, PluginConstants.SYNC_CHANNEL, (channel, player, message) -> {
            String signal = new String(message);
            if (signal.equals(PluginConstants.RELOAD_SIGNAL)) {
                loadData();
                getLogger().info("Netzwerk-Reload empfangen: Befehlsfilter aktualisiert!");
            }
        });

        getServer().getPluginManager().registerEvents(new PaperCommandListener(engine, this), this);
        getLogger().info("CommandBlocker (Paper Slave) erfolgreich geladen!");
    }

    public void loadData() {
        reloadConfig();

        boolean useDb = getConfig().getBoolean("database.enabled", false);
        boolean isWhitelist = getConfig().getBoolean("settings.whitelist-mode", false);

        if (useDb) {
            databaseManager.connect(
                    getConfig().getString("database.host"),
                    getConfig().getInt("database.port"),
                    getConfig().getString("database.name"),
                    getConfig().getString("database.user"),
                    getConfig().getString("database.pass")
            );
            engine.loadSettings(databaseManager.getFilters(), isWhitelist);
        } else {
            List<String> filters = getConfig().getStringList("filters");
            engine.loadSettings(filters, isWhitelist);
        }

        Bukkit.getOnlinePlayers().forEach(org.bukkit.entity.Player::updateCommands);
    }

    @Override
    public void onDisable() {
        databaseManager.close();
    }
}