package com.samagames.burnthatchicken.util;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ParticlesUtils
{
	public static void sendParticleToPlayers(EnumParticle particule, float x, float y, float z)
	{
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particule, true, x, y, z, 0F, 0F, 0F, 0F, 1, 1);
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (!(p instanceof CraftPlayer))
				return ;
			((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
		}
	}
}
