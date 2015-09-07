package com.samagames.burnthatchicken.task;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.samagames.burnthatchicken.BTCMap.BTCGameZone;
import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.metadata.ChickenMetadataValue;
import com.samagames.burnthatchicken.metadata.MetadataUtils;
import com.samagames.burnthatchicken.util.ChatUtils;
import com.samagames.burnthatchicken.util.ParticlesUtils;

public class BTCChickenChecker implements Runnable
{
	private BTCPlugin main;
	private static BTCChickenChecker instance;
	private Random random;
	
	public BTCChickenChecker(BTCPlugin main)
	{
		this.main = main;
		instance = this;
		random = new Random();
	}
	
	@Override
	public void run()
	{
		if (main.isDebug())
			return ;
		for (World w : Bukkit.getWorlds())
			for (Entity e : w.getEntities())
			{
				if (e.getType() != EntityType.CHICKEN || e.isDead())
					continue ;
				Object f = MetadataUtils.getMetaData(main, e, "btc-fake");  
				if (f != null && f instanceof Integer)
				{
					e.teleport(main.getFakeChickens().getChicken((Integer)f));
					e.setVelocity(new Vector(0, 0, 0));
					continue ;
				}
				ChickenMetadataValue meta = ChickenMetadataValue.getMetadataValueFromChicken(main, e);
				if (meta == null)
				{
					e.remove();
					continue ;
				}
				if (meta.isSpecial())
				{
					for (int i = 0; i < 4; i++)
					{
						double theta = 2 * Math.PI * random.nextDouble();
						double phi = Math.acos(2 * random.nextDouble() - 1);
						double x = e.getLocation().getX() + (1 * Math.sin(phi) * Math.cos(theta));
						double y = e.getLocation().getY() + (1 * Math.sin(phi) * Math.sin(theta));
						double z = e.getLocation().getZ() + (1 * Math.cos(phi));
						ParticlesUtils.sendParticleToPlayers(meta.getSpecialAttribute().getParticle(), (float)x, (float)y, (float)z);
					}
					for (BTCGameZone zone : main.getCurrentMap().getGameZones())
						if (zone.getUniqueId() == meta.getGameZoneId() && !zone.isEnded())
							if (zone.isInChickenEndZone(e.getLocation()))
								e.remove();
				}
				else for (BTCGameZone zone : main.getCurrentMap().getGameZones())
				{
					if (zone.getUniqueId() == meta.getGameZoneId() && !zone.isEnded())
						if (zone.isInChickenEndZone(e.getLocation()))
						{
							zone.setEnded(true);
							clearChicken(zone.getUniqueId());
							String player = main.getPlayerById(zone.getUniqueId());
							if (player == null)
								return ;
							main.getPlayers().remove(player);
							Player p = Bukkit.getPlayer(player);
							main.addPlayerToRank(player);
							if (p == null)
								return ;
							ChatUtils.sendBigMessage(p, "", 0, 100, 0);
							ChatUtils.sendSmallMessage(p, ChatColor.GOLD + "Vous avez perdu !", 0, 100, 0);
							main.getPlayers().put(player, -1);
							p.setWalkSpeed(0.2F);
							p.removePotionEffect(PotionEffectType.JUMP);
							p.setGameMode(GameMode.SPECTATOR);
							p.getInventory().clear();
							ChatUtils.broadcastMessage(ChatUtils.getPluginPrefix() + player + " est éliminé !");
							main.checkPlayers();
							main.updateScoreBoard();
						}
				}
			}
	}

	public void clearChicken(int zoneid)
	{
		for (World w : Bukkit.getWorlds())
			for (Entity e : w.getEntities())
			{
				if (e.getType() != EntityType.CHICKEN)
					continue ;
				if (MetadataUtils.getMetaData(main, e, "btc-fake") != null)
					continue ;
				ChickenMetadataValue meta = ChickenMetadataValue.getMetadataValueFromChicken(main, e);
				if (meta == null)
				{
					e.remove();
					continue ;
				}
				if (zoneid == meta.getGameZoneId())
					e.remove();
			}
	}
	
	public static BTCChickenChecker getInstance()
	{
		return instance;
	}
}
