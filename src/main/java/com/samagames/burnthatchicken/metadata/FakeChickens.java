package com.samagames.burnthatchicken.metadata;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.samagames.burnthatchicken.BTCPlugin;

public class FakeChickens
{
	private ArrayList<Location> list;
	private BTCPlugin main;
	
	public FakeChickens(BTCPlugin main)
	{
		this.main = main;
		list = new ArrayList<Location>();
		
		try{
			File file = new File("plugins/BurnThatChicken", "fakes.yml");
			if (file.exists())
			{
				YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
				int i = 0;
				while (yaml.get("fake-" + i) != null)
				{
					String w = yaml.getString("fake-" + i + ".world");
					World world = Bukkit.getWorld(w);
					double x = yaml.getDouble("fake-" + i + ".x", 0);
					double y = yaml.getDouble("fake-" + i + ".y", 0);
					double z = yaml.getDouble("fake-" + i + ".z", 0);
					int yaw = yaml.getInt("fake-" + i + ".yaw", 0);
					Location loc = new Location(world, x, y, z);
					loc.setYaw(yaw);
					list.add(loc);
					i++;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void addFakeChicken(Location location)
	{
		list.add(location);
	}
	
	public void spawnChickens()
	{
		for (Integer n = 0; n < list.size(); n++)
		{
			Location l = list.get(n);
			if (l.getBlock().getType() != Material.AIR)
				return ;
			Chicken chk = (Chicken)l.getWorld().spawnEntity(l, EntityType.CHICKEN);
			chk.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255));
			chk.setPassenger(l.getWorld().spawnEntity(l, EntityType.ENDER_CRYSTAL));
			MetadataUtils.setMetaData(main, chk, "btc-fake", n);
			MetadataUtils.setMetaData(main, chk.getPassenger(), "btc-fake", n);
		}
	}
	
	public Location getChicken(int n)
	{
		return list.get(n);
	}
}
