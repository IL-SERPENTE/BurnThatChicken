package com.samagames.burnthatchicken.metadata;

import net.minecraft.server.v1_8_R3.EnumParticle;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.samagames.burnthatchicken.BTCPlayer;
import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.task.BTCBackgroundTask;

public enum SpecialChicken {
	DOUBLE_ARROW(ChatColor.GREEN, EnumParticle.CRIT, 120, "Double flèches"), BLINDNESS(
			ChatColor.BLACK, EnumParticle.SMOKE_NORMAL, 20, "Aveugle"), MORE_CHICKENS(
			ChatColor.RED, EnumParticle.FLAME, -1, "Poulets x3");

	private String name;
	private ChatColor color;
	private EnumParticle particle;
	private int duration;

	SpecialChicken(ChatColor c, EnumParticle p, int d, String n) {
		color = c;
		particle = p;
		duration = d;
		name = n;
	}

	public ChatColor getColor() {
		return color;
	}

	public EnumParticle getParticle() {
		return particle;
	}

	public int getDuration() {
		return duration;
	}

	public String getName() {
		return name;
	}

	public void run(BTCPlugin main, Player p) {
		BTCPlayer player = main.getGame().getPlayer(p.getUniqueId());
		if (player == null)
			return;
		switch (this) {
		case BLINDNESS:
			p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
					duration + 20, 1));
			break;
		case MORE_CHICKENS:
			BTCBackgroundTask task = BTCBackgroundTask.getInstance();
			if (player.getZone() != null)
				for (int i = 0; i < 3; i++)
					task.spawnChicken(player.getZone(), false);
			break ;
		default:
			break;
		}
	}
}
