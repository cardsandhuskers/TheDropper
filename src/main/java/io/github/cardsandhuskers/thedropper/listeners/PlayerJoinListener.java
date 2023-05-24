package io.github.cardsandhuskers.thedropper.listeners;

import io.github.cardsandhuskers.thedropper.TheDropper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;

import static io.github.cardsandhuskers.thedropper.TheDropper.handler;
import static io.github.cardsandhuskers.thedropper.handlers.GameStageHandler.currentLevel;

public class PlayerJoinListener implements Listener {
    private TheDropper plugin;
    private ArrayList<Location> levels;

    public PlayerJoinListener(TheDropper plugin, ArrayList<Location> levels) {
        this.plugin = plugin;
        this.levels = levels;

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.teleport(plugin.getConfig().getLocation("spawn"));
        if(handler.getPlayerTeam(p) != null) {
            p.setGameMode(GameMode.ADVENTURE);
            System.out.println(currentLevel.keySet());
            if(currentLevel.containsKey(p.getUniqueId())) {
                p.teleport(levels.get(currentLevel.get(p.getUniqueId()) - 1));
            } else {
                currentLevel.put(p.getUniqueId(), 1);
                p.teleport(levels.get(currentLevel.get(p.getUniqueId()) - 1));
            }
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->p.setGameMode(GameMode.SPECTATOR), 10L);
        }
    }
}
