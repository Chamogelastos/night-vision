package io.github.Chamogelastos.nightvision;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;

public class ConfigManager {

    private final NightVision plugin;
    public Runnable onConfigReload;

    private final File dataFolder;

    boolean requirePermission = false;
    boolean showParticles = false;
    boolean showIcon = false;
    boolean preventMilkRemoval = true;
    boolean applyOnJoin = false;

    String prefix;
    String reloaded;
    String nightVisionDisabled;
    String nightVisionEnabled;
    String noPermission;
    String playerOnly;

    public ConfigManager(NightVision plugin) {
        this.plugin = plugin;
        dataFolder = plugin.getDataFolder();

        createDefaultConfig();
        loadConfig();
    }

    public void loadConfig() {
        File configFile = new File(dataFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        applyOnJoin = config.getBoolean("apply-on-join", false);
        requirePermission = config.getBoolean("require-permission", false);
        showParticles = config.getBoolean("effects.show-particles", false);
        showIcon = config.getBoolean("effects.show-icon", false);
        preventMilkRemoval = config.getBoolean("effects.prevent-milk-removal", true);
        prefix = config.getString("messages.prefix", "&8[&bNightVision&8] &r");
        reloaded = config.getString("messages.reloaded", "&aConfiguration reloaded successfully.");
        nightVisionEnabled = config.getString("messages.night-vision-enabled", "&aNight Vision has been enabled.");
        nightVisionDisabled = config.getString("messages.night-vision-disabled", "&cNight Vision has been disabled.");
        noPermission = config.getString("messages.no-permission", "&cYou do not have permission to use this command.");
        playerOnly = config.getString("messages.player-only", "&cThis command can only be run by a player.");
    }

    public String getFormattedMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    private WatchService watcher;

    public void startWatcher() throws IOException, InterruptedException {
        watcher = FileSystems.getDefault().newWatchService();

        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (ClosedWatchServiceException _) {
                break; // Exit loop if the service is closed
            }
            for (var event : key.pollEvents()) {
                var kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) continue;
                var fileName = event.context();
                if (Objects.equals(fileName.toString(), "config.yml")) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        loadConfig();
                        onConfigReload.run();
                    });
                }
            }
            if (!key.reset()) {
                break;
            }
        }

    }

    public void stopWatcher() throws IOException {
        watcher.close();
    }

    private void createDefaultConfig() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            var resourceStream = plugin.getResource("config.yml");
            if (resourceStream != null) {
                try (InputStreamReader reader = new InputStreamReader(resourceStream, StandardCharsets.UTF_8)) {
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
                    boolean changed = false;
                    for (String key : defaultConfig.getKeys(true)) {
                        if (!config.contains(key)) {
                            config.set(key, defaultConfig.get(key));
                            changed = true;
                        }
                    }
                    if (changed) {
                        try {
                            config.save(configFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}