package com.samagames.burnthatchicken.util;

import java.util.Collection;

import net.samagames.api.SamaGamesAPI;

import net.samagames.tools.Titles;
import org.bukkit.Bukkit;
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
