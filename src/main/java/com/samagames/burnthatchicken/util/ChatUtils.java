package com.samagames.burnthatchicken.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import net.samagames.api.SamaGamesAPI;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ChatUtils {
	private ChatUtils() {
	}

	public static String getPluginPrefix() {
		return SamaGamesAPI.get().getGameManager().getCoherenceMachine()
				.getGameTag();
	}

	public static void broadcastMessage(String msg) {
		Bukkit.getConsoleSender().sendMessage(msg);
		for (Player p : Bukkit.getOnlinePlayers())
			p.sendMessage(msg);
	}

	public static void sendBigMessage(Player p, String msg, int fadein,
			int duration, int fadeout) {
		IChatBaseComponent title = ChatSerializer
				.a("{'text': \"" + msg + "\"}");
		((CraftPlayer) p).getHandle().playerConnection
				.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TITLE, title));
		((CraftPlayer) p).getHandle().playerConnection
				.sendPacket(new PacketPlayOutTitle(fadein, duration, fadeout));
	}

	public static void sendSmallMessage(Player p, String msg, int fadein,
			int duration, int fadeout) {
		IChatBaseComponent title = ChatSerializer
				.a("{'text': \"" + msg + "\"}");
		((CraftPlayer) p).getHandle().playerConnection
				.sendPacket(new PacketPlayOutTitle(EnumTitleAction.SUBTITLE,
						title));
		((CraftPlayer) p).getHandle().playerConnection
				.sendPacket(new PacketPlayOutTitle(fadein, duration, fadeout));
	}

	public static void broadcastBigMessage(String msg, int fadein,
			int duration, int fadeout) {
		Collection<? extends Player> list = Bukkit.getOnlinePlayers();
		for (Player p : list)
			sendBigMessage(p, msg, fadein, duration, fadeout);
	}

	public static void broadcastSmallMessage(String msg, int fadein,
			int duration, int fadeout) {
		Collection<? extends Player> list = Bukkit.getOnlinePlayers();
		for (Player p : list)
			sendSmallMessage(p, msg, fadein, duration, fadeout);
	}
}
