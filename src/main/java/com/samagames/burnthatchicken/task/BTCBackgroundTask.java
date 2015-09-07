package com.samagames.burnthatchicken.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.samagames.burnthatchicken.BTCMap.BTCGameZone;
import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.metadata.ChickenMetadataValue;
import com.samagames.burnthatchicken.metadata.SpecialChicken;
import com.samagames.burnthatchicken.util.BTCInventories;
import com.samagames.burnthatchicken.util.ChatUtils;
import com.samagames.burnthatchicken.util.GameState;

public class BTCBackgroundTask implements Runnable
{
	private int[] delays = new int[]{0, 4, 4, 4, 3, 3, 3, 3, 3, 2, 2, 2};
	private BTCPlugin main;
	private int delay;
	private Random random;
	private static BTCBackgroundTask instance;
	
	public BTCBackgroundTask(BTCPlugin main)
	{
		this.main = main;
		delay = -1;
		random = new Random();
		instance = this;
	}
	
	@Override
	public void run()
	{
		if (main.isDebug())
			return ;
		switch(main.getGameState())
		{
		case IN_GAME:
			int r = 0;
			for (int i = 0; i < delays.length; i++)
			{
				if (delay == r)
				{
					this.spawnChickens();
					break;
				}
				r += delays[i];
			}
			if (delay > r)
			{
				if ((delay - r) % 2 == 0 || random.nextInt(10) < 7)
					this.spawnChickens();
			}
			delay++;
			main.updateScoreBoard();
			break;
		case WAITING:
			if (Bukkit.getOnlinePlayers().size() < main.getCurrentMap().getMinPlayers())
			{
				Collection<? extends Player> list = Bukkit.getOnlinePlayers();
				for (Player p : list)
				{
					p.setLevel(0);
					ChatUtils.clearTitles(p);
				}
				delay = -1;
			}
			else
			{
				if (delay == -1)
					delay = 20;
				Collection<? extends Player> list = Bukkit.getOnlinePlayers();
				for (Player p : list)
					p.setLevel(delay);
				if (delay == 0)
				{
					ChatUtils.broadcastBigMessage(" ", 0, 20, 0);
					ChatUtils.broadcastSmallMessage(ChatColor.GOLD + "Début de la partie !", 0, 20, 0);
					ChatUtils.broadcastMessage(ChatUtils.getPluginPrefix() + "Début de la partie !");
					Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable(){
						@Override
						public void run()
						{
							ChatUtils.broadcastBigMessage(" ", 0, 40, 0);
							ChatUtils.broadcastSmallMessage(ChatColor.GOLD + "Tuez tout les poulets avant qu'ils ne tombent !", 0, 40, 0);
						}
					}, 20);
					selectPlayers();
					main.setGameState(GameState.IN_GAME);
					main.updateScoreBoard();
				}
				else if (delay % 10 == 0 || delay <= 5)
				{
					ChatUtils.broadcastBigMessage("", 0, 20, 0);
					ChatUtils.broadcastSmallMessage(ChatColor.GOLD + "Début de la partie dans " + delay + " secondes", 0, 20, 0);
					ChatUtils.broadcastMessage(ChatUtils.getPluginPrefix() + "Début de la partie dans " + delay + " secondes");
				}
			}
			if (delay > 0)
				delay--;
			break;
		case FINISHED:
			if (delay == 0)
			{
				main.setGameState(GameState.WAITING);
				ChatUtils.broadcastMessage(ChatUtils.getPluginPrefix() + "Expulsion des joueurs");
				for (Player p : Bukkit.getOnlinePlayers())
					p.kickPlayer(ChatColor.RED + "Expulsion du serveur");
				main.getPlayers().clear();
				main.loadNextMap();
			}
			else if (delay <= 5)
				ChatUtils.broadcastMessage(ChatUtils.getPluginPrefix() + "Expulsion des joueurs dans " + delay + " secondes");
			delay--;
		case INITIALIZING:
		default:
			break;
		}
	}
	
	private void selectPlayers()
	{
		Map<String, Integer> list = main.getPlayers();
		Set<String> p1 = list.keySet();
		ArrayList<String> players = new ArrayList<String>();
		for (String p : p1)
			players.add(p);
		for (String player : players)
		{
			try{
				Player p = Bukkit.getPlayer(player);
				if (p == null)
				{
					list.remove(player);
					continue ;
				}
				if (list.get(player) != -1)
					continue ;
				int n;
				do
				{
					n = (int)Math.abs((int)Math.abs(random.nextInt()) % main.getCurrentMap().getMaxPlayers());
				} while (list.containsValue(n));
				list.remove(player);
				list.put(player, n);
				p.teleport(main.getCurrentMap().getGameZones().get(n).getPlayerSpawn());
				if (!main.getCurrentMap().canPlayersMove())
					p.setWalkSpeed(0);
				p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
				BTCInventories.giveGameInventory(p);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		for (BTCGameZone zone : main.getCurrentMap().getGameZones())
		{
			if (main.getPlayers().containsValue(zone.getUniqueId()))
				zone.setEnded(false);
			else
				zone.setEnded(true);
		}
	}
	
	public void setDelay(int d)
	{
		delay = d;
	}
	
	private void spawnChickens()
	{
		List<BTCGameZone> zones = main.getCurrentMap().getGameZones();
		for (BTCGameZone zone : zones)
			spawnChicken(zone, true);
	}
	
	public void spawnChicken(BTCGameZone zone, boolean canSpecial)
	{
		if (zone.isEnded())
			return ;
		Location spawn = this.randomLocationInZone(zone.getChickenSpawnZone()[0], zone.getChickenSpawnZone()[1]);
		Chicken e = (Chicken)spawn.getWorld().spawnEntity(spawn, EntityType.CHICKEN);
		if (canSpecial && random.nextInt(15) == 0)
		{
			Location spawn2 = this.randomLocationInZone(zone.getChickenSpawnZone()[0], zone.getChickenSpawnZone()[1]);
			Chicken e2 = (Chicken)spawn2.getWorld().spawnEntity(spawn2, EntityType.CHICKEN);
			SpecialChicken[] list = SpecialChicken.values();
			SpecialChicken sc = list[random.nextInt(list.length)];
			e2.setBaby();
			ChickenMetadataValue.setMetadataValueToChicken(main, e2, zone.getUniqueId(), sc);
			e2.setHealth(1);
		}
		e.setAdult();
		ChickenMetadataValue.setMetadataValueToChicken(main, e, zone.getUniqueId(), null);
		e.setHealth(1);
	}
	
	private Location randomLocationInZone(Location l1, Location l2)
	{
		double x = random.nextDouble() * Math.abs(l1.getX() - l2.getX()) + Math.min(l1.getX(), l2.getX());
		double y = random.nextDouble() * Math.abs(l1.getY() - l2.getY()) + Math.min(l1.getY(), l2.getY());
		double z = random.nextDouble() * Math.abs(l1.getZ() - l2.getZ()) + Math.min(l1.getZ(), l2.getZ());
		Location newLoc = new Location(l1.getWorld(), x, y, z);
		if (newLoc.getWorld().getBlockAt(newLoc).getType() != Material.AIR)
			return randomLocationInZone(l1, l2);
		if (newLoc.getWorld().getBlockAt(newLoc.clone().add(0.3, 0, 0)).getType() != Material.AIR)
			return randomLocationInZone(l1, l2);
		if (newLoc.getWorld().getBlockAt(newLoc.clone().add(0, 0, 0.3)).getType() != Material.AIR)
			return randomLocationInZone(l1, l2);
		if (newLoc.getWorld().getBlockAt(newLoc.clone().add(-0.3, 0, 0)).getType() != Material.AIR)
			return randomLocationInZone(l1, l2);
		if (newLoc.getWorld().getBlockAt(newLoc.clone().add(0, 0, -0.3)).getType() != Material.AIR)
			return randomLocationInZone(l1, l2);
		return newLoc;
	}
	
	public static BTCBackgroundTask getInstance()
	{
		return instance;
	}
	
	public int getDelay()
	{
		return delay;
	}
}
