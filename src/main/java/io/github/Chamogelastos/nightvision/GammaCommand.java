package io.github.Chamogelastos.nightvision;

import net.strokkur.commands.Aliases;
import net.strokkur.commands.Executes;
import net.strokkur.commands.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@net.strokkur.commands.Command("gamma")
@Aliases({"nv", "nightvision"})
@Permission("nightvision.use")
public class GammaCommand {

    private final NightVisionManager nightVisionManager;
    private final ConfigManager configManager;

    public GammaCommand(NightVisionManager nightVisionManager, ConfigManager configManager) {
        this.nightVisionManager = nightVisionManager;
        this.configManager = configManager;
    }

    @Executes
    public void onCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            Bukkit.getConsoleSender().sendMessage(configManager.getFormattedMessage(configManager.playerOnly));
            return;
        }

        boolean nightVisionActive = nightVisionManager.isNightVisionActive(((Player) sender).getUniqueId());
        var hasEffect = ((Player) sender).hasPotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);

        // If the effect is not active according to the plugin, enable it.
        if (!nightVisionActive) {
            nightVisionManager.toggleNightVision((Player) sender, configManager.showIcon);
            sender.sendMessage(configManager.getFormattedMessage(configManager.nightVisionEnabled));
        }
        // If the effect IS active, but the player somehow doesn't have it, apply it.
        else if (!hasEffect) {
            nightVisionManager.applyEffect((Player) sender, configManager.showIcon);
            sender.sendMessage(configManager.getFormattedMessage(configManager.nightVisionEnabled));
        }
        // If the effect is active and the player has it, disable it.
        else {
            nightVisionManager.toggleNightVision((Player) sender, configManager.showIcon);
            sender.sendMessage(configManager.getFormattedMessage(configManager.nightVisionDisabled));
        }

    }
}
