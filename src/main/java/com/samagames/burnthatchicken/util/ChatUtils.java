package com.samagames.burnthatchicken.util;

import java.util.Collection;

import net.samagames.api.SamaGamesAPI;

import net.samagames.tools.Titles;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatUtils {
    private ChatUtils() {
    }

    public static String getPluginPrefix() {
        return SamaGamesAPI.get().getGameManager().getCoherenceMachine().getGameTag();
    }

    public static void broadcastMessage(String msg) {
        Bukkit.getConsoleSender().sendMessage(msg);
        for (Player p : Bukkit.getOnlinePlayers())
            p.sendMessage(msg);
    }

    public static void broadcastTitle(String title, String subtitle, int fadein, int duration, int fadeout) {
        Collection<? extends Player> list = Bukkit.getOnlinePlayers();
        for (Player p : list)
            Titles.sendTitle(p, fadein, duration, fadeout, title, subtitle);
    }
}
