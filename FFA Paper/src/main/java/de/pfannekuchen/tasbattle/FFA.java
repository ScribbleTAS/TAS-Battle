package de.pfannekuchen.tasbattle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.pfannekuchen.tasbattle.Configuration.Arena;
import de.pfannekuchen.tasbattle.Configuration.Combat;
import de.pfannekuchen.tasbattle.Configuration.Kit;
import de.pfannekuchen.tasbattle.util.BukkitSerialization;
import net.kyori.adventure.text.Component;

public class FFA extends JavaPlugin implements Listener, PluginMessageListener {

	/* Disable Block Placing and Damage in the Lobby World */
	@EventHandler public void onBlockBreak(BlockBreakEvent e) { if (e.getPlayer().getWorld().getName().equalsIgnoreCase("lobby") && !e.getPlayer().isOp()) e.setCancelled(true); }
	@EventHandler public void onBlockPlace(BlockPlaceEvent e) { if (e.getPlayer().getWorld().getName().equalsIgnoreCase("lobby") && !e.getPlayer().isOp()) e.setCancelled(true); }
	@EventHandler public void onDamage(EntityDamageEvent e) { if (e.getEntity().getWorld().getName().equalsIgnoreCase("lobby")) e.setCancelled(true); }
	/* Teleport Players that connect to the Lobby, or the spectating Lobby */
	@EventHandler public void onConnect(PlayerJoinEvent e) {
		// TODO: Spectator
		e.joinMessage(null);
		e.getPlayer().teleport(new Location(Bukkit.getWorld("lobby"), 0, 100, 0));
	}
	
	public static FFA PLUGIN;
	
	@Override
	public void onEnable() {
		PLUGIN = this;
		getDataFolder().mkdirs();
		Configuration.configFile = new File(getDataFolder(), "config.dat");
		try {
			Configuration.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "ffa:data");
		Bukkit.getMessenger().registerIncomingPluginChannel(this, "ffa:data", this);
		Bukkit.getPluginManager().registerEvents(this, this);
		super.onEnable();
	}

	/**
	 * Tab Completing for the FFA Command
	 */
	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
		if (args.length == 1) {
			return Arrays.asList("help", "arenas", "addarena", "setarena", "delarena", "togglecombat", "addkit", "delkit", "getkit", "selkit", "kits", "togglecommunity", "start", "stop", "info", "spectate");
		} else if (args.length == 2) {
			switch (args[0]) {
				case "setarena": 
					return Configuration.getInstance().arenas.stream().map(Arena::toString).toList();
				case "delarena": 
					return Configuration.getInstance().arenas.stream().map(Arena::toString).toList();
				case "togglecombat": 
					return Arrays.asList("OLD", "NEW", "DEFAULT");
				case "delkit": 
					return Configuration.getInstance().kits.stream().map(Kit::toString).toList();
				case "getkit": 
					return Configuration.getInstance().kits.stream().map(Kit::toString).toList();
				case "selkit": 
					return Configuration.getInstance().kits.stream().map(Kit::toString).toList();
			}
		}
		return super.onTabComplete(sender, command, alias, args);
	}
	
	/**
	 * The Command for FFA Managing
	 */
	@Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 0) return false;
		switch (args[0]) {
			case "help":
				sender.sendMessage(Component.text("�6� �7Displaying Help for /ffa\n"
						+ "�6� �7/ffa help - �bDisplays this help page\n"
						+ "�6� �7/ffa arenas - �bShows all arenas\n"
						+ "�6� �7/ffa addarena <name> - �bAdds an arena with <name> as worldname\n"
						+ "�6� �7/ffa setarena <name> - �bSets an arena with <name> as active\n"
						+ "�6� �7/ffa delarena <name> - �bRemoves an arena from the list\n"
						+ "�6� �7/ffa togglecombat <combat> - �bForces a specific combat system for the next game\n"
						+ "�6� �7/ffa addkit <name> - �bAdds your current inventory as a kit\n"
						+ "�6� �7/ffa delkit <name> - �bRemoves a kit registration\n"
						+ "�6� �7/ffa getkit <name> - �bLoads the kit into your inventory\n"
						+ "�6� �7/ffa selkit <name> - �bSelects a kit for the next game\n"
						+ "�6� �7/ffa kits - �bList all kits\n"
						+ "�6� �7/ffa togglecommunity - �bDisables or enables community voting\n"
						+ "�6� �7/ffa start - �bStarts the game now.\n"
						+ "�6� �7/ffa stop - �bStops the game now.\n"
						+ "�6� �7/ffa info - �bDisplays current settings.\n"
						+ "�6� �7/ffa spectate - �bSpectates the current game, instead of playing."));
				break;
			case "arenas":
				sender.sendMessage(Component.text("�6� �7There are " + Configuration.getInstance().arenas.size()  + " arenas registered."));
				for (Arena arena : Configuration.getInstance().arenas) sender.sendMessage(Component.text("�6� �7" + arena.name));
				break;
			case "addarena":
				if (args.length != 2) {
					sender.sendMessage(Component.text("�6� �7Enter a world name"));
					return true;
				}
				File world_dir = new File(getDataFolder().getParentFile().getParentFile(), args[1]);
				if (!world_dir.exists()) {
					sender.sendMessage(Component.text("�6� �7Enter a world name."));
					return true;
				}
				Arena ar = new Arena();
				ar.name = args[1];
				Configuration.getInstance().arenas.add(ar);
				sender.sendMessage(Component.text("�6� �7Arena created."));
				return true;
			case "setarena": 
				if (args.length != 2) {
					sender.sendMessage(Component.text("�6� �7What Arena to select?"));
					return true;
				}
				for (Arena arena : Configuration.getInstance().arenas) {
					if (arena.name.equalsIgnoreCase(args[1])) {
						sender.sendMessage(Component.text("�6� �7Arena selected."));
						Configuration.getInstance().currentArena = arena;
						return true;
					}
				}
				sender.sendMessage(Component.text("�6� �7What Arena to select?"));
				break;
			case "delarena": 
				if (args.length != 2) {
					sender.sendMessage(Component.text("�6� �7What Arena to remove?"));
					return true;
				}
				for (Arena arena : new ArrayList<>(Configuration.getInstance().arenas)) {
					if (arena.name.equalsIgnoreCase(args[1])) {
						Configuration.getInstance().arenas.remove(arena);
						sender.sendMessage(Component.text("�6� �7The Arena has been deleted."));
						return true;
					}
				}
				sender.sendMessage(Component.text("�6� �7What Arena to remove?"));
				break;
			case "togglecombat": 
				if (args.length == 2) {
					try {
						Combat c = Combat.valueOf(args[1].toUpperCase());
						// TODO: Update Combat Mode
						Configuration.getInstance().combatmode = c;
						Configuration.save();
						return true;
					} catch (Exception e) {}
				}
				sender.sendMessage(Component.text("�6� �7What Combat Mode? NEW, OLD, DEFAULT."));
				break;
			case "addkit": 
				if (args.length != 2) {
					sender.sendMessage(Component.text("�6� �7Enter a kit name"));
					return true;
				}
				Kit kt = new Kit();
				kt.name = args[1];
				kt.data = BukkitSerialization.playerInventoryToBase64(((Player) sender).getInventory());
				Configuration.getInstance().kits.add(kt);
				sender.sendMessage(Component.text("�6� �7Kit created."));
				break;
			case "delkit": 
				if (args.length != 2) {
					sender.sendMessage(Component.text("�6� �7What Kit to remove?"));
					return true;
				}
				for (Kit kit : new ArrayList<>(Configuration.getInstance().kits)) {
					if (kit.name.equalsIgnoreCase(args[1])) {
						Configuration.getInstance().kits.remove(kit);
						sender.sendMessage(Component.text("�6� �7The Kit has been deleted."));
						return true;
					}
				}
				sender.sendMessage(Component.text("�6� �7What Kit to remove?"));
				break;
			case "getkit": 
				if (args.length != 2) {
					sender.sendMessage(Component.text("�6� �7What Kit to get?"));
					return true;
				}
				for (Kit kit : Configuration.getInstance().kits) {
					if (kit.name.equalsIgnoreCase(args[1])) {
						sender.sendMessage(Component.text("�6� �7Kit loaded into your inventory."));
						try {
							BukkitSerialization.playerInventoryToBase64((Player) sender, kit.data);
						} catch (IllegalStateException | IOException e) {
							e.printStackTrace();
							sender.sendMessage(Component.text("�6� �7Kit data is corrupted. :("));
						}
						return true;
					}
				}
				sender.sendMessage(Component.text("�6� �7What Kit to get?"));
				break;
			case "selkit": 
				if (args.length != 2) {
					sender.sendMessage(Component.text("�6� �7What Kit to select?"));
					return true;
				}
				for (Kit kit : Configuration.getInstance().kits) {
					if (kit.name.equalsIgnoreCase(args[1])) {
						sender.sendMessage(Component.text("�6� �7Kit selected."));
						Configuration.getInstance().currentKit = kit;
						return true;
					}
				}
				sender.sendMessage(Component.text("�6� �7What Kit to select?"));
				break;
			case "kits": 
				sender.sendMessage(Component.text("�6� �7There are " + Configuration.getInstance().kits.size()  + " kits registered."));
				for (Kit kit : Configuration.getInstance().kits) sender.sendMessage(Component.text("�6� �7" + kit.name));
				break;
			case "togglecommunity":
				Configuration.getInstance().shouldAskCommunity = !Configuration.getInstance().shouldAskCommunity;
				if (Configuration.getInstance().shouldAskCommunity) sender.sendMessage(Component.text("�6� �7The Players can now decide what settings to play with."));	
				else sender.sendMessage(Component.text("�6� �7The Players are forced to play the selected settings."));
				break;
			case "start": 
				// TODO: Start the Game
				break;
			case "stop": 
				// TODO: End the Game
				break;
			case "info": 
				if (Configuration.getInstance().shouldAskCommunity) sender.sendMessage(Component.text("�6� �7There are no settings, because the Community can decide which Map/Kit to play with."));
				else {
					sender.sendMessage(Component.text("�6� �7Current Settings:"));
					sender.sendMessage(Component.text("�6� �7Map: " + Configuration.getInstance().currentArena));
					sender.sendMessage(Component.text("�6� �7Kit: " + Configuration.getInstance().currentKit));
				}
				break;
			case "spectate": 
				// TODO: Spectate a game
				break;
		}
		return true;
	}

	@Override
	public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
		if (!channel.equalsIgnoreCase("ffa:data")) return;
	}
	
}