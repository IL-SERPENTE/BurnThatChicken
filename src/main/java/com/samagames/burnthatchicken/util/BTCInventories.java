package com.samagames.burnthatchicken.util;

import java.util.ArrayList;

import net.samagames.tools.RulesBook;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

/*
 * This file is part of BurnThatChicken.
 *
 * BurnThatChicken is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BurnThatChicken is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BurnThatChicken.  If not, see <http://www.gnu.org/licenses/>.
 */
public class BTCInventories {
    private static ItemStack book = null;

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
                new Enchantment[] {
                        Enchantment.ARROW_FIRE,
                        Enchantment.ARROW_INFINITE,
                        Enchantment.DURABILITY
                },
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
        if (book == null)
            book = new RulesBook("BurnThatChicken").addOwner("Rigner")
                    .addPage("Objectif", "Tuez tous les poulets avant qu'ils n'arrivent en bas !")
                    .addPage("Poulets spéciaux",
                            "Certains poulets vous donnent des bonus, repérez les !\n" +
                                    "Attention ils ne sont pas tous gentils...\n" +
                                    "(vous n'êtes pas obligés de les tuer)")
                    .toItemStack();
        inv.addItem(book);
    }

    public static void setItemMeta(ItemStack item, String name, String[] desc, Enchantment[] e, int[] l) {
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
