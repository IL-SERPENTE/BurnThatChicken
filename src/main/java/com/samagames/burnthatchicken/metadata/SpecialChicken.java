package com.samagames.burnthatchicken.metadata;

import net.minecraft.server.v1_9_R2.EnumParticle;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.samagames.burnthatchicken.BTCPlayer;
import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.task.BTCBackgroundTask;

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
public enum SpecialChicken {
    DOUBLE_ARROW(ChatColor.GREEN, EnumParticle.CRIT, 120, "Double flèches"),
    BLINDNESS(ChatColor.BLACK, EnumParticle.SMOKE_NORMAL, 20, "Aveugle"),
    MORE_CHICKENS(ChatColor.RED, EnumParticle.FLAME, -1, "Poulets x3");

    private String name;
    private ChatColor color;
    private EnumParticle particle;
    private int duration;

    SpecialChicken(ChatColor c, EnumParticle p, int d, String n) {
        color = c;
        particle = p;
        duration = d;
        name = n;
    }

    public ChatColor getColor() {
        return color;
    }

    public EnumParticle getParticle() {
        return particle;
    }

    public int getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public void run(BTCPlugin main, Player p) {
        BTCPlayer player = main.getGame().getPlayer(p.getUniqueId());
        if (player == null)
            return;
        switch (this) {
            case BLINDNESS:
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                        duration + 20, 1));
                break;
            case MORE_CHICKENS:
                BTCBackgroundTask task = BTCBackgroundTask.getInstance();
                if (player.getZone() != null)
                    for (int i = 0; i < 3; i++)
                        task.spawnChicken(player.getZone(), false);
                break ;
            default:
                break;
        }
    }
}
