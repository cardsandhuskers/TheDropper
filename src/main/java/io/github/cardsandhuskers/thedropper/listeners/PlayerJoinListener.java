package io.github.cardsandhuskers.thedropper.listeners;

import io.github.cardsandhuskers.thedropper.TheDropper;
import io.github.cardsandhuskers.thedropper.handlers.LevelSkipHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;

import static io.github.cardsandhuskers.thedropper.TheDropper.gameState;
import static io.github.cardsandhuskers.thedropper.TheDropper.handler;
import static io.github.cardsandhuskers.thedropper.handlers.GameStageHandler.currentLevel;

public class PlayerJoinListener implements Listener {
    private TheDropper plugin;
    private ArrayList<Location> levels;
    private LevelSkipHandler levelSkipHandler;

    public PlayerJoinListener(TheDropper plugin, ArrayList<Location> levels, LevelSkipHandler levelSkipHandler) {
        this.plugin = plugin;
        this.levels = levels;
        this.levelSkipHandler = levelSkipHandler;

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.teleport(plugin.getConfig().getLocation("spawn"));
        if(handler.getPlayerTeam(p) != null) {
            p.setGameMode(GameMode.ADVENTURE);
            //System.out.println(currentLevel.keySet());
            if(currentLevel.containsKey(p.getUniqueId())) {
                p.teleport(levels.get(currentLevel.get(p.getUniqueId()) - 1));
            } else {
                currentLevel.put(p.getUniqueId(), 1);
                p.teleport(levels.get(currentLevel.get(p.getUniqueId()) - 1));
            }

            if(gameState == TheDropper.State.GAME_IN_PROGRESS && handler.getPlayerTeam(p) != null) {
                for(Player target:Bukkit.getOnlinePlayers()) {
                    if(handler.getPlayerTeam(target) != handler.getPlayerTeam(p)) {
                        p.hidePlayer(plugin, target);
                        target.hidePlayer(plugin, p);
                    }
                }
            }

        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->p.setGameMode(GameMode.SPECTATOR), 2L);
        }
    }
}
