package com.samagames.burnthatchicken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.IGameProperties;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.samagames.burnthatchicken.util.JsonUtils;

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
public class BTCMap {
    private ArrayList<BTCGameZone> gamezones;
    private Location lobby;
    private boolean move;
    private boolean jump;
    private int id;

    public BTCMap(Location lobby, boolean move, boolean jump) {
        this.lobby = lobby;
        this.move = move;
        this.jump = jump;
        gamezones = new ArrayList<>();
        id = 0;
    }

    public boolean canPlayersJump() {
        return jump;
    }

    public boolean canPlayersMove() {
        return move;
    }

    public Location getWaitingLobby() {
        return lobby;
    }

    public boolean isReady() {
        int max = SamaGamesAPI.get().getGameManager().getGameProperties()
                .getMaxSlots();
        int min = SamaGamesAPI.get().getGameManager().getGameProperties()
                .getMinSlots();
        return gamezones.size() >= max && max >= min && min > 0;
    }

    public void addGameZone(Location p, Location s1, Location s2, Location e1, Location e2) {
        gamezones.add(new BTCGameZone(id, p, s1, s2, e1, e2));
        Bukkit.getLogger().info("GameZone " + id + " added.");
        id++;
    }

    public List<BTCGameZone> getGameZones() {
        return gamezones;
    }

    public static BTCMap loadMap(BTCPlugin main) {
        IGameProperties properties = main.getApi().getGameManager().getGameProperties();
        Location lobby = JsonUtils.getLocation(properties.getConfig("lobby", null));
        boolean move = properties.getConfig("move", new JsonPrimitive(false)).getAsBoolean();
        boolean jump = properties.getConfig("jump", new JsonPrimitive(false)).getAsBoolean();
        BTCMap g = new BTCMap(lobby, move, jump);

        int j = 0;
        while (true) {
            JsonElement element = properties.getConfig("zone-" + j, null);
            if (element == null)
                break ;
            try {
                Location spawn = JsonUtils.getLocation(element.getAsJsonObject().get("spawn"));
                Location sz1 = JsonUtils.getLocation(element.getAsJsonObject().get("spawnzone1"));
                Location sz2 = JsonUtils.getLocation(element.getAsJsonObject().get("spawnzone2"));
                Location ez1 = JsonUtils.getLocation(element.getAsJsonObject().get("endzone1"));
                Location ez2 = JsonUtils.getLocation(element.getAsJsonObject().get("endzone2"));
                g.addGameZone(spawn, sz1, sz2, ez1, ez2);
            } catch (Exception e) {
                main.getLogger().log(Level.SEVERE, "Error loading zone " + j, e);
            }
            j++;
        }

        if (g.isReady())
            return g;
        else
            Bukkit.getLogger().severe(
                    "[BTC] Map incorrecte ( zones : " + g.getGameZones().size()
                            + ").");
        return null;
    }

    public static class BTCGameZone {
        private Location spawn;
        private Location[] spawnzone;
        private Location[] endzone;
        private int id;
        private boolean end;
        private BTCPlayer player;

        public BTCGameZone(int i, Location s, Location z1, Location z2, Location e1, Location e2) {
            id = i;
            spawn = s;
            spawnzone = new Location[2];
            spawnzone[0] = z1;
            spawnzone[1] = z2;
            endzone = new Location[2];
            endzone[0] = e1;
            endzone[1] = e2;
            end = true;
            player = null;
        }

        public Location getPlayerSpawn() {
            return spawn;
        }

        public Location[] getChickenSpawnZone() {
            return spawnzone;
        }

        public Location[] getChickenEndZone() {
            return endzone;
        }

        public boolean isInChickenEndZone(Location loc) {
            double[] dim = new double[2];

            dim[0] = endzone[0].getX();
            dim[1] = endzone[1].getX();
            Arrays.sort(dim);
            if (loc.getX() > dim[1] || loc.getX() < dim[0])
                return false;
            dim[0] = endzone[0].getZ();
            dim[1] = endzone[1].getZ();
            Arrays.sort(dim);
            if (loc.getZ() > dim[1] || loc.getZ() < dim[0])
                return false;

            dim[0] = endzone[0].getY();
            dim[1] = endzone[1].getY();
            Arrays.sort(dim);
            if (loc.getY() > dim[1] || loc.getY() < dim[0])
                return false;

            return true;
        }

        public void setEnded(boolean e) {
            end = e;
        }

        public boolean isEnded() {
            return end;
        }

        public int getUniqueId() {
            return id;
        }

        public void setPlayer(BTCPlayer player) {
            this.player = player;
        }

        public BTCPlayer getPlayer() {
            return player;
        }
    }
}
