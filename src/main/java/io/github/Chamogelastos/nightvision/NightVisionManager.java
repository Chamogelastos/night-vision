package io.github.Chamogelastos.nightvision;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NightVisionManager {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public NightVisionManager(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    private List<UUID> activeUsers = new ArrayList<>();

    public void loadInitialUsers() {
        activeUsers.clear();
        activeUsers.addAll(databaseManager.getNightVisionUsers());
    }

    public void toggleNightVision(Player player, boolean showIcon) {
        var uuid = player.getUniqueId();
        if (activeUsers.contains(uuid)) {
            activeUsers.remove(uuid);
            removeEffect(player);
            databaseManager.removeNightVisionUser(uuid);
        } else {
            activeUsers.add(uuid);
            applyEffect(player, showIcon);
            databaseManager.removeNightVisionUser(uuid);
        }
    }

    public void applyEffect(Player player, boolean showIcon) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            return;
        }

        PotionEffect effect = new PotionEffect(
                PotionEffectType.NIGHT_VISION,
                Integer.MAX_VALUE,
                0,
                false,
                configManager.showParticles,
                showIcon
        );
        player.addPotionEffect(effect);
    }

    public void enableEffectOnJoin(Player player) {
        activeUsers.add(player.getUniqueId());
        applyEffect(player, configManager.showIcon);
    }

    public void removeEffect(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }

    public boolean isNightVisionActive(UUID uuid) {
        return activeUsers.contains(uuid);
    }

    public void removeUser(UUID uuid) {
        activeUsers.remove(uuid);
        databaseManager.removeNightVisionUser(uuid);
    }
}