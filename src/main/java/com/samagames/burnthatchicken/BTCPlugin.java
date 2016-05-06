package com.samagames.burnthatchicken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import net.samagames.api.SamaGamesAPI;

import net.samagames.tools.Titles;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.samagames.burnthatchicken.BTCMap.BTCGameZone;
import com.samagames.burnthatchicken.metadata.SpecialChicken;
import com.samagames.burnthatchicken.task.BTCBackgroundTask;
import com.samagames.burnthatchicken.task.BTCChickenChecker;
import com.samagames.burnthatchicken.task.BTCNoSprintTask;
import com.samagames.burnthatchicken.task.PowerUpTask;
import com.samagames.burnthatchicken.util.ChatUtils;
import com.samagames.burnthatchicken.util.GameState;

public class BTCPlugin extends JavaPlugin {
    private static BTCPlugin instance;

    private BTCGame game;

    private BTCMap map;
    private SamaGamesAPI api;
    private HashMap<Integer, BTCPlayer> ranking;
    private ArrayList<PowerUpTask> powerups;

    public BTCPlugin() {
        instance = this;
    }

    @Override
    public void onEnable() {
        game = new BTCGame(this);
        api = SamaGamesAPI.get();
        api.getGameManager().registerGame(game);
        map = BTCMap.loadMap(this);
        if (map == null) {
            getServer().shutdown();
            return;
        }
        powerups = new ArrayList<>();
        ranking = new HashMap<>();

        getServer().getScheduler().runTaskTimer(this, new BTCBackgroundTask(this), 20, 20);
        getServer().getScheduler().runTaskTimer(this, new BTCChickenChecker(this), 1, 1);
        getServer().getScheduler().runTaskTimer(this, new BTCNoSprintTask(this), 1, 1);
        getServer().getPluginManager().registerEvents(new BTCListener(this), this);

        for (World w : getServer().getWorlds())
            for (Entity e : w.getEntities())
                if (e.getType() != EntityType.PLAYER)
                    e.remove();

        game.setGameState(GameState.WAITING);
    }

    public BTCGame getGame() {
        return game;
    }

    public BTCMap getCurrentMap() {
        return map;
    }

    public void checkPlayers() {
        int n = 0;
        List<BTCPlayer> list = new ArrayList<>();
        for (BTCPlayer player : game.getInGamePlayers().values()) {
            n++;
            list.add(player);
        }
        if (n < 2) {
            for (BTCPlayer player : list) {
                BTCGameZone zone = player.getZone();
                zone.setEnded(true);
                BTCChickenChecker.getInstance().clearChicken(zone);
                player.setSpectator();
                addPlayerToRank(player);
            }
            for (Entry<Integer, BTCPlayer> entry : ranking.entrySet()) {
                BTCPlayer who = entry.getValue();
                Player player = who.getPlayerIfOnline();
                if (player != null) {
                    Titles.sendTitle(player, 0, 100, 0, ChatColor.GOLD + "Fin de la partie", ChatColor.AQUA
                            + "Tu es " + entry.getKey()
                            + (entry.getKey() == 1 ? "er" : "e"));
                }
                if (entry.getKey() == 1)
                    who.addStars(1, "Victoire");
            }
            ChatUtils.broadcastMessage(ChatUtils.getPluginPrefix()
                    + " Fin de la partie");
            game.setGameState(GameState.FINISHED);
            game.handleGameEnd();
            if (ranking.size() < 3) // Coeur dans le code OGM OGM OGM
                api.getGameManager().getCoherenceMachine().getTemplateManager()
                        .getPlayerWinTemplate()
                        .execute(ranking.get(1).getPlayer());
            else
                api.getGameManager()
                        .getCoherenceMachine()
                        .getTemplateManager()
                        .getPlayerLeaderboardWinTemplate()
                        .execute(ranking.get(1).getPlayer(),
                                ranking.get(2).getPlayer(),
                                ranking.get(3).getPlayer());
        }
    }

    public void addPlayerToRank(BTCPlayer player) {
        if (ranking.isEmpty())
            ranking.put(1, player);
        else {
            for (int i = ranking.size(); i >= 1; i--) {
                ranking.put(i + 1, ranking.get(i));
                ranking.remove(i);
            }
            ranking.put(1, player);
        }
    }

    public void addPowerUp(UUID player, SpecialChicken powerup, int duration) {
        if (duration != -1) {
            PowerUpTask task = new PowerUpTask(this, player, powerup);
            getServer().getScheduler().runTaskLater(this, task, duration);
            powerups.add(task);
        }
        Player p = getServer().getPlayer(player);
        if (p != null)
            Titles.sendTitle(p, 0, 40, 0, " ", ChatColor.AQUA
                    + powerup.getName()
                    + ChatColor.GOLD
                    + " activé"
                    + (duration == -1 ? "" : " pour " + (duration / 20)
                    + " seconde" + ((duration / 20) > 1 ? "s" : "") + " !"));
    }

    public boolean hasPowerUp(UUID player, SpecialChicken powerup) {
        for (PowerUpTask n : powerups) {
            if (n.getName().equals(player) && n.getSpecial() == powerup)
                return true;
        }
        return false;
    }

    public void removePowerUp(PowerUpTask task) {
        for (int i = 0; i < powerups.size(); i++)
            if (powerups.get(i).equals(task)) {
                powerups.remove(i);
                return;
            }
    }

    public BTCPlayer getPlayerByZone(int uniqueId) {
        List<BTCPlayer> list = new ArrayList<>();
        list.addAll(game.getInGamePlayers().values());
        for (BTCPlayer player : list) {
            if (player.isSpectator() || player.isModerator())
                continue;
            if (player.getZone() != null
                    && player.getZone().getUniqueId() == uniqueId)
                return player;
        }
        return null;
    }

    public static BTCPlugin getInstance() {
        return instance;
    }

    public SamaGamesAPI getApi() {
        return api;
    }
}
