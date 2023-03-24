package io.github.cardsandhuskers.thedropper.listeners;

import io.github.cardsandhuskers.thedropper.TheDropper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static io.github.cardsandhuskers.thedropper.TheDropper.handler;
import static io.github.cardsandhuskers.thedropper.TheDropper.multiplier;

public class ItemClickListener implements Listener {
    private TheDropper plugin = (TheDropper) Bukkit.getPluginManager().getPlugin("TheDropper");
    public ItemClickListener() {
    }

    @EventHandler
    public void ItemClickListener(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if(e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.DIAMOND) {
            e.setCancelled(true);
            e.getClickedInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
            int diamondPoints = plugin.getConfig().getInt("diamondPoints");

            handler.getPlayerTeam(p).addTempPoints(p, diamondPoints * multiplier);
            p.sendMessage(ChatColor.AQUA + "Found a Diamond! " + ChatColor.YELLOW + "+" + (diamondPoints * multiplier) + ChatColor.AQUA + " points!");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
        }
    }
}
