package net.lyndara.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.lyndara.core.CommandEngine;
import net.lyndara.core.DatabaseManager;
import net.lyndara.core.PluginConstants;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Plugin(id = "commandblocker", name = "CommandBlocker", version = "1.0", authors = {"WeLovePaperAPI"})
public class VelocityLauncher {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final CommandEngine engine;
    private final DatabaseManager databaseManager;
    private final MinecraftChannelIdentifier syncChannel = MinecraftChannelIdentifier.from(PluginConstants.SYNC_CHANNEL);

    private String blockMessage = "&cDieser Befehl ist auf dem Proxy blockiert.";

    @Inject
    public VelocityLauncher(ProxyServer server, Logger logger, @NotNull @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.engine = new CommandEngine();
        this.databaseManager = new DatabaseManager();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(syncChannel);
        loadConfig();

        server.getEventManager().register(this, new CommandListener(engine, this));

        // /vcreload
        CommandMeta reloadMeta = server.getCommandManager().metaBuilder("vcreload").plugin(this).build();
        server.getCommandManager().register(reloadMeta, (SimpleCommand) invocation -> {
            if (!invocation.source().hasPermission("commandfilter.admin")) {
                invocation.source().sendMessage(Component.text("Keine Proxy-Rechte!", NamedTextColor.RED));
                return;
            }
            sync();
            invocation.source().sendMessage(Component.text("Netzwerk-Konfiguration synchronisiert!", NamedTextColor.GREEN));
        });

        // /vblock
        CommandMeta blockMeta = server.getCommandManager().metaBuilder("vblock").plugin(this).build();
        server.getCommandManager().register(blockMeta, (SimpleCommand) inv -> {
            if (!inv.source().hasPermission("commandfilter.admin")) {
                inv.source().sendMessage(Component.text("Keine Proxy-Rechte!", NamedTextColor.RED));
                return;
            }
            if (inv.arguments().length < 1) {
                inv.source().sendMessage(Component.text("Nutze: /vblock <pattern>", NamedTextColor.RED));
                return;
            }
            databaseManager.addFilter(inv.arguments()[0]);
            sync();
            inv.source().sendMessage(Component.text("Befehl '" + inv.arguments()[0] + "' wurde global blockiert!", NamedTextColor.GREEN));
        });

        // /vunblock
        CommandMeta unblockMeta = server.getCommandManager().metaBuilder("vunblock").plugin(this).build();
        server.getCommandManager().register(unblockMeta, (SimpleCommand) inv -> {
            if (!inv.source().hasPermission("commandfilter.admin")) {
                inv.source().sendMessage(Component.text("Keine Proxy-Rechte!", NamedTextColor.RED));
                return;
            }
            if (inv.arguments().length < 1) {
                inv.source().sendMessage(Component.text("Nutze: /vunblock <pattern>", NamedTextColor.RED));
                return;
            }
            databaseManager.removeFilter(inv.arguments()[0]);
            sync();
            inv.source().sendMessage(Component.text("Befehl '" + inv.arguments()[0] + "' wurde wieder freigegeben.", NamedTextColor.YELLOW));
        });

        logger.info("CommandBlocker (Velocity Master) erfolgreich geladen!");
    }

    private void sync() {
        loadConfig();
        server.getAllServers().forEach(s ->
                s.sendPluginMessage(syncChannel, PluginConstants.RELOAD_SIGNAL.getBytes()));
    }

    @SuppressWarnings("unchecked")
    public void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) Files.createDirectories(dataDirectory);
            File configFile = getConfigFile();

            try (InputStream in = new FileInputStream(configFile)) {
                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(in);
                if (data == null) return;

                Map<String, Object> settings = (Map<String, Object>) data.get("settings");
                boolean isWhitelist = false;
                if (settings != null) {
                    this.blockMessage = (String) settings.getOrDefault("message", this.blockMessage);
                    isWhitelist = (boolean) settings.getOrDefault("whitelist-mode", false);
                }

                Map<String, Object> dbSettings = (Map<String, Object>) data.get("database");
                if (dbSettings != null && (boolean) dbSettings.getOrDefault("enabled", false)) {
                    databaseManager.connect(
                            (String) dbSettings.get("host"),
                            (int) dbSettings.get("port"),
                            (String) dbSettings.get("name"),
                            (String) dbSettings.get("user"),
                            (String) dbSettings.get("pass")
                    );
                    engine.loadSettings(databaseManager.getFilters(), isWhitelist);
                    logger.info("Filter erfolgreich aus MySQL geladen.");
                } else {
                    List<String> filters = (List<String>) data.get("filters");
                    engine.loadSettings(filters, isWhitelist);
                }
            }
        } catch (IOException e) {
            logger.error("Kritischer Fehler beim Laden der Master-Config!", e);
        }
    }

    private @NotNull File getConfigFile() {
        File file = dataDirectory.resolve("config.yml").toFile();
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("database:");
                writer.println("  enabled: false");
                writer.println("  host: \"localhost\"");
                writer.println("  port: 3306");
                writer.println("  name: \"minecraft\"");
                writer.println("  user: \"root\"");
                writer.println("  pass: \"\"");
                writer.println("settings:");
                writer.println("  whitelist-mode: false");
                writer.println("  message: \"&cBlockiert durch Proxy-Filter.\"");
                writer.println("filters:");
                writer.println("  - \"server\"");
            } catch (IOException e) {
                logger.error("Konnte Default-Config nicht erstellen", e);
            }
        }
        return file;
    }

    public String getBlockMessage() {
        return blockMessage;
    }
}