package com.samagames.burnthatchicken.util;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class BTCInventories {
	private BTCInventories() {
	}

	public static void giveGameInventory(Player p) {
		PlayerInventory inv = p.getInventory();
		inv.clear();
		inv.setArmorContents(new ItemStack[] { new ItemStack(Material.AIR),
				new ItemStack(Material.AIR), new ItemStack(Material.AIR),
				new ItemStack(Material.AIR) });
		ItemStack bow = new ItemStack(Material.BOW);
		setItemMeta(bow, ChatColor.AQUA + "" + ChatColor.BOLD
				+ "Arc anti-poulet",
				new String[] { "Utilisez cet arc pour tuer les poulets" },
				new Enchantment[] { Enchantment.ARROW_FIRE,
						Enchantment.ARROW_INFINITE, Enchantment.DURABILITY },
				new int[] { 1, 1, 10 });
		ItemMeta meta = bow.getItemMeta();
		meta.spigot().setUnbreakable(true);
		bow.setItemMeta(meta);
		for (int i = 0; i < 9; i++)
			inv.setItem(i, bow);
		ItemStack arrow = new ItemStack(Material.ARROW);
		inv.setItem(31, arrow);
	}

	public static void giveLobbyInventory(Player p) {
		PlayerInventory inv = p.getInventory();
		inv.clear();
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		meta.addPage(
				"\n\n ]-------[ ]-------[\n       SamaGames\n    BurnThatChicken\n ]-------[ ]-------[\n\nObjectif :\n\nTuez tout les poulets avant qu'ils n'arrivent en bas !",
				"\nPoulets spéciaux :\n\nCertains poulets vous donnent des bonus, repérez les !\nAttention ils ne sont pas tous gentils...\n(vous n'êtes pas obligés de les tuer)");
		meta.setAuthor("Rigner");
		meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD
				+ "BurnThatChicken");
		book.setItemMeta(meta);
		inv.addItem(book);
	}

	public static void setItemMeta(ItemStack item, String name, String[] desc,
			Enchantment e[], int[] l) {
		ItemMeta m = item.getItemMeta();
		if (name != null)
			m.setDisplayName(name);
		ArrayList<String> lore = new ArrayList<>();
		if (desc != null) {
			for (String d : desc)
				if (d != null)
					lore.add(d);
		}
		m.setLore(lore);
		if (e != null && l != null) {
			for (int i = 0; i < e.length && i < l.length; i++) {
				m.addEnchant(e[i], l[i], true);
			}
		}
		item.setItemMeta(m);
	}
}
