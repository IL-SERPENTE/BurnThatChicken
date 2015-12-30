package com.samagames.burnthatchicken.task;

import com.samagames.burnthatchicken.BTCPlayer;
import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.util.GameState;

public class BTCNoSprintTask implements Runnable {
	private BTCPlugin main;

	public BTCNoSprintTask(BTCPlugin btcPlugin) {
		main = btcPlugin;
	}

	@Override
	public void run() {
		if (main.getGame().getGameState() == GameState.WAITING)
			return;
		for (BTCPlayer player : main.getGame().getInGamePlayers().values())
			if (player.isOnline())
				player.getPlayerIfOnline().setSprinting(false);
	}

}
