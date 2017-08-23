package com.samagames.burnthatchicken.util;

import net.minecraft.server.v1_9_R2.EnumParticle;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

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
public class ParticlesUtils {
    private ParticlesUtils() {
    }

    public static void sendParticleToPlayers(EnumParticle particule, float x, float y, float z) {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                particule, true, x, y, z, 0F, 0F, 0F, 0F, 1, 1);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!(p instanceof CraftPlayer))
                return;
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }
}
