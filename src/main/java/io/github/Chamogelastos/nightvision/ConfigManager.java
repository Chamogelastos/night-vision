package io.github.Chamogelastos.nightvision;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final NightVision plugin;

    private final File dataFolder;

    boolean showParticles = false;
    boolean showIcon = false;
    boolean preventMilkRemoval = true;
    boolean applyOnJoin = false;

    String prefix;
    String reloaded;
    String nightVisionDisabled;
    String nightVisionEnabled;
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
        showParticles = config.getBoolean("effects.show-particles", false);
        showIcon = config.getBoolean("effects.show-icon", false);
        preventMilkRemoval = config.getBoolean("effects.prevent-milk-removal", true);
        prefix = config.getString("messages.prefix", "<dark_gray>[<aqua>NightVision<dark_gray>] <r>");
        reloaded = config.getString("messages.reloaded", "<green>Configuration reloaded successfully.");
        nightVisionEnabled = config.getString("messages.night-vision-enabled", "<green>Night Vision has been enabled.");
        nightVisionDisabled = config.getString("messages.night-vision-disabled", "<red>Night Vision has been disabled.");
        playerOnly = config.getString("messages.player-only", "<red>This command can only be run by a player.");
        
        if (plugin.isEnabled()) {
            plugin.reloadPlayerEffects();
        }
    }

    public Component getFormattedMessage(String message) {
        return MiniMessage.miniMessage().deserialize(message);
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