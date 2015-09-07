package com.samagames.burnthatchicken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.samagames.burnthatchicken.BTCMap.BTCGameZone;
import com.samagames.burnthatchicken.metadata.FakeChickens;
import com.samagames.burnthatchicken.metadata.SpecialChicken;
import com.samagames.burnthatchicken.task.BTCBackgroundTask;
import com.samagames.burnthatchicken.task.BTCChickenChecker;
import com.samagames.burnthatchicken.task.BTCNoSprintTask;
import com.samagames.burnthatchicken.task.PowerUpTask;
import com.samagames.burnthatchicken.util.ChatUtils;
import com.samagames.burnthatchicken.util.GameState;

public class BTCPlugin extends JavaPlugin
{
	private BTCListener listener;
	private HashMap<String, Integer> players;
	private HashMap<String, Integer> chickens;
	private GameState game_state;
	private ArrayList<BTCMap> maps;
	private int currentmap;
	private HashMap<Integer, String> ranking;
	private ArrayList<PowerUpTask> powerups;
	private FakeChickens fakes;
	private boolean debug;
	
	@Override
	public void onEnable()
	{
		game_state = GameState.INITIALIZING;
		
		debug = false;
		powerups = new ArrayList<PowerUpTask>();
		players = new HashMap<String, Integer>();
		chickens = new HashMap<String, Integer>();
		ranking = new HashMap<Integer, String>();
		maps = new ArrayList<BTCMap>();
		if (!BTCMap.loadMaps(maps))
			return ;
		currentmap = 0;
		fakes = new FakeChickens(this);
		listener = new BTCListener(this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BTCBackgroundTask(this), 20, 20);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BTCChickenChecker(this), 1, 1);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BTCNoSprintTask(this), 1, 1);
		Bukkit.getPluginManager().registerEvents(listener, this);
		
		Collection <? extends Player> players = Bukkit.getOnlinePlayers();
		for (Player p : players)
		{
			PlayerJoinEvent ev = new PlayerJoinEvent(p, "");
			listener.onConnect(ev);
		}
		
		for (World w : Bukkit.getWorlds())
		{
			for (Entity e : w.getEntities())
			{
				if (!(e instanceof Player))
					e.remove();
			}
		}
		fakes.spawnChickens();
		
		game_state = GameState.WAITING;
	}
	
	public void loadNextMap()
	{
		currentmap = (currentmap + 1) % maps.size();
	}
	
	public BTCMap getCurrentMap()
	{
		return maps.get(currentmap);
	}
	
	public GameState getGameState()
	{
		return game_state;
	}
	
	public void setGameState(GameState gs)
	{
		game_state = gs;
	}
	
	public Map<String, Integer> getPlayers()
	{
		return players;
	}
	
	public String getPlayerById(int id)
	{
		Set <String> list = players.keySet();
		for (String p : list)
			if (players.get(p) == id)
				return p;
		return null;
	}
	
	public void checkPlayers()
	{
		int n = 0;
		for (String p : players.keySet())
			if (players.get(p) >= 0)
				n++;
		if (n < 2)
		{
			ArrayList<String> list = new ArrayList<String>();
			for (String player : players.keySet())
				list.add(player);
			for (String player : list)
				if (players.get(player) >= 0)
				{
					BTCGameZone zone = getCurrentMap().getGameZones().get(players.get(player));
					zone.setEnded(true);
					BTCChickenChecker.getInstance().clearChicken(zone.getUniqueId());
					getPlayers().remove(player);
					Player p = Bukkit.getPlayer(player);
					addPlayerToRank(player);
					if (p == null)
						return ;
					players.put(player, -1);
					p.setWalkSpeed(0.2F);
					p.removePotionEffect(PotionEffectType.JUMP);
					p.setGameMode(GameMode.SPECTATOR);
					p.getInventory().clear();
				}
			for (int p : ranking.keySet())
			{
				String who = ranking.get(p);
				Player player = Bukkit.getPlayer(who);
				ChatUtils.sendSmallMessage(player, ChatColor.AQUA + "Tu es " + p + (p == 1 ? "er" : "e"), 0, 100, 0);
			}
			ChatUtils.broadcastBigMessage(ChatColor.GOLD + "Fin de la partie", 0, 100, 0);
			ChatUtils.broadcastMessage(ChatUtils.getPluginPrefix() + "Fin de la partie");
			ChatUtils.broadcastMessage(ChatColor.GOLD           + "+-----[ BurnThatChicken ]-----+");
			ChatUtils.broadcastMessage(ChatColor.GOLD           + "|");
			if (ranking.containsKey(1))
				ChatUtils.broadcastMessage(ChatColor.GOLD       + "|" + ChatColor.YELLOW + " 1er : " + ChatColor.GOLD + ranking.get(1));
			if (ranking.containsKey(2))
				ChatUtils.broadcastMessage(ChatColor.GOLD       + "|" + ChatColor.YELLOW + "   2e : " + ChatColor.GOLD + ranking.get(2));
			if (ranking.containsKey(3))
				ChatUtils.broadcastMessage(ChatColor.GOLD       + "|" + ChatColor.YELLOW + "     3e : " + ChatColor.GOLD + ranking.get(3));
			ChatUtils.broadcastMessage(ChatColor.GOLD           + "|");
			ChatUtils.broadcastMessage(ChatColor.GOLD           + "+-----------------------------+");
			ChatUtils.broadcastMessage(ChatUtils.getPluginPrefix() + "Expulsion des joueurs dans 15 secondes");
			BTCBackgroundTask.getInstance().setDelay(15);
			this.game_state = GameState.FINISHED;
			ranking.clear();
			chickens.clear();
		}
	}
	
	public void addPlayerToRank(String name)
	{
		if (ranking.isEmpty())
			ranking.put(1, name);
		else
		{
			for (int i = ranking.size(); i >= 1; i--)
			{
				ranking.put(i + 1, ranking.get(i));
				ranking.remove(i);
			}
			ranking.put(1, name);
		}
	}
	
	public void updateScoreBoard()
	{
		int n = 0;
		for (String p : players.keySet())
			if (players.get(p) != -1)
				n++;
		for (Player p : Bukkit.getOnlinePlayers())
		{
			Scoreboard sc = Bukkit.getScoreboardManager().getNewScoreboard();
			Objective obj = sc.registerNewObjective(ChatColor.GOLD + "≡ BTC ≡", "dummy");
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
			obj.getScore("").setScore(-1);
			if (game_state == GameState.WAITING)
				obj.getScore(ChatColor.GRAY + "Joueurs : " + ChatColor.WHITE + players.size()).setScore(-2);
			else
				obj.getScore(ChatColor.GRAY + "Joueurs : " + ChatColor.WHITE + n).setScore(-2);
			obj.getScore(" ").setScore(-3);
			if (game_state == GameState.IN_GAME || game_state == GameState.FINISHED)
			{
				int time = BTCBackgroundTask.getInstance().getDelay();
				int min = time / 60;
				int sec = time % 60;
				obj.getScore(ChatColor.GRAY + "Temps : " + ChatColor.WHITE + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec).setScore(-4);
				obj.getScore("  ").setScore(-5);
				obj.getScore(ChatColor.GRAY + "Poulets : " + ChatColor.WHITE + getChicken(p.getName())).setScore(-6);
				obj.getScore("   ").setScore(-7);
				obj.getScore(ChatColor.GRAY + "PowerUps :").setScore(-8);
				boolean ok = false;
				int i = -9;
				for (SpecialChicken sp : SpecialChicken.values())
				{
					if (this.hasPowerUp(p.getName(), sp))
					{
						obj.getScore(sp.getName()).setScore(i);
						i--;
						ok = true;
					}
				}
				if (!ok)
					obj.getScore("Aucun").setScore(i);
				obj.getScore("    ").setScore(ok ? i : i - 1);
			}
			p.setScoreboard(sc);
		}
	}
	
	public void addPowerUp(String player, SpecialChicken powerup, int duration)
	{
		if (duration != -1)
		{
			PowerUpTask task = new PowerUpTask(this, player, powerup);
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, task, duration);
			powerups.add(task);
		}
		Player p = Bukkit.getPlayer(player);
		if (p != null)
		{
			ChatUtils.sendBigMessage(p, " ", 0, 40, 0);
			ChatUtils.sendSmallMessage(p, ChatColor.AQUA + powerup.getName() + ChatColor.GOLD + " activé" + (duration == -1 ? "" : " pour " + (duration / 20) + " secondes !"), 0, 40, 0);
		}
	}
	
	public boolean hasPowerUp(String player, SpecialChicken powerup)
	{
		for (PowerUpTask n : powerups)
		{
			if (n.getName().equals(player) && n.getSpecial() == powerup)
				return true;
		}
		return false;
	}
	
	public void removePowerUp(PowerUpTask task)
	{
		for (int i = 0; i < powerups.size(); i++)
			if (powerups.get(i).equals(task))
			{
				powerups.remove(i);
				return ;
			}
	}
	
	public void addChicken(String name)
	{
		if (chickens.containsKey(name))
			chickens.put(name, chickens.get(name) + 1);
		else
			chickens.put(name, 1);
	}
	
	public int getChicken(String name)
	{
		if (chickens.containsKey(name))
			return chickens.get(name);
		chickens.put(name, 0);
		return 0;
	}
	
	public void setDebug(boolean d)
	{
		debug = d;
	}
	
	public boolean isDebug()
	{
		return debug;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (label.equalsIgnoreCase("debug") && sender.hasPermission("btc.debug"))
		{
			debug = !debug;
			sender.sendMessage("Debug : " + debug);
		}
		return true;
	}
	
	public FakeChickens getFakeChickens()
	{
		return fakes;
	}
}
