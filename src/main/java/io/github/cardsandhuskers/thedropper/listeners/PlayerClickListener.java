package io.github.cardsandhuskers.thedropper.listeners;

import io.github.cardsandhuskers.thedropper.handlers.LevelSkipHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerClickListener implements Listener {

    LevelSkipHandler levelSkipHandler;
    public PlayerClickListener(LevelSkipHandler levelSkipHandler) {
        this.levelSkipHandler = levelSkipHandler;
    }


    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e) {
        if(e.getItem() != null && e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("level skip")) {
            levelSkipHandler.onSkip(e.getPlayer());
        }
    }
}
