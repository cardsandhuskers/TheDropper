package io.github.cardsandhuskers.thedropper.handlers;

import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.teams.objects.TempPointsHolder;
import io.github.cardsandhuskers.thedropper.TheDropper;
import io.github.cardsandhuskers.thedropper.listeners.*;
import io.github.cardsandhuskers.thedropper.objects.Countdown;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.util.*;

import static io.github.cardsandhuskers.teams.Teams.handler;
import static io.github.cardsandhuskers.thedropper.TheDropper.*;

public class GameStageHandler {
    public static HashMap<UUID, Integer> currentLevel;
    private HashMap<Integer, Integer> playersCompleted;
    private ArrayList<Location> levels;
    private ArrayList<Block> buttons;
    private TheDropper plugin;
    private Countdown gameTimer;
    private LevelSkipHandler levelSkipHandler;
    public static HashMap<Player, Integer> wins;
    public static int numLevels = 0;
    private String GAME_DESCRIPTION, POINTS_DESCRIPTION;

    public GameStageHandler(TheDropper plugin) {
        wins = new HashMap<>();
        this.plugin = plugin;

                POINTS_DESCRIPTION = ChatColor.STRIKETHROUGH + "----------------------------------------" + ChatColor.RESET +
                        ChatColor.GOLD + "" + ChatColor.BOLD + "\nHow the game is Scored (for each level):" +
                        "\n1st Place: " + ChatColor.GOLD + (int)(plugin.getConfig().getInt("maxPoints") * multiplier) + ChatColor.RESET + " points" +
                        "\nWith: -" + ChatColor.GOLD + (int)(plugin.getConfig().getInt("dropOff") * multiplier) + ChatColor.RESET + " point for each player ahead" +
                        //"\nFor finding a " + ChatColor.AQUA + "" + ChatColor.BOLD + "Diamond" + ChatColor.RESET + ": " +
                        //ChatColor.GOLD + (int)(plugin.getConfig().getInt("diamondPoints") * multiplier) + ChatColor.RESET + " points" +
                        ChatColor.STRIKETHROUGH + "\n----------------------------------------";

    }


    /**
     * Starts the game: initializes maps/lists
     * teleports players
     * Initializes listeners
     */
    public void start() {
        currentLevel = new HashMap<>();
        playersCompleted = new HashMap<>();

        levels = new ArrayList<>();
        buttons = new ArrayList<>();

        for(Player p: Bukkit.getOnlinePlayers()) {
            p.teleport(plugin.getConfig().getLocation("spawn"));
            Inventory inv = p.getInventory();
            inv.clear();
            if(handler.getPlayerTeam(p) != null) {
                p.setGameMode(GameMode.ADVENTURE);
            } else {
                p.setGameMode(GameMode.SPECTATOR);
            }
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(20);
        }
        //initialize all players at level 1
        for(Team t:handler.getTeams()) {
            t.resetTempPoints();
            for(OfflinePlayer p:t.getPlayers()) {
                currentLevel.put(p.getUniqueId(), 1);
            }
        }


        World world = plugin.getConfig().getLocation("spawn").getWorld();
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        for(org.bukkit.scoreboard.Team t:Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        }


        //fill chests
        int counter = 1;
        while(plugin.getConfig().getLocation("chests." + counter) != null) {
            Location l = plugin.getConfig().getLocation("chests." + counter);
            //System.out.println(l.getBlock().getType());
            if(l.getBlock().getType() == Material.CHEST) {
                Chest chest = (Chest) l.getBlock().getState();
                Inventory chestInv = chest.getBlockInventory();
                chestInv.clear();
                chestInv.setItem(0, new ItemStack(Material.DIAMOND));
            }
            counter++;
        }


        //populate lists
        counter = 1;
        while(plugin.getConfig().getLocation("levels." + counter) != null) {
            levels.add(plugin.getConfig().getLocation("levels." + counter));
            counter++;
        }
        numLevels = counter;

        counter = 1;
        while(plugin.getConfig().getLocation("buttons." + counter) != null) {
            buttons.add(plugin.getConfig().getLocation("buttons." + counter).getBlock());
            counter++;
        }

        counter= 1;
        for(Location l:levels) {
            playersCompleted.put(counter, 0);
            counter++;
        }

        levelSkipHandler = new LevelSkipHandler(levels);

        plugin.getServer().getPluginManager().registerEvents(new PlayerDamageListener(levels), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ButtonPressListener(playersCompleted, buttons, levels, this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin, levels, levelSkipHandler), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ItemClickListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerClickListener(levelSkipHandler), plugin);


        GAME_DESCRIPTION =
                ChatColor.STRIKETHROUGH + "----------------------------------------\n" + ChatColor.RESET +
                        StringUtils.center(ChatColor.GOLD + "" + ChatColor.BOLD + "The Dropper", 30) +
                        ChatColor.BLUE + "" + ChatColor.BOLD + "\nHow To Play:" +
                        "\nWelcome to the dropper!" +
                        "\nThere are " + numLevels + " levels, you will have " + ChatColor.YELLOW + "" + ChatColor.BOLD + 12 + ChatColor.RESET + " minutes to complete as many levels as you can!" +
                        "\nEach level will have a hidden chest that contains 1 diamond! The first person to find this diamond gets bonus points!" +
                        "\nMake sure to turn your Render Distance up! At least 16 chunks is recommended if your computer can handle it." +
                        ChatColor.STRIKETHROUGH + "\n----------------------------------------";

        pregameCountdown();
    }

    /**
     * Pregame countdown timer
     */
    public void pregameCountdown() {
        Countdown pregameTimer = new Countdown((JavaPlugin)plugin,
                //should be 60
                plugin.getConfig().getInt("PregameTime"),
                //Timer Start
                () -> {
                    gameState = State.GAME_STARTING;
                },

                //Timer End
                () -> {
                    Bukkit.broadcastMessage(ChatColor.RED + "Start!");
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1F);
                        p.sendTitle(ChatColor.GREEN + "GO!", "", 5, 20, 5);
                        if(handler.getPlayerTeam(p) != null) {
                            p.setGameMode(GameMode.ADVENTURE);
                        } else {
                            p.setGameMode(GameMode.SPECTATOR);
                        }
                        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 18000, 1));
                    }
                    TheDropper.timeVar = 0;
                    levelSkipHandler.giveSkips();

                    gameTimer();

                },

                //Each Second
                (t) -> {
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 2) Bukkit.broadcastMessage(GAME_DESCRIPTION);
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 12) Bukkit.broadcastMessage(POINTS_DESCRIPTION);

                    TheDropper.timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() <= 4) {
                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2F);
                            p.sendTitle(ChatColor.GREEN + ">" + t.getSecondsLeft() + "<", "", 2, 16, 2);
                        }
                    }
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        pregameTimer.scheduleTimer();
    }

    /**
     * During game countdown timer
     */
    public void gameTimer() {
        gameTimer = new Countdown((JavaPlugin)plugin,
                //should be 720
                plugin.getConfig().getInt("GameTime"),
                //Timer Start
                () -> {
                    for(UUID u:currentLevel.keySet()) {
                        Player p = Bukkit.getPlayer(u);
                        if(p != null) {
                            if(currentLevel.get(u) <= levels.size()) {
                                p.teleport(levels.get(currentLevel.get(u) - 1));
                            }
                        }
                    }
                    gameState = TheDropper.State.GAME_IN_PROGRESS;
                },

                //Timer End
                () -> {
                    gameEndTimer();

                },

                //Each Second
                (t) -> {
                    TheDropper.timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() <= 4) {
                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2F);
                        }
                        Bukkit.broadcastMessage(ChatColor.GREEN + "Game ends in " + ChatColor.AQUA + t.getSecondsLeft() + ChatColor.GREEN + " Seconds!");
                    }
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameTimer.scheduleTimer();
    }

    /**
     * Resets the timers and calls the gameEndTimer
     */
    public void endGame() {
        if(gameTimer != null) {
            gameTimer.cancelTimer();
            gameEndTimer();
        }
    }

    /**
     * Timer for after the game ends, to return to lobby at the end
     */
    public void gameEndTimer() {
        for(Player p:Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SPECTATOR);
            p.teleport(levels.get(levels.size() - 1));
        }
        HandlerList.unregisterAll(plugin);
        for(org.bukkit.scoreboard.Team t:Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        }

        Countdown gameEndTimer = new Countdown((JavaPlugin)plugin,
                //should be 60
                plugin.getConfig().getInt("PostgameTime"),
                //Timer Start
                () -> {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "GAME OVER!");
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1F);
                        p.sendTitle(ChatColor.GREEN + "GAME OVER", "", 5, 20, 5);
                    }
                    TheDropper.timeVar = 0;

                    gameState = TheDropper.State.GAME_OVER;

                },

                //Timer End
                () -> {
                    try {
                        plugin.statCalculator.saveRecords();
                    } catch (IOException e) {
                        StackTraceElement[] trace = e.getStackTrace();
                        String str = "";
                        for(StackTraceElement element:trace) str += element.toString() + "\n";
                        plugin.getLogger().severe("ERROR Calculating Stats!\n" + str);
                    }

                    for(Player p:Bukkit.getOnlinePlayers()) {
                        if(plugin.getConfig().getLocation("lobby") != null) {
                            p.teleport(plugin.getConfig().getLocation("lobby"));
                        } else {
                            Bukkit.broadcastMessage(ChatColor.RED + "NO LOBBY LOCATION FOUND");
                        }
                    }
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        if(p.isOp()) {
                            p.performCommand("startRound");
                            break;
                        }
                    }

                },

                //Each Second
                (t) -> {
                    TheDropper.timeVar = t.getSecondsLeft();

                    if(t.getSecondsLeft() == t.getTotalSeconds() - 1) {
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

                    if(t.getSecondsLeft() == t.getTotalSeconds() - 6) {
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
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 11) {
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
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameEndTimer.scheduleTimer();


    }

}
