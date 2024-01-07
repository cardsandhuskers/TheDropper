package io.github.cardsandhuskers.thedropper.listeners;

import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.thedropper.TheDropper;
import io.github.cardsandhuskers.thedropper.handlers.GameStageHandler;
import io.github.cardsandhuskers.thedropper.objects.Stats;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;

import static io.github.cardsandhuskers.thedropper.TheDropper.*;
import static io.github.cardsandhuskers.thedropper.handlers.GameStageHandler.currentLevel;

public class ButtonPressListener implements Listener {
    private HashMap<Integer, Integer> playersCompleted;
    private ArrayList<Location> levels;
    private ArrayList<Block> buttons;
    private GameStageHandler gameStageHandler;
    private HashMap<Player, Integer> levelFails;
    private TheDropper plugin = (TheDropper) Bukkit.getPluginManager().getPlugin("TheDropper");
    private Stats stats;

    public ButtonPressListener(HashMap<Integer, Integer> playersCompleted, ArrayList<Block> buttons, ArrayList<Location> levels, 
        GameStageHandler gameStageHandler, HashMap levelFails, Stats stats) {
        this.playersCompleted = playersCompleted;
        this.levels = levels;
        this.buttons = buttons;
        this.gameStageHandler = gameStageHandler;
        this.levelFails = levelFails;
        this.stats = stats;
    }

    @EventHandler
    public void onButtonPress(PlayerInteractEvent e) {
        //System.out.println(e.getClickedBlock());
        if(e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.STONE_BUTTON) {
            Material mat = e.getClickedBlock().getType();
            Location loc = e.getClickedBlock().getLocation();
            Player p = e.getPlayer();
            if(mat == Material.STONE_BUTTON && currentLevel.containsKey(p.getUniqueId())) {
                //System.out.println(buttons);
                int level = currentLevel.get(p.getUniqueId());
                try {
                    if (loc.equals(buttons.get(level - 1).getLocation())) {
                        currentLevel.put(p.getUniqueId(), currentLevel.get(p.getUniqueId()) + 1);

                        //level indexes from 1
                        if (level < levels.size() + 1) {
                            //System.out.println(level);
                            //System.out.println(levels.size());
                            //level is past level and levels indexes from 0, so no + or - necessary
                            p.teleport(levels.get(level));
                            givePoints(p);
                            //new level, reset level fails
                            if(levelFails.containsKey(p)) {
                                levelFails.put(p, 0);
                            }

                            Inventory inv = p.getInventory();
                            if(inv.contains(Material.GOLD_BLOCK)) inv.remove(Material.GOLD_BLOCK);

                            if (level >= levels.size() - 1) {
                                p.sendMessage(ChatColor.YELLOW + "You Completed all Levels!");
                                p.setGameMode(GameMode.SPECTATOR);
                                for(Player target: Bukkit.getOnlinePlayers()) {
                                    p.showPlayer(plugin, target);
                                }

                                if (playersCompleted.get(currentLevel.get(p.getUniqueId()) - 1) == currentLevel.keySet().size()) {
                                    gameStageHandler.endGame();
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    plugin.getLogger().severe("Error with button! " + e.getPlayer());
                    ex.printStackTrace();
                }
            }
            e.setCancelled(true);
        }
    }

    /**
     * Handles giving points when a player completes a level
     * @param p - player to give points to
     */
    public void givePoints(Player p) {
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(20);
        //for(PotionEffect e:p.getActivePotionEffects()) {
        //    p.removePotionEffect(e.getType());
        //}
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
        int level = currentLevel.get(p.getUniqueId()) - 1;
        //System.out.println(playersCompleted.keySet());
        int numCompleted = playersCompleted.get(level);
        if(handler.getPlayerTeam(p) != null) {
            Team t = handler.getPlayerTeam(p);
            double maxPoints = plugin.getConfig().getDouble("maxPoints") * multiplier;
            double dropOff = plugin.getConfig().getDouble("dropOff") * multiplier;

            double points = maxPoints - (dropOff * numCompleted);

            if(numCompleted == 0) {
                if(gameStageHandler.wins.containsKey(p)) gameStageHandler.wins.put(p, gameStageHandler.wins.get(p) + 1);
                else gameStageHandler.wins.put(p, 1);
            }


            for (Player player : Bukkit.getOnlinePlayers()) {
                String message;
                if(player.equals(p)) {
                    message = "You";
                    message += ChatColor.GREEN + " finished level " + ChatColor.AQUA + level + ChatColor.GREEN + " in " + ChatColor.AQUA + ChatColor.BOLD + (numCompleted+1);
                } else {
                    message = handler.getPlayerTeam(p).color + p.getName();
                    message += ChatColor.GRAY + " finished level " + level + " in " + ChatColor.BOLD + (numCompleted+1);
                }

                if(numCompleted == 10 || numCompleted == 11 || numCompleted == 12) {
                    message += "th";
                } else if(numCompleted % 10 == 0) {
                    message += "st";
                } else if(numCompleted % 10 == 1) {
                    message += "nd";
                } else if(numCompleted % 10 == 2) {
                    message += "rd";
                } else {
                    message += "th";
                }

                if(player.equals(p)) {
                    message += ChatColor.RESET + "" + ChatColor.GREEN + " place";
                    message += " [" + ChatColor.YELLOW + "" + ChatColor.BOLD + "+" + points + ChatColor.RESET + ChatColor.GREEN + "] points";
                } else {
                    message += ChatColor.RESET + "" + ChatColor.GRAY + " place";
                }

                player.sendMessage(message);
            }

            //Name, Team, Level, Place, Points
            String csvLine = p.getName() + "," + handler.getPlayerTeam(p).getTeamName() + "," + level + "," + (numCompleted+1) + "," + levelFails.getOrDefault(p, 0) + "," + "0";
            stats.addEntry(csvLine);

            t.addTempPoints(p, points);

            playersCompleted.put(level, playersCompleted.get(level) + 1);
        }
    }

    private boolean isButton(Material mat) {
        switch(mat) {
            case STONE_BUTTON:
            case ACACIA_BUTTON:
            case BAMBOO_BUTTON:
            case BIRCH_BUTTON:
            case CRIMSON_BUTTON:
            case DARK_OAK_BUTTON:
            case JUNGLE_BUTTON:
            case MANGROVE_BUTTON:
            case OAK_BUTTON:
            case SPRUCE_BUTTON:
            case WARPED_BUTTON:
            case POLISHED_BLACKSTONE_BUTTON:
            return true;
            default: return false;
        }
    }
}
