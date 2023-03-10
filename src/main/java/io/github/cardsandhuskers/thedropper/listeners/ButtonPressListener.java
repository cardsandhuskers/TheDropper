package io.github.cardsandhuskers.thedropper.listeners;

import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.thedropper.TheDropper;
import io.github.cardsandhuskers.thedropper.handlers.GameStageHandler;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
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
    private TheDropper plugin = (TheDropper) Bukkit.getPluginManager().getPlugin("TheDropper");
    public ButtonPressListener(HashMap<Integer, Integer> playersCompleted, ArrayList<Block> buttons, ArrayList<Location> levels, GameStageHandler gameStageHandler) {
        this.playersCompleted = playersCompleted;
        this.levels = levels;
        this.buttons = buttons;
        this.gameStageHandler = gameStageHandler;
    }

    @EventHandler
    public void onButtonPress(PlayerInteractEvent e) {
        //System.out.println(e.getClickedBlock());
        if(e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.STONE_BUTTON) {
            Material mat = e.getClickedBlock().getType();
            Location loc = e.getClickedBlock().getLocation();
            Player p = e.getPlayer();
            if(mat == Material.STONE_BUTTON && currentLevel.containsKey(p.getUniqueId()) == true) {
                //System.out.println(buttons);
                int level = currentLevel.get(p.getUniqueId());
                if(loc.equals(buttons.get(level - 1).getLocation())) {
                    currentLevel.put(p.getUniqueId(), currentLevel.get(p.getUniqueId()) + 1);

                    if(level < levels.size()) {
                        //level is past level and levels indexes from 0, so no + or - necessary
                        p.teleport(levels.get(level));
                        if (level < levels.size() - 1) {
                            givePoints(p);
                        } else {
                            givePoints(p);
                            p.sendMessage("YOU win");
                            p.setGameMode(GameMode.SPECTATOR);

                            if(playersCompleted.get(currentLevel.get(p.getUniqueId()) - 1) == currentLevel.keySet().size()) {
                                gameStageHandler.endGame();
                            }
                        }
                    }
                }
            }
            e.setCancelled(true);
        }
    }
    public void givePoints(Player p) {
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(20);
        for(PotionEffect e:p.getActivePotionEffects()) {
            p.removePotionEffect(e.getType());
        }
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
        int level = currentLevel.get(p.getUniqueId()) - 1;
        //System.out.println(playersCompleted.keySet());
        int numCompleted = playersCompleted.get(level);
        if(handler.getPlayerTeam(p) != null) {
            Team t = handler.getPlayerTeam(p);
            int maxPoints = plugin.getConfig().getInt("maxPoints");
            int dropOff = plugin.getConfig().getInt("dropOff");

            int points = (int)((maxPoints - (dropOff * numCompleted)) * multiplier);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.equals(p)) {
                    switch(numCompleted) {
                        case 0:
                            p.sendMessage(ChatColor.GREEN + "You finished level " + ChatColor.YELLOW + level + ChatColor.GREEN + " in " + ChatColor.YELLOW + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.GREEN + "st place [" + ChatColor.YELLOW + ChatColor.BOLD + "+" + points + ChatColor.RESET + ChatColor.GREEN + "] points");
                            break;
                        case 1:
                            p.sendMessage(ChatColor.GREEN + "You finished level " + ChatColor.YELLOW + level + ChatColor.GREEN + " in " + ChatColor.YELLOW + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GREEN + "nd place [" + ChatColor.YELLOW + ChatColor.BOLD + "+" + points + ChatColor.RESET + ChatColor.GREEN + "] points");
                            break;
                        case 2:
                            p.sendMessage(ChatColor.GREEN + "You finished level " + ChatColor.YELLOW + level + ChatColor.GREEN + " in " + ChatColor.YELLOW + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.GREEN + "rd place [" + ChatColor.YELLOW + ChatColor.BOLD + "+" + points + ChatColor.RESET + ChatColor.GREEN + "] points");
                            break;
                        default:
                            p.sendMessage(ChatColor.GREEN + "You finished level " + ChatColor.YELLOW + level + ChatColor.GREEN + " in " + ChatColor.YELLOW + ChatColor.BOLD + (numCompleted + 1) + ChatColor.RESET + ChatColor.GREEN + "th place [" + ChatColor.YELLOW + ChatColor.BOLD + "+" + points + ChatColor.RESET + ChatColor.GREEN + "] points");
                    }

                } else {
                    switch(numCompleted) {
                        case 0:
                            player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.GREEN + " finished level " + ChatColor.YELLOW + level + ChatColor.GREEN + " in " + ChatColor.YELLOW + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.GREEN + "st place");
                            break;
                        case 1:
                            player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.GREEN + " finished level " + ChatColor.YELLOW + level + ChatColor.GREEN + " in " + ChatColor.YELLOW + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GREEN + "nd place");
                            break;
                        case 2:
                            player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.GREEN + " finished level " + ChatColor.YELLOW + level + ChatColor.GREEN + " in " + ChatColor.YELLOW + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.GREEN + "rd place");
                            break;
                        default:
                            player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.GREEN + " finished level " + ChatColor.YELLOW + level + ChatColor.GREEN + " in " + ChatColor.YELLOW + ChatColor.BOLD + (numCompleted + 1) + ChatColor.RESET + ChatColor.GREEN + "th place");
                    }
                }
            }
            t.addTempPoints(p, points);
            ppAPI.give(p.getUniqueId(), points);

            playersCompleted.put(level, playersCompleted.get(level) + 1);
        }
    }
}
