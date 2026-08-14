package io.github.Chamogelastos.nightvision;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NightVision extends JavaPlugin {
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private NightVisionManager nightVisionManager;

    @Override
    public void onEnable() {
        try {
            configManager = new ConfigManager(this);
            databaseManager = new DatabaseManager(this, getDataFolder());
            nightVisionManager = new NightVisionManager(databaseManager, configManager);
            nightVisionManager.loadInitialUsers().thenRun(() -> {
                getServer().getGlobalRegionScheduler().run(this, _ -> {
                    for (Player player : getServer().getOnlinePlayers()) {
                        if (nightVisionManager.isNightVisionActive(player.getUniqueId())) {
                            if (!player.hasPermission("nightvision.use")) {
                                nightVisionManager.removeUser(player.getUniqueId());
                                continue;
                            }
                            nightVisionManager.applyEffect(player, configManager.showIcon);
                        }
                    }
                });
            });

            getServer().getPluginManager().registerEvents(new PlayerListener(this, nightVisionManager, configManager), this);

            this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(e -> {
                GammaCommandBrigadier.register(e.registrar(), nightVisionManager, configManager);
            }));
        } catch (Exception e) {
            getLogger().severe("NightVision failed to enable. A critical error occurred during setup.");
            getLogger().severe("e: " + e);
        }
    }

    @Override
    public void onDisable() {
        if (nightVisionManager != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                if (nightVisionManager.isNightVisionActive(player.getUniqueId())) {
                    nightVisionManager.removeEffect(player);
                }
            }
        }
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
    }

    public void reloadPlayerEffects() {
        for (Player player : this.getServer().getOnlinePlayers()) {
            if (nightVisionManager.isNightVisionActive(player.getUniqueId())) {
                nightVisionManager.removeEffect(player);
                nightVisionManager.applyEffect(player, configManager.showIcon);
            }
        }
    }
}