package net.lyndara.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.lyndara.core.CommandEngine;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;

@Plugin(
        id = "commandblocker",
        name = "CommandBlocker",
        version = "1.0",
        authors = {"WeLovePaperAPI"}
)
public class VelocityLauncher {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final CommandEngine engine;

    @Inject
    public VelocityLauncher(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.engine = new CommandEngine();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        engine.loadSettings(List.of("admin", "tp", "server"), false);

        server.getEventManager().register(this, new CommandListener(engine));
        logger.info("CmmandBlocker (Velocity) erfolgreich geladen!");
    }
}