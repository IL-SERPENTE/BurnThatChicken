package com.samagames.burnthatchicken;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.samagames.api.games.Game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.samagames.burnthatchicken.BTCMap.BTCGameZone;
import com.samagames.burnthatchicken.task.BTCChickenChecker;
import com.samagames.burnthatchicken.util.BTCInventories;
import com.samagames.burnthatchicken.util.ChatUtils;
import com.samagames.burnthatchicken.util.GameState;

public class BTCGame extends Game<BTCPlayer> {
	private GameState gamestate;
	private BTCPlugin main;

	public BTCGame(BTCPlugin plugin) {
		super("burnthatchicken", "BurnThatChicken", "", BTCPlayer.class);
		gamestate = GameState.INITIALIZING;
		main = plugin;
	}

	public GameState getGameState() {
		return gamestate;
	}

	public void setGameState(GameState gs) {
		gamestate = gs;
	}

	@Override
	public void startGame() {
		super.startGame();
		Bukkit.getScheduler()
				.scheduleSyncDelayedTask(
						main,
						() -> {
							ChatUtils.broadcastBigMessage(" ", 0, 40, 0);
							ChatUtils
									.broadcastSmallMessage(
											ChatColor.GOLD
													+ "Tuez tout les poulets avant qu'ils ne tombent !",
											0, 40, 0);
						}, 20);
		selectPlayers();
		main.getGame().setGameState(GameState.IN_GAME);
		main.updateScoreBoard();
	}

	private void selectPlayers() {
		Random random = new Random();
		List<BTCPlayer> list = new ArrayList<BTCPlayer>();
		list.addAll(getInGamePlayers().values());
		for (BTCPlayer player : list) {
			try {
				Player p = player.getPlayerIfOnline();
				if (p == null)
					continue;
				int n;
				BTCGameZone zone;
				do {
					n = Math.abs(random.nextInt()
							% gameManager.getGameProperties().getMaxSlots());
					zone = main.getCurrentMap().getGameZones().get(n);
				} while (zone.getPlayer() != null);
				player.setZone(zone);
				zone.setPlayer(player);
				p.teleport(zone.getPlayerSpawn());
				if (!main.getCurrentMap().canPlayersMove())
					p.setWalkSpeed(0);
				p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,
						Integer.MAX_VALUE, 128));
				BTCInventories.giveGameInventory(p);
			} catch (Exception e) {
				main.getServer().getLogger().severe(e.getMessage());
			}
		}
		for (BTCGameZone zone : main.getCurrentMap().getGameZones())
			zone.setEnded(zone.getPlayer() == null);
	}

	@Override
	public void handleLogout(Player player) {
		super.handleLogout(player);
		if (main.getGame().getGameState() == GameState.FINISHED)
			return;
		if (main.getGame().getGameState() == GameState.IN_GAME) {

			BTCPlayer btc = main.getGame().getPlayer(player.getUniqueId());
			if (btc == null)
				return;
			main.addPlayerToRank(btc);
			if (btc.getZone() != null) {
				btc.getZone().setEnded(true);
				BTCChickenChecker.getInstance().clearChicken(btc.getZone());
			}
			main.checkPlayers();
		}
		main.updateScoreBoard();
	}
}
