package com.samagames.burnthatchicken.task;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;

import com.samagames.burnthatchicken.BTCMap.BTCGameZone;
import com.samagames.burnthatchicken.BTCPlugin;
import com.samagames.burnthatchicken.metadata.ChickenMetadataValue;
import com.samagames.burnthatchicken.metadata.SpecialChicken;
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
public class BTCBackgroundTask implements Runnable {
    private int[] delays = new int[] { 0, 4, 4, 4, 3, 3, 3, 3, 3, 2, 2, 2 };
    private BTCPlugin main;
    private int delay;
    private Random random;
    private static BTCBackgroundTask instance;

    public BTCBackgroundTask(BTCPlugin main) {
        this.main = main;
        delay = -1;
        random = new Random();
        instance = this;
    }

    @Override
    public void run() {
        if (main.getGame().getGameState() != GameState.IN_GAME)
            return;

        int r = 0;
        for (int i = 0; i < delays.length; i++) {
            if (delay == r) {
                this.spawnChickens();
                break;
            }
            r += delays[i];
        }
        if (delay > r)
            this.spawnChickens();
        delay++;
    }

    private void spawnChickens() {
        List<BTCGameZone> zones = main.getCurrentMap().getGameZones();
        for (BTCGameZone zone : zones)
            spawnChicken(zone, true);
    }

    public void spawnChicken(BTCGameZone zone, boolean canSpecial) {
        if (zone.isEnded())
            return;
        Location spawn = this.randomLocationInZone(zone.getChickenSpawnZone()[0], zone.getChickenSpawnZone()[1]);
        Chicken e = (Chicken) spawn.getWorld().spawnEntity(spawn, EntityType.CHICKEN);

        if (canSpecial && random.nextInt(15) == 0) {
            Location spawn2 = this.randomLocationInZone(zone.getChickenSpawnZone()[0], zone.getChickenSpawnZone()[1]);
            Chicken e2 = (Chicken) spawn2.getWorld().spawnEntity(spawn2, EntityType.CHICKEN);
            SpecialChicken[] list = SpecialChicken.values();
            SpecialChicken sc = list[random.nextInt(list.length)];
            e2.setBaby();
            ChickenMetadataValue.setMetadataValueToChicken(main, e2, zone.getUniqueId(), sc);
            e2.setHealth(1);
        }

        e.setAdult();
        ChickenMetadataValue.setMetadataValueToChicken(main, e, zone.getUniqueId(), null);
        e.setHealth(1);
    }

    private Location randomLocationInZone(Location l1, Location l2) {
        double x = random.nextDouble() * Math.abs(l1.getX() - l2.getX()) + Math.min(l1.getX(), l2.getX());
        double y = random.nextDouble() * Math.abs(l1.getY() - l2.getY()) + Math.min(l1.getY(), l2.getY());
        double z = random.nextDouble() * Math.abs(l1.getZ() - l2.getZ()) + Math.min(l1.getZ(), l2.getZ());

        Location newLoc = new Location(l1.getWorld(), x, y, z);

        for (double xc = -0.3D; xc <= 0.3D; xc += 0.3D)
            for (double zc = -0.3D; zc <= 0.3D; zc += 0.3D)
                if (newLoc.getWorld().getBlockAt(newLoc.clone().add(xc, 0, zc)).getType() != Material.AIR)
                    return randomLocationInZone(l1, l2);
        return newLoc;
    }

    public static BTCBackgroundTask getInstance() {
        return instance;
    }
}
