package com.samagames.burnthatchicken.task;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;

import com.samagames.burnthatchicken.BTCMap.BTCGameZone;
import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.metadata.ChickenMetadataValue;
import com.samagames.burnthatchicken.metadata.SpecialChicken;

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
		switch(main.getGame().getGameState())
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
		default:
			break;
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
