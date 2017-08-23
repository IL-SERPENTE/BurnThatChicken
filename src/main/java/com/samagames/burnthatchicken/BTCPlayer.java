package com.samagames.burnthatchicken;

import com.samagames.burnthatchicken.metadata.SpecialChicken;
import net.samagames.api.games.GamePlayer;

import net.samagames.tools.scoreboards.ObjectiveSign;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.samagames.burnthatchicken.BTCMap.BTCGameZone;
import com.samagames.burnthatchicken.util.BTCInventories;


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
 */public class BTCPlayer extends GamePlayer {
    private Player player;
    private BTCGameZone zone;
    private int chickens;
    private ObjectiveSign objective;
    private BTCGame game;

    public BTCPlayer(Player player) {
        super(player);
        this.player = player;
        zone = null;
        chickens = 0;
        this.objective = new ObjectiveSign("burnthatchicken", ChatColor.GOLD + "BTC" + ChatColor.WHITE + " | " + ChatColor.AQUA + "00:00");

        this.game = BTCPlugin.getInstance().getGame();
        this.updateScoreboard();
    }

    @Override
    public void handleLogin(boolean reconnect) {
        BTCPlugin main = BTCPlugin.getInstance();
        player.setMaxHealth(20);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setGameMode(GameMode.ADVENTURE);
        player.setWalkSpeed(0.2F);
        player.removePotionEffect(PotionEffectType.JUMP);
        player.teleport(main.getCurrentMap().getWaitingLobby());
        BTCInventories.giveLobbyInventory(player);
    }

    @Override
    public void setSpectator() {
        super.setSpectator();
        player.setWalkSpeed(0.2F);
        player.removePotionEffect(PotionEffectType.JUMP);
        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().clear();
    }

    public BTCGameZone getZone() {
        return zone;
    }

    public void setZone(BTCGameZone zone) {
        this.zone = zone;
    }

    public void addChicken() {
        chickens++;
        if (chickens % 5 == 0)
            this.addCoins(1, "5 poulets tués");
    }

    public String getName() {
        return player.getName();
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void handleLogout()
    {
        this.objective.removeReceiver(this.getOfflinePlayer());
    }

    public void updateScoreboard()
    {
        this.objective.setLine(0, " ");
        this.objective.setLine(1, ChatColor.GRAY + "Joueurs : " + ChatColor.WHITE + this.game.getInGamePlayers().size());
        this.objective.setLine(2, ChatColor.GRAY + "Poulets : " + ChatColor.WHITE + this.chickens);
        this.objective.setLine(3, "  ");
        this.objective.setLine(4, ChatColor.GRAY + "Bonus :");
        int i = 5;
        boolean ok = false;
        for (SpecialChicken sp : SpecialChicken.values())
            if (BTCPlugin.getInstance().hasPowerUp(this.uuid, sp)) {
                this.objective.setLine(i++, sp.getName());
                ok = true;
            }
        if (!ok)
            this.objective.setLine(i++, "Aucun");
        this.objective.setLine(i, "   ");
        this.objective.updateLines();
    }

    public void setScoreboard()
    {
        this.objective.addReceiver(this.getOfflinePlayer());
    }

    public void setScoreboardTime(String time)
    {
        this.objective.setDisplayName(ChatColor.GOLD + "BTC" + ChatColor.WHITE + " | " + ChatColor.AQUA + time);
        this.updateScoreboard();
    }
}
