package com.samagames.burnthatchicken.task;

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
            return ;

        main.getGame().getInGamePlayers().values().forEach(btcPlayer -> btcPlayer.getPlayer().setSprinting(false));
    }
}
