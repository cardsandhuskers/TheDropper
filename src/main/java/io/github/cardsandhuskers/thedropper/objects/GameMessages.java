package io.github.cardsandhuskers.thedropper.objects;

import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.teams.objects.TempPointsHolder;
import io.github.cardsandhuskers.thedropper.TheDropper;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static io.github.cardsandhuskers.teams.Teams.handler;
import static io.github.cardsandhuskers.thedropper.TheDropper.multiplier;

public class GameMessages {

    /**
     *
     * @param numLevels
     * @return String to announce for game rules
     */
    public static String getGameDescription(int numLevels, TheDropper plugin) {
        String GAME_DESCRIPTION =
                ChatColor.STRIKETHROUGH + "----------------------------------------\n" + ChatColor.RESET +
                        StringUtils.center(ChatColor.GOLD + "" + ChatColor.BOLD + "The Dropper", 30) +
                        ChatColor.BLUE + "" + ChatColor.BOLD + "\nHow To Play:" + ChatColor.RESET +
                        "\nWelcome to the dropper!" +
                        "\nThere are " + ChatColor.YELLOW + "" + ChatColor.BOLD + numLevels + ChatColor.RESET + " levels, " +
                        "you will have " + ChatColor.YELLOW + "" + ChatColor.BOLD + plugin.getConfig().getInt("GameTime")/60 + ChatColor.RESET + " minutes to complete as many levels as you can!" +
                        //"\nEach level will have a hidden chest that contains 1 diamond! The first person to find this diamond gets bonus points!" +
                        "\nMake sure to turn your Render Distance up! At least 16 chunks is recommended if your computer can handle it." +
                        "\nAfter " + plugin.getConfig().getInt("skipFails") + " fails on a level, you will receive a " + ChatColor.YELLOW + ChatColor.BOLD + "skip item" + ChatColor.RESET + " that you can use to skip a level that's giving you trouble! You won't receive any points for the skipped level though." +
                        ChatColor.STRIKETHROUGH + "\n----------------------------------------";
        return GAME_DESCRIPTION;
    }

    /**
     *
     * @param plugin
     * @return String to announce for points
     */
    public static String getPointsDescription(TheDropper plugin) {
        String POINTS_DESCRIPTION = ChatColor.STRIKETHROUGH + "----------------------------------------" + ChatColor.RESET +
                ChatColor.GOLD + "" + ChatColor.BOLD + "\nHow the game is Scored (for each level):" +
                "\n1st Place: " + ChatColor.GOLD + (int)(plugin.getConfig().getInt("maxPoints") * multiplier) + ChatColor.RESET + " points" +
                "\nWith: -" + ChatColor.GOLD + (int)(plugin.getConfig().getInt("dropOff") * multiplier) + ChatColor.RESET + " point for each player ahead" +
                //"\nFor finding a " + ChatColor.AQUA + "" + ChatColor.BOLD + "Diamond" + ChatColor.RESET + ": " +
                //ChatColor.GOLD + (int)(plugin.getConfig().getInt("diamondPoints") * multiplier) + ChatColor.RESET + " points" +
                ChatColor.STRIKETHROUGH + "\n----------------------------------------";
        return POINTS_DESCRIPTION;
    }


    /**
     * Announces the top 5 earning players in the game
     */
    public static void announceTopPlayers() {
        ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
        for(Team team: handler.getTeams()) {
            for(Player p:team.getOnlinePlayers()) {
                tempPointsList.add(team.getPlayerTempPoints(p));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }

        Collections.sort(tempPointsList, Comparator.comparing(TempPointsHolder::getPoints));
        Collections.reverse(tempPointsList);

        int max;
        if(tempPointsList.size() >= 5) {
            max = 4;
        } else {
            max = tempPointsList.size() - 1;
        }

        Bukkit.broadcastMessage("\n" + ChatColor.RED + "" + ChatColor.BOLD + "Top 5 Players:");
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "------------------------------");
        int number = 1;
        for(int i = 0; i <= max; i++) {
            TempPointsHolder h = tempPointsList.get(i);
            Bukkit.broadcastMessage(number + ". " + handler.getPlayerTeam(h.getPlayer()).color + h.getPlayer().getName() + ChatColor.RESET + "    Points: " +  h.getPoints());
            number++;
        }
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "------------------------------");
    }

    /**
     * Announces the leaderboard for players on your team based on points earned in the game
     */
    public static void announceTeamPlayers() {
        for (Team team : handler.getTeams()) {
            ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
            for (Player p : team.getOnlinePlayers()) {
                if (team.getPlayerTempPoints(p) != null) {
                    tempPointsList.add(team.getPlayerTempPoints(p));
                }
            }
            Collections.sort(tempPointsList, Comparator.comparing(TempPointsHolder::getPoints));
            Collections.reverse(tempPointsList);

            for (Player p : team.getOnlinePlayers()) {
                p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Your Team Standings:");
                p.sendMessage(ChatColor.DARK_BLUE + "------------------------------");
                int number = 1;
                for (TempPointsHolder h : tempPointsList) {
                    p.sendMessage(number + ". " + handler.getPlayerTeam(p).color + h.getPlayer().getName() + ChatColor.RESET + "    Points: " + h.getPoints());
                    number++;
                }
                p.sendMessage(ChatColor.DARK_BLUE + "------------------------------\n");
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }
    }

    /**
     * Announces the leaderboard of teams based on points earned in the game
     */
    public static void announceTeamLeaderboard() {
        ArrayList<Team> teamList = handler.getTeams();
        Collections.sort(teamList, Comparator.comparing(Team::getTempPoints));
        Collections.reverse(teamList);

        Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Team Leaderboard:");
        Bukkit.broadcastMessage(ChatColor.GREEN + "------------------------------");
        int counter = 1;
        for(Team team:teamList) {
            Bukkit.broadcastMessage(counter + ". " + team.color + ChatColor.BOLD +  team.getTeamName() + ChatColor.RESET + " Points: " + team.getTempPoints());
            counter++;
        }
        Bukkit.broadcastMessage(ChatColor.GREEN + "------------------------------");
        for(Player p: Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        }
    }

}
