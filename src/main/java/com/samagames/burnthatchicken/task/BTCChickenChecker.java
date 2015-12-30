package com.samagames.burnthatchicken.task;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.samagames.burnthatchicken.BTCMap.BTCGameZone;
import com.samagames.burnthatchicken.BTCPlayer;
import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.metadata.ChickenMetadataValue;
import com.samagames.burnthatchicken.util.ChatUtils;
import com.samagames.burnthatchicken.util.ParticlesUtils;

public class BTCChickenChecker implements Runnable {
	private BTCPlugin main;
	private static BTCChickenChecker instance;
	private Random random;

	public BTCChickenChecker(BTCPlugin main) {
		this.main = main;
		instance = this;
		random = new Random();
	}

	@Override
	public void run() {
		for (World w : Bukkit.getWorlds())
			for (Entity e : w.getEntities()) {
				if (e.getType() != EntityType.CHICKEN || e.isDead())
					continue;
				ChickenMetadataValue meta = ChickenMetadataValue
						.getMetadataValueFromChicken(main, e);
				if (meta == null) {
					e.remove();
					continue;
				}
				if (meta.isSpecial()) {
					for (int i = 0; i < 4; i++) {
						double theta = 2 * Math.PI * random.nextDouble();
						double phi = Math.acos(2 * random.nextDouble() - 1);
						double x = e.getLocation().getX()
								+ (1 * Math.sin(phi) * Math.cos(theta));
						double y = e.getLocation().getY()
								+ (1 * Math.sin(phi) * Math.sin(theta));
						double z = e.getLocation().getZ() + (1 * Math.cos(phi));
						ParticlesUtils.sendParticleToPlayers(meta
								.getSpecialAttribute().getParticle(),
								(float) x, (float) y, (float) z);
					}
					for (BTCGameZone zone : main.getCurrentMap().getGameZones())
						if (zone.getUniqueId() == meta.getGameZoneId()
								&& !zone.isEnded()
								&& zone.isInChickenEndZone(e.getLocation()))
							e.remove();
				} else
					for (BTCGameZone zone : main.getCurrentMap().getGameZones()) {
						if (zone.getUniqueId() == meta.getGameZoneId()
								&& !zone.isEnded()
								&& zone.isInChickenEndZone(e.getLocation())) {
							zone.setEnded(true);
							clearChicken(zone);
							BTCPlayer player = main.getPlayerByZone(zone
									.getUniqueId());
							if (player == null)
								return;
							player.setSpectator();
							main.addPlayerToRank(player);
							Player p = player.getPlayerIfOnline();
							ChatUtils.broadcastMessage(ChatUtils
									.getPluginPrefix()
									+ " "
									+ player.getName()
									+ " est éliminé !");
							if (p != null) {
								ChatUtils.sendBigMessage(p, "", 0, 100, 0);
								ChatUtils.sendSmallMessage(p, ChatColor.GOLD
										+ "Vous avez perdu !", 0, 100, 0);
							}
							main.checkPlayers();
							main.updateScoreBoard();
						}
					}
			}
	}

	public void clearChicken(BTCGameZone btcGameZone) {
		for (World w : Bukkit.getWorlds())
			for (Entity e : w.getEntities()) {
				if (e.getType() != EntityType.CHICKEN)
					continue;
				ChickenMetadataValue meta = ChickenMetadataValue
						.getMetadataValueFromChicken(main, e);
				if (meta == null
						|| btcGameZone.getUniqueId() == meta.getGameZoneId())
					e.remove();
			}
	}

	public static BTCChickenChecker getInstance() {
		return instance;
	}
}
