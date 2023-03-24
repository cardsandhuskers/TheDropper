package io.github.cardsandhuskers.thedropper.objects;

import io.github.cardsandhuskers.thedropper.TheDropper;
import io.github.cardsandhuskers.thedropper.handlers.GameStageHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static io.github.cardsandhuskers.thedropper.TheDropper.*;
import static io.github.cardsandhuskers.thedropper.handlers.GameStageHandler.numLevels;

public class Placeholder extends PlaceholderExpansion {
    private final TheDropper plugin;

    public Placeholder(TheDropper plugin) {
        this.plugin = plugin;
    }


    @Override
    public String getIdentifier() {
        return "TheDropper";
    }
    @Override
    public String getAuthor() {
        return "cardsandhuskers";
    }
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    @Override
    public boolean persist() {
        return true;
    }


    @Override
    public String onRequest(OfflinePlayer p, String s) {

        if(s.equalsIgnoreCase("timer")) {
            int time = timeVar;
            int mins = time / 60;
            String seconds = String.format("%02d", time - (mins * 60));
            return mins + ":" + seconds;
        }
        if(s.equalsIgnoreCase("timerstage")) {
            switch(gameState) {
                case GAME_STARTING:
                    return "Game Starts";
                case GAME_IN_PROGRESS:
                    return "Game Ends";
                case GAME_OVER:
                    return "Return To Lobby";
                default:
                    return "Game";
            }
        }
        if(s.equalsIgnoreCase("level")) {
            if(GameStageHandler.currentLevel != null && GameStageHandler.currentLevel.get(p.getUniqueId()) != null) {
                return GameStageHandler.currentLevel.get(p.getUniqueId()) - 1 + "/" + numLevels;
            } else {
                return 0 + "/" + 0;
            }

        }

        String[] values = s.split("_");
        try {
            if(values[0].equalsIgnoreCase("wins")) {
                ArrayList<StatCalculator.PlayerStatsHolder> statsHolders = plugin.statCalculator.getPlayerStatsHolders();
                int index = Integer.parseInt(values[1]);
                if(index > statsHolders.size()) return "";
                StatCalculator.PlayerStatsHolder holder = statsHolders.get(Integer.parseInt(values[1]) - 1);
                String color = "";
                if (handler.getPlayerTeam(Bukkit.getPlayer(holder.name)) != null)
                    color = handler.getPlayerTeam(Bukkit.getPlayer(holder.name)).color;
                return color + holder.name + ChatColor.RESET + ": " + holder.wins;
            }


        } catch (Exception e) {
            /*StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            plugin.getLogger().warning("Error with Placeholder!\n" + str);*/
        }


        return null;
    }
}
