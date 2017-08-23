package com.samagames.burnthatchicken.task;

import java.util.UUID;

import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.metadata.SpecialChicken;

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
public class PowerUpTask implements Runnable {
    private BTCPlugin main;
    private UUID name;
    private SpecialChicken special;

    public PowerUpTask(BTCPlugin m, UUID n, SpecialChicken s) {
        main = m;
        name = n;
        special = s;
    }

    @Override
    public void run() {
        main.removePowerUp(this);
    }

    public UUID getName() {
        return name;
    }

    public SpecialChicken getSpecial() {
        return special;
    }
}
