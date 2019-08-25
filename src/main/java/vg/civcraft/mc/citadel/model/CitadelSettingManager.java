package vg.civcraft.mc.citadel.model;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.playerstate.InformationState;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.CommandReplySetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.DecimalFormatSetting;

public class CitadelSettingManager {

	private BooleanSetting byPass;
	private CommandReplySetting ctiNotReinforced;
	//private CommandReplySetting ctiAllied;
	private CommandReplySetting ctiEnemy;
	//private CommandReplySetting modeSwitch;
	private DecimalFormatSetting ctiPercentageHealth;
	private DecimalFormatSetting ctiReinforcementHealth;

	public CitadelSettingManager() {
		initSettings();
	}

	void initSettings() {
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("Citadel",
				"Citadel and reinforcement related settings");
		byPass = new BooleanSetting(Citadel.getInstance(), true, "Bypass", "citadelBypass",
				new ItemStack(Material.DIAMOND_PICKAXE),
				"Allows you to bypass reinforcements you have permission for and break them in a single break");
		PlayerSettingAPI.registerSetting(byPass, menu);

		MenuSection commandSection = menu.createMenuSection("Command replies",
				"Allows configuring the replies received when interacting with reinforcements or Citadel commands. For advanced users only");

		ctiNotReinforced = new CommandReplySetting(Citadel.getInstance(), ChatColor.YELLOW + "Not reinforced",
				"CTI Message Unreinforced", "citadel_cti_unreinforced", new ItemStack(Material.YELLOW_TERRACOTTA),
				"The message received when interacting with an unreinforced block in Information Mode");
		PlayerSettingAPI.registerSetting(ctiNotReinforced, commandSection);

		ctiPercentageHealth = new DecimalFormatSetting(Citadel.getInstance(), new DecimalFormat("#.##"),
				"Reinforcement Percentage Health Format", "citadel_cti_percentage_health",
				new ItemStack(Material.KNOWLEDGE_BOOK),
				"Decimal format used for displaying a percentage value for reinforcement health", 100.0 / 3);
		PlayerSettingAPI.registerSetting(ctiPercentageHealth, commandSection);

		ctiReinforcementHealth = new DecimalFormatSetting(Citadel.getInstance(), new DecimalFormat("0"),
				"Reinforcement Health Format", "citadel_cti_health", new ItemStack(Material.KNOWLEDGE_BOOK),
				"Decimal format used for displaying a reinforcements health", 100.0 / 3);
		PlayerSettingAPI.registerSetting(ctiReinforcementHealth, commandSection);

		ctiEnemy = new CommandReplySetting(Citadel.getInstance(),
				ChatColor.RED + "Reinforced at %%health_color%%%%perc_health%% (%%health%%/%%max_health%%)"
						+ ChatColor.RED + " health with " + ChatColor.AQUA + "%%%type%%%",
				"CTI Message Enemy", "citadel_cti_enemy", new ItemStack(Material.RED_TERRACOTTA),
				"The message received when interacting with enemy reinforcements");
		ctiEnemy.registerArgument("perc_health", "33.33", "the percentage health of the reinforcement");
		ctiEnemy.registerArgument("max_health", "50", "the maximum health of the reinforcement");
		ctiEnemy.registerArgument("health", "25", "the current health of the reinforcement");
		ctiEnemy.registerArgument("type", "Stone", "the type of the reinforcement");
		ctiEnemy.registerArgument("health_color", InformationState.getDamageColor(0.5).toString(),
				"a color representing the reinforcement health");
		PlayerSettingAPI.registerSetting(ctiEnemy, commandSection);
	}
	
	public void sendCtiEnemyMessage(Player player, Reinforcement reinforcement) {
		Map<String, String> args = new TreeMap<>();
		ReinforcementType type = reinforcement.getType();
		String percFormat = ctiPercentageHealth.getValue(player).format(reinforcement.getHealth() / type.getHealth() * 100);
		args.put("perc_health", percFormat);
		DecimalFormat reinHealthFormatter = ctiReinforcementHealth.getValue(player);
		args.put("health",reinHealthFormatter.format(reinforcement.getHealth()));
		args.put("max_health",reinHealthFormatter.format(type.getHealth()));
		args.put("type", type.getName());
		args.put("health_color", InformationState.getDamageColor(reinforcement.getHealth() / type.getHealth()).toString());
		CitadelUtility.sendAndLog(player, ChatColor.RESET, ctiEnemy.formatReply(player.getUniqueId(), args));
	}
}