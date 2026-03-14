package net.lyndara.paper;

import net.lyndara.core.CommandEngine;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

public class PaperLauncher extends JavaPlugin {

    private CommandEngine engine;

    @Override
    public void onEnable() {
        this.engine = new CommandEngine();

        engine.loadSettings(List.of("pl", "plugins", ".*:.*"), false);

        getServer().getPluginManager().registerEvents(new PaperCommandListener(engine), this);
        getLogger().info("CmmandBlocker (Paper) erfolgreich geladen!");
    }
}