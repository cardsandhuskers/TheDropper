package io.github.cardsandhuskers.thedropper.listeners;

import io.github.cardsandhuskers.thedropper.TheDropper;
import io.github.cardsandhuskers.thedropper.handlers.LevelSkipHandler;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.HashMap;

import static io.github.cardsandhuskers.thedropper.handlers.GameStageHandler.currentLevel;

public class PlayerDamageListener implements Listener {
    private ArrayList<Location> levels;
    private HashMap<Player, Integer> levelFails, totalFails;
    private LevelSkipHandler levelSkipHandler;
    private TheDropper plugin;

    public PlayerDamageListener(TheDropper plugin, ArrayList<Location> levels, HashMap levelFails, HashMap totalFails, LevelSkipHandler levelSkipHandler) {
        this.levels = levels;
        this.levelFails = levelFails;
        this.totalFails = totalFails;
        this.levelSkipHandler = levelSkipHandler;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity().getType().equals(EntityType.PLAYER)) {
            Player p = (Player) e.getEntity();
            if(e.getDamage() >= p.getHealth()) {
                //dead
                e.setCancelled(true);
                p.teleport(levels.get(currentLevel.get(p.getUniqueId()) - 1));
                p.setHealth(20);
                p.setFoodLevel(20);
                p.setSaturation(20);
                p.setFireTicks(0);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);

                if(levelFails.containsKey(p)) {
                    levelFails.put(p, levelFails.get(p) + 1);
                    totalFails.put(p, totalFails.get(p) + 1);
                } else {
                    levelFails.put(p, 1);
                    totalFails.put(p, 1);
                }

                if(levelFails.get(p) >= plugin.getConfig().getInt("skipFails")) {
                    levelSkipHandler.giveSkip(p);
                }
            }
        }
    }
}
