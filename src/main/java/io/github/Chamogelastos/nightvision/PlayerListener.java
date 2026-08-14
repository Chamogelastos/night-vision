package io.github.Chamogelastos.nightvision;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener {

    private final NightVision plugin;
    private final NightVisionManager nightVisionManager;
    private final ConfigManager configManager;

    public PlayerListener(NightVision nightVision, NightVisionManager nightVisionManager, ConfigManager configManager) {
        this.plugin = nightVision;
        this.configManager = configManager;
        this.nightVisionManager = nightVisionManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (configManager.applyOnJoin) {
            if (player.hasPermission("nightvision.use")) {
                nightVisionManager.enableEffectOnJoin(player);
            }
            return;
        }

        if (nightVisionManager.isNightVisionActive(player.getUniqueId())) {
            if (!player.hasPermission("nightvision.use")) {
                nightVisionManager.removeUser(player.getUniqueId());
            } else {
                nightVisionManager.applyEffect(player, configManager.showIcon);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (nightVisionManager.isNightVisionActive(event.getPlayer().getUniqueId())) {
            nightVisionManager.removeEffect(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (configManager.preventMilkRemoval &&
                event.getItem().getType() == Material.MILK_BUCKET &&
                nightVisionManager.isNightVisionActive(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().getActivePotionEffects().forEach(effect -> {
                if (effect.getType() != PotionEffectType.NIGHT_VISION) {
                    event.getPlayer().removePotionEffect(effect.getType());
                }
            });
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (nightVisionManager.isNightVisionActive(event.getPlayer().getUniqueId())) {
            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, _ -> {
                nightVisionManager.applyEffect(event.getPlayer(), configManager.showIcon);
            }, 1L);
        }
    }
}