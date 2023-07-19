package io.github.cardsandhuskers.thedropper.handlers;

import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.thedropper.TheDropper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static io.github.cardsandhuskers.thedropper.TheDropper.handler;
import static io.github.cardsandhuskers.thedropper.handlers.GameStageHandler.currentLevel;

public class LevelSkipHandler {
    //stored as hasSkip (starts true)
    public HashMap<OfflinePlayer, Boolean> levelSkipMap;
    private ArrayList<Location> levels;
    private TheDropper plugin;

    public LevelSkipHandler(ArrayList<Location> levels,TheDropper plugin) {
        this.levels = levels;
        this.plugin = plugin;
        buildMap();
    }
    public void buildMap() {
        levelSkipMap = new HashMap<>();
        for(Team t:handler.getTeams()) {
            for(OfflinePlayer p:t.getPlayers()) {
                levelSkipMap.put(p, true);
            }
        }

    }

    public void giveSkips() {
        ItemStack skip = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta skipMeta = skip.getItemMeta();
        skipMeta.setDisplayName("Level Skip");
        skipMeta.setLore(Collections.singletonList("Use this to skip a dropper level. Can only be used once"));
        skipMeta.addEnchant(Enchantment.LURE, 1, true);
        skipMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        skip.setItemMeta(skipMeta);

        for(Team t:handler.getTeams()) {
            for(OfflinePlayer p:t.getPlayers()) {
                if((Player)p != null) ((Player) p).getInventory().setItem(4, skip);
            }
        }
    }

    public void giveSkip(Player p) {
        ItemStack skip = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta skipMeta = skip.getItemMeta();
        skipMeta.setDisplayName("Level Skip");
        skipMeta.setLore(Collections.singletonList("Use this to skip a dropper level. Can only be used once"));
        skipMeta.addEnchant(Enchantment.LURE, 1, true);
        skipMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        skip.setItemMeta(skipMeta);

        p.getInventory().setItem(4, skip);
    }

    public void onSkip(Player p) {
        if(currentLevel.containsKey(p.getUniqueId())) {
            if(levelSkipMap.containsKey(p)) {
                levelSkipMap.put(p, false);
                currentLevel.put(p.getUniqueId(), currentLevel.get(p.getUniqueId()) + 1);

                p.teleport(levels.get(currentLevel.get(p.getUniqueId()) - 1));

                Inventory inv = p.getInventory();
                inv.remove(Material.GOLD_BLOCK);

                p.sendMessage(ChatColor.YELLOW + "You skipped level " + (currentLevel.get(p.getUniqueId()) - 1));

                p.setInvulnerable(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->p.setInvulnerable(false), 2L);


            } else {
                levelSkipMap.put((OfflinePlayer) p, true);
                onSkip(p);
            }

        }
    }
}
