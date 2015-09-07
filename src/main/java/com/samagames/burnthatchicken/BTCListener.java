package com.samagames.burnthatchicken;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.samagames.burnthatchicken.BTCMap.BTCGameZone;
import com.samagames.burnthatchicken.metadata.ChickenMetadataValue;
import com.samagames.burnthatchicken.metadata.MetadataUtils;
import com.samagames.burnthatchicken.metadata.SpecialChicken;
import com.samagames.burnthatchicken.task.BTCChickenChecker;
import com.samagames.burnthatchicken.util.BTCInventories;
import com.samagames.burnthatchicken.util.ChatUtils;
import com.samagames.burnthatchicken.util.GameState;

@SuppressWarnings("deprecation")
public class BTCListener implements Listener
{
	private BTCPlugin main;
	
	public BTCListener(BTCPlugin main)
	{
		this.main = main;
	}
	
	@EventHandler
	public void onPreConnect(PlayerLoginEvent ev)
	{
		if (main.getGameState() != GameState.WAITING)
			ev.disallow(Result.KICK_OTHER, "Partie en cours");
		else if (main.getPlayers().size() >= main.getCurrentMap().getMaxPlayers())
			ev.disallow(Result.KICK_FULL, "Serveur plein");
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent ev)
	{
		ev.setQuitMessage(null);
		this.onDisconnect(ev.getPlayer());
	}
	
	@EventHandler
	public void onKicked(PlayerKickEvent ev)
	{
		ev.setLeaveMessage(null);
		this.onDisconnect(ev.getPlayer());
	}
	
	public void onDisconnect(Player player)
	{
		int id = -1;
		if (main.getPlayers().containsKey(player.getName()))
		{
			id = main.getPlayers().get(player.getName());
			main.getPlayers().remove(player.getName());
		}
		if (main.getGameState() == GameState.FINISHED)
			return ;
		Bukkit.broadcastMessage(ChatUtils.getPluginPrefix() + player.getName() + " a quitté la partie" + (main.getGameState() == GameState.WAITING ? " ! " + ChatColor.DARK_GRAY + "[" + ChatColor.RED +
				main.getPlayers().size() + ChatColor.DARK_GRAY + "/" + ChatColor.RED + main.getCurrentMap().getMaxPlayers() + ChatColor.DARK_GRAY + "]" : "."));
		if (main.getGameState() == GameState.IN_GAME)
		{
			main.addPlayerToRank(player.getName());
			if (id != -1)
			{
				for (BTCGameZone zone : main.getCurrentMap().getGameZones())
				{
					if (zone.getUniqueId() != id)
						continue ;
					zone.setEnded(true);
					BTCChickenChecker.getInstance().clearChicken(id);
				}
			}
			main.checkPlayers();
		}
		main.updateScoreBoard();
	}
	
	@EventHandler
	public void onConnect(PlayerJoinEvent ev)
	{
		Player p = ev.getPlayer();
		main.getPlayers().put(ev.getPlayer().getName(), -1);
		ev.setJoinMessage(null);
		Bukkit.broadcastMessage(ChatUtils.getPluginPrefix() + p.getName() + " a rejoint la partie ! " + ChatColor.DARK_GRAY + "[" + ChatColor.RED +
				main.getPlayers().size() + ChatColor.DARK_GRAY + "/" + ChatColor.RED + main.getCurrentMap().getMaxPlayers() + ChatColor.DARK_GRAY + "]");
		ChatUtils.setFooterAndHeader(p, ChatColor.AQUA + "" + ChatColor.BOLD + "BurnThatChicken", ChatColor.GOLD + "" + ChatColor.BOLD + "SamaGames");
		p.setMaxHealth(20);
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setFlying(false);
		p.setAllowFlight(false);
		p.setGameMode(GameMode.ADVENTURE);
		p.setWalkSpeed(0.2F);
		p.removePotionEffect(PotionEffectType.JUMP);
		p.teleport(main.getCurrentMap().getWaitingLobby());
		BTCInventories.giveLobbyInventory(p);
		main.updateScoreBoard();
	}
	
	@EventHandler
	public void onEntityDamagedByEntity(EntityDamageByEntityEvent ev)
	{
		if (main.getGameState() != GameState.IN_GAME || !(ev.getDamager() instanceof Arrow) || !(ev.getEntity() instanceof Chicken))
		{
			ev.setCancelled(true);
			return ;
		}
		Arrow arrow = (Arrow)ev.getDamager();
		Chicken chicken = (Chicken)ev.getEntity();
		ChickenMetadataValue data = ChickenMetadataValue.getMetadataValueFromChicken(main, chicken);
		if (data == null || arrow.getShooter() == null || !(arrow.getShooter() instanceof Player) || !main.getPlayers().containsKey(((Player)arrow.getShooter()).getName()))
		{
			ev.setCancelled(true);
			return ;
		}
		Player shooter = (Player)arrow.getShooter();
		int sid = main.getPlayers().get(shooter.getName());
		int cid = data.getGameZoneId();
		if (sid != cid)
		{
			ev.setCancelled(true);
			return ;
		}
		chicken.setHealth(0);
		arrow.remove();
		main.addChicken(shooter.getName());
		if (data.isSpecial())
		{
			main.addPowerUp(shooter.getName(), data.getSpecialAttribute(), data.getSpecialAttribute().getDuration());
			data.getSpecialAttribute().run(main, shooter);
		}
		main.updateScoreBoard();
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent ev)
	{
		if (main.getGameState() != GameState.IN_GAME || ev.getCause() != DamageCause.ENTITY_ATTACK)
		{
			ev.setCancelled(true);
			return ;
		}
	}
	
	@EventHandler
	public void onEntityDamageByBlock(EntityDamageByBlockEvent ev)
	{
		ev.setCancelled(true);
	}
	
	@EventHandler
	public void onFoodLevelChanged(FoodLevelChangeEvent ev)
	{
		if (ev.getEntity() instanceof Player)
			((Player)ev.getEntity()).setFoodLevel(20);
		ev.setCancelled(true);
	}
	
	@EventHandler
	public void onChat(PlayerChatEvent ev)
	{
		if (ev.isCancelled())
			return ;
		ev.setCancelled(true);
		Bukkit.broadcastMessage(ChatColor.GRAY + ev.getPlayer().getName() + ": " + ev.getMessage());
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent ev)
	{
		switch (ev.getEntityType())
		{
		case PLAYER:
		case CHICKEN:
			break ;
		default:
			ev.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent ev)
	{
		if (main.getCurrentMap().canPlayersMove())
			return ;
		if (main.getGameState() == GameState.IN_GAME && ev.getPlayer().getGameMode() == GameMode.ADVENTURE)
		{
		    Location to = ev.getFrom();
		    to.setY(ev.getTo().getY());
		    to.setYaw(ev.getTo().getYaw());
		    to.setPitch(ev.getTo().getPitch());
		    ev.setTo(to);
		}
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent ev)
	{
		if (ev.toWeatherState())
			ev.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerDrop(PlayerDropItemEvent ev)
	{
		ev.setCancelled(!ev.getPlayer().hasPermission("sg.btc.drop"));
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent ev)
	{
		ev.setCancelled(!ev.getPlayer().hasPermission("sg.btc.build"));
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent ev)
	{
		ev.setCancelled(!ev.getPlayer().hasPermission("sg.btc.build"));
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent ev)
	{
		ev.setCancelled(!ev.getWhoClicked().hasPermission("sg.btc.inv"));
	}
	
	@EventHandler
	public void onShoot(EntityShootBowEvent ev)
	{
		ev.getBow().setDurability((short)0);
	}
	
	@EventHandler
	public void onShoot(ProjectileLaunchEvent ev)
	{
		if (!(ev.getEntity() instanceof Arrow) || ev.getEntity().getShooter() == null || !(ev.getEntity().getShooter() instanceof Player) || MetadataUtils.getMetaData(main, ev.getEntity(), "btc-arrow2") != null)
			return ;
		if (main.getGameState() != GameState.IN_GAME)
			ev.setCancelled(true);
		else if (main.hasPowerUp(((Player)ev.getEntity().getShooter()).getName(), SpecialChicken.DOUBLE_ARROW))
		{
			double angle = Math.PI / 60;
			Arrow arrow1 = (Arrow)ev.getEntity();
			Vector vec = arrow1.getVelocity();
			Vector vec1 = new Vector(vec.getZ() * Math.sin(angle) + vec.getX() * Math.cos(angle), vec.getY(),
					vec.getZ() * Math.cos(angle) - vec.getX() * Math.sin(angle));
			angle *= -1;
			Vector vec2 = new Vector(vec.getZ() * Math.sin(angle) + vec.getX() * Math.cos(angle), vec.getY(),
					vec.getZ() * Math.cos(angle) - vec.getX() * Math.sin(angle));
			arrow1.setVelocity(vec1);
			Arrow arrow2 = arrow1.getWorld().spawnArrow(arrow1.getLocation().add(vec2.clone().multiply(0.3)), vec2, 0.6F, 12F);
			arrow2.setFireTicks(arrow1.getFireTicks());
			arrow2.setVelocity(vec2);
			arrow2.setShooter(arrow1.getShooter());
			MetadataUtils.setMetaData(main, arrow2, "btc-arrow2", "");
		}
	}
	
	@EventHandler
	public void onHit(ProjectileHitEvent ev)
	{
		ev.getEntity().remove();
	}
}
