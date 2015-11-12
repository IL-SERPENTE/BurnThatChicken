package com.samagames.burnthatchicken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public class BTCMap
{
	private int maxplayers;
	private int minplayers;
	private ArrayList<BTCGameZone> gamezones;
	private int id;
	private Location lobby;
	private boolean move;
	private boolean jump;
	
	public BTCMap(int maxplayers, int minplayers, Location lobby, boolean move, boolean jump)
	{
		this.maxplayers = maxplayers;
		this.minplayers = minplayers;
		this.id = 0;
		this.lobby = lobby;
		this.move = move;
		this.jump = jump;
		gamezones = new ArrayList<BTCGameZone>();
	}
	
	public boolean canPlayersJump()
	{
		return jump;
	}
	
	public boolean canPlayersMove()
	{
		return move;
	}
	
	public int getMaxPlayers()
	{
		return maxplayers;
	}
	
	public int getMinPlayers()
	{
		return minplayers;
	}
	
	public Location getWaitingLobby()
	{
		return lobby;
	}
	
	public boolean isReady()
	{
		return (gamezones.size() >= maxplayers && maxplayers > 0 && maxplayers >= minplayers && minplayers > 0);
	}
	
	public void addGameZone(Location p, Location s1, Location s2, Location e1, Location e2)
	{
		gamezones.add(new BTCGameZone(id, p, s1, s2, e1, e2));
		id++;
	}
	
	public List<BTCGameZone> getGameZones()
	{
		return gamezones;
	}
	
	public static BTCMap loadMap(BTCPlugin main)
	{
		File file = new File("plugins/BurnThatChicken", "map.yml");
		if (!file.exists())
			return null;
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		String w = yaml.getString("world", null);
		if (w == null)
			return null;
		World world = Bukkit.getWorld(w);
		if (world == null)
			return null;
		int n = yaml.getInt("max_players", -1);
		int mn = yaml.getInt("min_players", -1);
		double x = yaml.getDouble("lobby.x");
		double y = yaml.getDouble("lobby.y");
		double z = yaml.getDouble("lobby.z");
		int yaw = yaml.getInt("lobby.yaw");
		Location lobby = new Location(world, x, y, z);
		BTCMap g = new BTCMap(n, mn, lobby, yaml.getBoolean("move", false), yaml.getBoolean("jump", false));
		for (int j = 0; true; j++)
		{
			if (yaml.get("zone-" + j) == null)
				break ;
			x = yaml.getDouble("zone-" + j + ".spawn.x");
			y = yaml.getDouble("zone-" + j + ".spawn.y");
			z = yaml.getDouble("zone-" + j + ".spawn.z");
			yaw = yaml.getInt("zone-" + j + ".spawn.yaw");
			Location spawn = new Location(world, x, y, z);
			spawn.setYaw(yaw);

			x = yaml.getDouble("zone-" + j + ".spawnzone1.x");
			y = yaml.getDouble("zone-" + j + ".spawnzone1.y");
			z = yaml.getDouble("zone-" + j + ".spawnzone1.z");
			Location szone1 = new Location(world, x, y, z);

			x = yaml.getDouble("zone-" + j + ".spawnzone2.x");
			y = yaml.getDouble("zone-" + j + ".spawnzone2.y");
			z = yaml.getDouble("zone-" + j + ".spawnzone2.z");
			Location szone2 = new Location(world, x, y, z);

			x = yaml.getDouble("zone-" + j + ".endzone1.x");
			y = yaml.getDouble("zone-" + j + ".endzone1.y");
			z = yaml.getDouble("zone-" + j + ".endzone1.z");
			Location ezone1 = new Location(world, x, y, z);

			x = yaml.getDouble("zone-" + j + ".endzone2.x");
			y = yaml.getDouble("zone-" + j + ".endzone2.y");
			z = yaml.getDouble("zone-" + j + ".endzone2.z");
			Location ezone2 = new Location(world, x, y, z);
			
			g.addGameZone(spawn, szone1, szone2, ezone1, ezone2);
		}
		if (g.isReady())
			return g;
		else
			Bukkit.getLogger().severe("[BTC] Map incorrecte (joueurs max : " + g.getMaxPlayers() + ",joueurs min : " + g.getMinPlayers() + ", zones : " + g.getGameZones().size() + ").");
		return null;
	}
	
	public static class BTCGameZone
	{
		private Location spawn;
		private Location[] spawnzone;
		private Location[] endzone;
		private int id;
		private boolean end;
		private BTCPlayer player;
		
		public BTCGameZone(int i, Location s, Location z1, Location z2, Location e1, Location e2)
		{
			id = i;
			spawn = s;
			spawnzone = new Location[2];
			spawnzone[0] = z1;
			spawnzone[1] = z2;
			endzone = new Location[2];
			endzone[0] = e1;
			endzone[1] = e2;
			end = true;
			player = null;
		}
		
		public Location getPlayerSpawn()
		{
			return spawn;
		}
		
		public Location[] getChickenSpawnZone()
		{
			return spawnzone;
		}
		
		public Location[] getChickenEndZone()
		{
			return endzone;
		}
		
		public boolean isInChickenEndZone(Location loc)
		{
			if ((loc.getX() >= endzone[0].getX() && loc.getX() <= endzone[1].getX()) || (loc.getX() >= endzone[1].getX() && loc.getX() <= endzone[0].getX()))
				if ((loc.getY() >= endzone[0].getY() && loc.getY() <= endzone[1].getY()) || (loc.getY() >= endzone[1].getY() && loc.getY() <= endzone[0].getY()))
					if ((loc.getZ() >= endzone[0].getZ() && loc.getZ() <= endzone[1].getZ()) || (loc.getZ() >= endzone[1].getZ() && loc.getZ() <= endzone[0].getZ()))
						return true;
			return false;
		}
		
		public void setEnded(boolean e)
		{
			end = e;
		}
		
		public boolean isEnded()
		{
			return end;
		}
		
		public int getUniqueId()
		{
			return id;
		}
		
		public void setPlayer(BTCPlayer player)
		{
			this.player = player;
		}
		
		public BTCPlayer getPlayer()
		{
			return player;
		}
	}
}
