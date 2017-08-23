package com.samagames.burnthatchicken.task;

import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.util.GameState;

/*
 * This file is part of BurnThatChicken.
 *
 * BurnThatChicken is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BurnThatChicken is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BurnThatChicken.  If not, see <http://www.gnu.org/licenses/>.
 */
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
