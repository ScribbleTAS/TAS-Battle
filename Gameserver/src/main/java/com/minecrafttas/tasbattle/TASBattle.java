package com.minecrafttas.tasbattle;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.minecrafttas.tasbattle.ffa.FFA;
import com.minecrafttas.tasbattle.gamemode.ModeManagement;
import com.minecrafttas.tasbattle.tickratechanger.TickrateChanger;

public class TASBattle extends JavaPlugin {

	/**
	 * Abstract gamemode module
	 */
	public static abstract class AbstractModule {

		/**
		 * Enable gamemode
		 * @param plugin Main Plugin
		 */
		public abstract void onEnable(TASBattle plugin);
		
		/**
		 * Execute command
		 * @param sender Command sender
		 * @param args Command parameters
		 */
		public abstract void onCommand(CommandSender sender, String[] args);
		
		/**
		 * Get command name
		 * @return Command name
		 */
		public abstract String getCommandName();
		
	}
	
	private TickrateChanger tickrateChanger;
	private ModeManagement modeManagement;

	/**
	 * Enable TAS Battle mod
	 */
	@Override
	public void onEnable() {
		this.tickrateChanger = new TickrateChanger();
		this.tickrateChanger.onEnable(this);
		
		this.modeManagement = new ModeManagement();
		this.modeManagement.onEnable(this);
		
		// init ffa for now
		new FFA().onEnable(this);
	}
	
	/**
	 * Handle TAS Battle command
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (this.tickrateChanger.getCommandName().equalsIgnoreCase(command.getName())) {
			this.tickrateChanger.onCommand(sender, args);
			return true;
		}
		
		return true;
	}

	/**
	 * Get Tickrate Changer instance
	 * @return Tickrate Changer instance
	 */
	public TickrateChanger getTickrateChanger() {
		return this.tickrateChanger;
	}

	/**
	 * Get Mode Management instance
	 * @return Mode Management instance
	 */
	public ModeManagement getModeManagement() {
		return this.modeManagement;
	}
	
}