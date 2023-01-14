package io.github.cardsandhuskers.thedropper.listeners;

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

    public PlayerDamageListener(ArrayList<Location> levels) {
        this.levels = levels;
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
            }
        }
    }
}
