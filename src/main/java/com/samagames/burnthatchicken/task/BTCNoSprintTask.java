package com.samagames.burnthatchicken.task;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.util.GameState;

public class BTCNoSprintTask implements Runnable
{
	private BTCPlugin main;
	
	public BTCNoSprintTask(BTCPlugin btcPlugin)
	{
		main = btcPlugin;
	}

	@Override
	public void run()
	{
		if (main.isDebug())
			return ;
		if (main.getGameState() == GameState.WAITING)
			return ;
		Collection <? extends Player> players = Bukkit.getOnlinePlayers();
		for (Player p : players)
			if (p.getGameMode() == GameMode.ADVENTURE)
				p.setSprinting(false);
	}

}
