package com.samagames.burnthatchicken;

import net.samagames.api.games.GamePlayer;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.samagames.burnthatchicken.BTCMap.BTCGameZone;
import com.samagames.burnthatchicken.util.BTCInventories;

public class BTCPlayer extends GamePlayer {
	private Player player;
	private BTCGameZone zone;
	private int chickens;

	public BTCPlayer(Player player) {
		super(player);
		this.player = player;
		zone = null;
		chickens = 0;
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
		main.updateScoreBoard();
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

	public int getChickens() {
		return chickens;
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
}
