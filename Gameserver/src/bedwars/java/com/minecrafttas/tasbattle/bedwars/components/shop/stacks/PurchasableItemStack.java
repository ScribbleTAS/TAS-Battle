package com.minecrafttas.tasbattle.bedwars.components.shop.stacks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.minecrafttas.tasbattle.gui.ClickableInventory.Interaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;

/**
 * Purchasable item stack in shop inventory
 */
public class PurchasableItemStack extends ItemStack {
	
	/**
	 * Price types for shop
	 */
	@RequiredArgsConstructor
	public enum Price {
		IRON("§f_ Iron", Material.IRON_INGOT), GOLD("§6_ Gold", Material.GOLD_INGOT), DIAMOND("§b_ Diamond", Material.DIAMOND), EMERALD("§2_ Emerald", Material.EMERALD);
		
		@Getter private final String displayName;
		@Getter private final Material type;
		
	}
	
	@Getter private final Price price;
	@Getter private final int priceAmount;
	
	/**
	 * Initialize purchasable item stack
	 * @param price Price type
	 * @param amount Price amount
	 * @param material Material of item
	 * @param count Item count
	 * @param name Item name
	 */
	public PurchasableItemStack(Price price, int amount, Material material, int count, String name) {
		super(material, count);
		this.price = price;
		this.priceAmount = amount;
		this.editMeta(e -> {
			e.displayName(Component.text("§f" + name));
			e.lore(Arrays.asList(Component.text("§7Cost: " + price.getDisplayName().replace("_", amount + ""))));
		});
	}
	
	/**
	 * Purchase the item stack
	 * @param p Player
	 * @return Was successful
	 */
	public boolean reward(Player p) {
		// add item to inventory
		var purchaseItemStack = this.clone();
		purchaseItemStack.lore(null);
		p.getInventory().addItem(purchaseItemStack);
		
		return true;
	}
	
	/**
	 * Create purchase interaction
	 * @return Interaction callback
	 */
	public Interaction purchase() {
		return p -> {
			// find available materials
			var items = p.getInventory().all(this.price.type);
			int available = 0;
			for (var item : items.entrySet())
				available += item.getValue().getAmount();
			
			// find and sort materials
			var itemStacks = new ArrayList<>(items.values());
			Collections.sort(itemStacks, (o1, o2) -> o2.getAmount() - o1.getAmount());
			Collections.reverse(itemStacks);
			
			if (available >= this.priceAmount && this.reward(p)) {
				
				// pay items
				int cost = this.priceAmount;
				for (var itemStack : itemStacks) {
					
					// remove items from inventory
					if (cost >= itemStack.getAmount()) {
						cost -= itemStack.getAmount();
						p.getInventory().removeItem(itemStack);
					} else {
						itemStack.setAmount(itemStack.getAmount() - cost);
						cost = 0;
					}
					
					if (cost <= 0)
						break;
				}
				
				// play sound
				p.playSound(Sound.sound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, Source.BLOCK, 0.3f, 2.0f));
			} else {
				// play sound
				p.playSound(Sound.sound(org.bukkit.Sound.BLOCK_ANVIL_LAND, Source.BLOCK, 0.3f, 1.0f));
			}
		};
	}
	
}