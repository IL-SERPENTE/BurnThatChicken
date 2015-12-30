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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.util.Vector;

import com.samagames.burnthatchicken.metadata.ChickenMetadataValue;
import com.samagames.burnthatchicken.metadata.MetadataUtils;
import com.samagames.burnthatchicken.metadata.SpecialChicken;
import com.samagames.burnthatchicken.util.GameState;

public class BTCListener implements Listener {
	private BTCPlugin main;

	public BTCListener(BTCPlugin main) {
		this.main = main;
	}

	@EventHandler
	public void onEntityDamagedByEntity(EntityDamageByEntityEvent ev) {
		if (main.getGame().getGameState() != GameState.IN_GAME
				|| !(ev.getDamager() instanceof Arrow)
				|| !(ev.getEntity() instanceof Chicken)) {
			ev.setCancelled(true);
			return ;
		}
		Arrow arrow = (Arrow) ev.getDamager();
		Chicken chicken = (Chicken) ev.getEntity();
		ChickenMetadataValue data = ChickenMetadataValue
				.getMetadataValueFromChicken(main, chicken);
		if (data == null || arrow.getShooter() == null
				|| !(arrow.getShooter() instanceof Player)) {
			ev.setCancelled(true);
			return ;
		}
		Player shooter = (Player) arrow.getShooter();
		BTCPlayer player = main.getGame().getPlayer(shooter.getUniqueId());
		if (player == null || player.getZone().getUniqueId() != data.getGameZoneId()) {
			ev.setCancelled(true);
			return ;
		}
		chicken.setHealth(0);
		arrow.remove();
		player.addChicken();
		if (data.isSpecial()) {
			main.addPowerUp(shooter.getUniqueId(), data.getSpecialAttribute(),
					data.getSpecialAttribute().getDuration());
			data.getSpecialAttribute().run(main, shooter);
		}
		main.updateScoreBoard();
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent ev) {
		if (main.getGame().getGameState() == GameState.WAITING
				&& ev.getEntity() instanceof Player
				&& ev.getCause() == DamageCause.VOID) {
			ev.setCancelled(true);
			ev.getEntity().teleport(main.getCurrentMap().getWaitingLobby());
			return;
		}
		if (main.getGame().getGameState() != GameState.IN_GAME
				|| ev.getCause() != DamageCause.ENTITY_ATTACK) {
			ev.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onEntityDamageByBlock(EntityDamageByBlockEvent ev) {
		ev.setCancelled(true);
	}

	@EventHandler
	public void onFoodLevelChanged(FoodLevelChangeEvent ev) {
		if (ev.getEntity() instanceof Player)
			((Player) ev.getEntity()).setFoodLevel(20);
		ev.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent ev) {
		ev.setCancelled(true);
		String msg = ChatColor.GRAY + ev.getPlayer().getDisplayName()
				+ ChatColor.WHITE + ": " + ev.getMessage();
		for (Player p : Bukkit.getOnlinePlayers())
			p.sendMessage(msg);
		Bukkit.getConsoleSender().sendMessage(msg);
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent ev) {
		switch (ev.getEntityType()) {
		case PLAYER:
		case CHICKEN:
			break;
		default:
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent ev) {
		if (main.getCurrentMap().canPlayersMove())
			return;
		if (main.getGame().getGameState() == GameState.IN_GAME
				&& ev.getPlayer().getGameMode() == GameMode.ADVENTURE) {
			Location to = ev.getFrom();
			to.setY(ev.getTo().getY());
			to.setYaw(ev.getTo().getYaw());
			to.setPitch(ev.getTo().getPitch());
			ev.setTo(to);
		}
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent ev) {
		if (ev.toWeatherState())
			ev.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDrop(PlayerDropItemEvent ev) {
		ev.setCancelled(!ev.getPlayer().hasPermission("sg.btc.drop"));
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent ev) {
		ev.setCancelled(!ev.getPlayer().hasPermission("sg.btc.build"));
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent ev) {
		ev.setCancelled(!ev.getPlayer().hasPermission("sg.btc.build"));
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent ev) {
		ev.setCancelled(!ev.getWhoClicked().hasPermission("sg.btc.inv"));
	}

	@EventHandler
	public void onShoot(EntityShootBowEvent ev) {
		ev.getBow().setDurability((short) 0);
	}

	@EventHandler
	public void onShoot(ProjectileLaunchEvent ev) {
		if (!(ev.getEntity() instanceof Arrow)
				|| ev.getEntity().getShooter() == null
				|| !(ev.getEntity().getShooter() instanceof Player)
				|| MetadataUtils
				.getMetaData(main, ev.getEntity(), "btc-arrow2") != null)
			return;
		if (main.getGame().getGameState() != GameState.IN_GAME)
			ev.setCancelled(true);
		else if (main.hasPowerUp(
				((Player) ev.getEntity().getShooter()).getUniqueId(),
				SpecialChicken.DOUBLE_ARROW)) {
			double angle = Math.PI / 60;
			Arrow arrow1 = (Arrow) ev.getEntity();
			Vector vec = arrow1.getVelocity();
			Vector vec1 = new Vector(vec.getZ() * Math.sin(angle) + vec.getX()
					* Math.cos(angle), vec.getY(), vec.getZ() * Math.cos(angle)
					- vec.getX() * Math.sin(angle));
			angle *= -1;
			Vector vec2 = new Vector(vec.getZ() * Math.sin(angle) + vec.getX()
					* Math.cos(angle), vec.getY(), vec.getZ() * Math.cos(angle)
					- vec.getX() * Math.sin(angle));
			arrow1.setVelocity(vec1);
			Arrow arrow2 = arrow1.getWorld().spawnArrow(
					arrow1.getLocation().add(vec2.clone().multiply(0.3)), vec2,
					0.6F, 12F);
			arrow2.setFireTicks(arrow1.getFireTicks());
			arrow2.setVelocity(vec2);
			arrow2.setShooter(arrow1.getShooter());
			MetadataUtils.setMetaData(main, arrow2, "btc-arrow2", "");
		}
	}

	@EventHandler
	public void onHit(ProjectileHitEvent ev) {
		ev.getEntity().remove();
	}
}
