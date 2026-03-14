package net.lyndara.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.lyndara.core.CommandEngine;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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

    private String blockMessage = "&cDieser Befehl ist auf dem Proxy blockiert.";

    @Inject
    public VelocityLauncher(ProxyServer server, Logger logger, @NotNull @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.engine = new CommandEngine();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        loadConfig();

        server.getEventManager().register(this, new CommandListener(engine, this));

        CommandMeta meta = server.getCommandManager()
                .metaBuilder("vcreload")
                .build();

        server.getCommandManager().register(meta, (SimpleCommand) invocation -> {

            if (!invocation.source().hasPermission("commandfilter.admin")) {
                invocation.source().sendMessage(Component.text("Keine Rechte!", NamedTextColor.RED));
                return;
            }

            loadConfig();

            invocation.source().sendMessage(
                    Component.text("Konfiguration neu geladen!", NamedTextColor.GREEN)
            );
        });

        logger.info("CommandBlocker (Velocity) erfolgreich geladen!");
    }

    public void loadConfig() {

        try {

            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            File configFile = getConfigFile();

            try (InputStream in = new FileInputStream(configFile)) {

                Yaml yaml = new Yaml();
                Object loaded = yaml.load(in);

                if (!(loaded instanceof Map<?, ?> data)) {
                    return;
                }

                Object settingsObj = data.get("settings");

                boolean whitelistMode = false;
                List<String> filters = List.of();

                if (settingsObj instanceof Map<?, ?> settings) {

                    Object messageObj = settings.get("message");
                    if (messageObj instanceof String msg) {
                        blockMessage = msg;
                    }

                    Object whitelistObj = settings.get("whitelist-mode");
                    if (whitelistObj instanceof Boolean b) {
                        whitelistMode = b;
                    }
                }

                Object filtersObj = data.get("filters");

                if (filtersObj instanceof List<?> list) {
                    filters = list.stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .toList();
                }

                engine.loadSettings(filters, whitelistMode);
            }

        } catch (IOException e) {
            logger.error("Fehler beim Laden der Config!", e);
        }
    }

    private @NotNull File getConfigFile() {

        File configFile = dataDirectory.resolve("config.yml").toFile();

        if (!configFile.exists()) {

            try (PrintWriter writer = new PrintWriter(configFile)) {

                writer.println("settings:");
                writer.println("  whitelist-mode: false");
                writer.println("  message: \"&cDieser Befehl ist auf dem Proxy blockiert.\"");
                writer.println("");
                writer.println("filters:");
                writer.println("  - \"server\"");
                writer.println("  - \"glist\"");
                writer.println("  - \"velocity\"");
                writer.println("  - \".*:.*\"");

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return configFile;
    }

    public String getBlockMessage() {
        return blockMessage;
    }
}