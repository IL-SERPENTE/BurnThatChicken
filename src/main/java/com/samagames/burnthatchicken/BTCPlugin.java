package com.samagames.burnthatchicken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.samagames.api.SamaGamesAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
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
	private static BTCPlugin instance;
	
	private BTCListener listener;
	private FakeChickens fakes;
	private BTCGame game;
	
	private BTCMap map;
	private SamaGamesAPI api;
	private HashMap<Integer, BTCPlayer> ranking;
	private ArrayList<PowerUpTask> powerups;
	
	@Override
	public void onEnable()
	{
		instance = this;
		
		game = new BTCGame(this);
		api = SamaGamesAPI.get();
		api.getGameManager().registerGame(game);
		map = BTCMap.loadMap(this);
		if (map == null)
		{
			getServer().shutdown();
			return ;
		}
		listener = new BTCListener(this);
		powerups = new ArrayList<PowerUpTask>();
		ranking = new HashMap<Integer, BTCPlayer>();
		fakes = new FakeChickens(this);
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BTCBackgroundTask(this), 20, 20);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BTCChickenChecker(this), 1, 1);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BTCNoSprintTask(this), 1, 1);
		getServer().getPluginManager().registerEvents(listener, this);
		
		fakes.spawnChickens();
		
		game.setGameState(GameState.WAITING);
	}
	
	public BTCGame getGame()
	{
		return game;
	}
	
	public BTCMap getCurrentMap()
	{
		return map;
	}
	
	public void checkPlayers()
	{
		int n = 0;
		List<BTCPlayer> list = new ArrayList<BTCPlayer>();
		list.addAll(game.getInGamePlayers().values());
		for (BTCPlayer player : list)
		{
			if (player.isSpectator() || player.isModerator())
				continue ;
			n++;
		}
		if (n < 2)
		{
			for (BTCPlayer player : list)
				if (player.isSpectator() || player.isModerator())
				{
					BTCGameZone zone = player.getZone();
					zone.setEnded(true);
					BTCChickenChecker.getInstance().clearChicken(zone);
					player.setSpectator();
					addPlayerToRank(player);
				}
			for (int p : ranking.keySet())
			{
				BTCPlayer who = ranking.get(p);
				Player player = who.getPlayerIfOnline();
				if (player != null)
				{
					ChatUtils.sendSmallMessage(player, ChatColor.AQUA + "Tu es " + p + (p == 1 ? "er" : "e"), 0, 100, 0);
					if (p == 1)
						who.addStars(1, "Victoire");
				}
			}
			ChatUtils.broadcastBigMessage(ChatColor.GOLD + "Fin de la partie", 0, 100, 0);
			ChatUtils.broadcastMessage(ChatUtils.getPluginPrefix() + " Fin de la partie");
			game.setGameState(GameState.FINISHED);
			game.handleGameEnd();
			if (ranking.size() <3) //Coeur dans le code OGM OGM OGM
				api.getGameManager().getCoherenceMachine().getTemplateManager().getPlayerWinTemplate().execute(ranking.get(1).getPlayer());
			else
				api.getGameManager().getCoherenceMachine().getTemplateManager().getPlayerLeaderboardWinTemplate().execute(
						ranking.get(1).getPlayer(),
						ranking.get(2).getPlayer(),
						ranking.get(3).getPlayer());
		}
	}
	
	public void addPlayerToRank(BTCPlayer player)
	{
		if (ranking.isEmpty())
			ranking.put(1, player);
		else
		{
			for (int i = ranking.size(); i >= 1; i--)
			{
				ranking.put(i + 1, ranking.get(i));
				ranking.remove(i);
			}
			ranking.put(1, player);
		}
	}
	
	public void updateScoreBoard()
	{
		int n = 0;
		List<BTCPlayer> list = new ArrayList<BTCPlayer>();
		list.addAll(game.getInGamePlayers().values());
		for (BTCPlayer player : list)
		{
			if (player.isSpectator() || player.isModerator())
				continue ;
			n++;
		}
		for (Player p : Bukkit.getOnlinePlayers())
		{
			Scoreboard sc = Bukkit.getScoreboardManager().getNewScoreboard();
			Objective obj = sc.registerNewObjective(ChatColor.GOLD + "≡ BTC ≡", "dummy");
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
			obj.getScore("").setScore(-1);
			if (game.getGameState() == GameState.WAITING)
				obj.getScore(ChatColor.GRAY + "Joueurs : " + ChatColor.WHITE + list.size()).setScore(-2);
			else
				obj.getScore(ChatColor.GRAY + "Joueurs : " + ChatColor.WHITE + n).setScore(-2);
			obj.getScore(" ").setScore(-3);
			if (game.getGameState() == GameState.IN_GAME || game.getGameState() == GameState.FINISHED)
			{
				int time = BTCBackgroundTask.getInstance().getDelay();
				int min = time / 60;
				int sec = time % 60;
				obj.getScore(ChatColor.GRAY + "Temps : " + ChatColor.WHITE + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec).setScore(-4);
				obj.getScore("  ").setScore(-5);
				BTCPlayer player = game.getPlayer(p.getUniqueId());
				if (player != null)
				{
					obj.getScore(ChatColor.GRAY + "Poulets : " + ChatColor.WHITE + player.getChickens()).setScore(-6);
					obj.getScore("   ").setScore(-7);
					obj.getScore(ChatColor.GRAY + "PowerUps :").setScore(-8);
					boolean ok = false;
					int i = -9;
					for (SpecialChicken sp : SpecialChicken.values())
					{
						if (this.hasPowerUp(p.getUniqueId(), sp))
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
			}
			p.setScoreboard(sc);
		}
	}
	
	public void addPowerUp(UUID player, SpecialChicken powerup, int duration)
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
	
	public boolean hasPowerUp(UUID player, SpecialChicken powerup)
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
	
	public FakeChickens getFakeChickens()
	{
		return fakes;
	}

	public BTCPlayer getPlayerByZone(int uniqueId)
	{
		List<BTCPlayer> list = new ArrayList<BTCPlayer>();
		list.addAll(game.getInGamePlayers().values());
		for (BTCPlayer player : list)
		{
			if (player.isSpectator() || player.isModerator())
				continue ;
			if (player.getZone() != null && player.getZone().getUniqueId() == uniqueId)
				return player;
		}
		return null;
	}
	
	public static BTCPlugin getInstance()
	{
		return instance;
	}
	
	public SamaGamesAPI getApi()
	{
		return api;
	}
}
