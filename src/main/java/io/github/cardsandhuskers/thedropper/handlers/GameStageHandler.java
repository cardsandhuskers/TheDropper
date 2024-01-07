package io.github.cardsandhuskers.thedropper.handlers;

import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.thedropper.TheDropper;
import io.github.cardsandhuskers.thedropper.TheDropper.State;
import io.github.cardsandhuskers.thedropper.listeners.*;
import io.github.cardsandhuskers.thedropper.objects.Countdown;
import io.github.cardsandhuskers.thedropper.objects.GameMessages;
import io.github.cardsandhuskers.thedropper.objects.Stats;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
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
    private Countdown pregameTimer, gameTimer, gameEndTimer;
    private LevelSkipHandler levelSkipHandler;
    public static HashMap<Player, Integer> wins;
    public static int numLevels = 0;;
    private Stats stats;

    public GameStageHandler(TheDropper plugin, Stats stats) {
        wins = new HashMap<>();
        this.plugin = plugin;
        this.stats = stats;
    }


    /**
     * Starts the game: initializes maps/lists
     * teleports players to lobby, Initializes listeners, sets gamerules, and initializes lists
     *
     * Calls pregame countdown at the end
     */
    public void start() {
        currentLevel = new HashMap<>();
        playersCompleted = new HashMap<>();

        levels = new ArrayList<>();
        buttons = new ArrayList<>();

        //player prep
        for(Player p: Bukkit.getOnlinePlayers()) {
            p.teleport(plugin.getConfig().getLocation("spawn"));
            Inventory inv = p.getInventory();
            inv.clear();
            if(TeamHandler.getInstance().getPlayerTeam(p) != null) {
                p.setGameMode(GameMode.ADVENTURE);
            } else {
                p.setGameMode(GameMode.SPECTATOR);
            }
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(20);
        }

        prepLevelData();

        //Gamerules
        for(org.bukkit.scoreboard.Team t:Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        }
        World world = plugin.getConfig().getLocation("spawn").getWorld();
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);


        HashMap<Player, Integer> totalFails = new HashMap<>();
        HashMap<Player, Integer> levelFails = new HashMap<>();

        for(Team t: TeamHandler.getInstance().getTeams()) {
            for(Player p:t.getOnlinePlayers()) {
                totalFails.put(p, 0);
                levelFails.put(p, 0);
            }
        }

        levelSkipHandler = new LevelSkipHandler(levels, plugin, levelFails, stats);

        plugin.getServer().getPluginManager().registerEvents(new PlayerDamageListener(plugin, levels, levelFails, totalFails, levelSkipHandler), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ButtonPressListener(playersCompleted, buttons, levels, this, levelFails, stats), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin, levels, levelSkipHandler), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ItemClickListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerClickListener(levelSkipHandler), plugin);

        pregameCountdown();
    }

    /**
     * Handles logic for loading level data from configs and prepping the player level map
     */
    public void prepLevelData() {
        //initialize all players at level 1
        for(Team t:TeamHandler.getInstance().getTeams()) {
            t.resetTempPoints();
            for(OfflinePlayer p:t.getPlayers()) {
                currentLevel.put(p.getUniqueId(), 1);
            }
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
    }

    /**
     * Pregame countdown timer
     */
    public void pregameCountdown() {
        pregameTimer = new Countdown((JavaPlugin)plugin,
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
                        if(TeamHandler.getInstance().getPlayerTeam(p) != null) {
                            p.setGameMode(GameMode.ADVENTURE);
                        } else {
                            p.setGameMode(GameMode.SPECTATOR);
                        }
                        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 18000, 1));
                    }
                    TheDropper.timeVar = 0;

                    gameTimer();

                },

                //Each Second
                (t) -> {
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 2) Bukkit.broadcastMessage(GameMessages.getGameDescription(numLevels, plugin));
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 12) Bukkit.broadcastMessage(GameMessages.getPointsDescription(plugin));

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
     * teleports players, gives armor
     */
    public void gameTimer() {
        gameTimer = new Countdown((JavaPlugin)plugin,
                //should be 720
                plugin.getConfig().getInt("GameTime"),
                //Timer Start
                () -> {
                    for(UUID u:currentLevel.keySet()) {
                        Player p = Bukkit.getPlayer(u);
                        if (p != null) {
                            if (currentLevel.get(u) <= levels.size()) {
                                p.teleport(levels.get(currentLevel.get(u) - 1));
                            }
                        }
                    }
                    gameState = TheDropper.State.GAME_IN_PROGRESS;

                    TeamHandler handler = TeamHandler.getInstance();
                    for(Team t:handler.getTeams()) {
                        for(Player p:t.getOnlinePlayers()) {
                            Team team = handler.getPlayerTeam(p);
                            /*
                            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
                            LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
                            if(handler.getPlayerTeam(p) != null) {
                                bootsMeta.setColor(team.translateColor());
                            }
                            bootsMeta.setUnbreakable(true);
                            boots.setItemMeta(bootsMeta);
                            p.getEquipment().setBoots(boots);
                            */
                            for(Player target:Bukkit.getOnlinePlayers()) {
                                if(handler.getPlayerTeam(target) != handler.getPlayerTeam(p)) {
                                    p.hidePlayer(plugin, target);
                                }
                            }
                        }
                    }
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
     * Timer for after the game ends, will return to lobby at the end
     *
     * Calls statCalculation, resets players, and teleports players back
     */
    public void gameEndTimer() {
        for(Player p:Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SPECTATOR);
            p.teleport(levels.get(levels.size() - 1));
            for(Player target:Bukkit.getOnlinePlayers()) {
                p.showPlayer(plugin, target);
            }
        }
        HandlerList.unregisterAll(plugin);
        for(org.bukkit.scoreboard.Team t:Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        }

        gameEndTimer = new Countdown((JavaPlugin)plugin,
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

                    //writes new stat data
                    int eventNum;
                    try {eventNum = Bukkit.getPluginManager().getPlugin("LobbyPlugin").getConfig().getInt("eventNum");} catch (Exception e) {eventNum = 1;}
                    stats.writeToFile(plugin.getDataFolder().toPath().toString(), "dropperStats" + eventNum);

                },

                //Timer End
                () -> {
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

                    try {
                        plugin.statCalculator.calculateStats();
                    } catch (Exception e) {
                        StackTraceElement[] trace = e.getStackTrace();
                        String str = "";
                        for(StackTraceElement element:trace) str += element.toString() + "\n";
                        plugin.getLogger().severe("ERROR Calculating Stats!\n" + str);
                    }

                },

                //Each Second
                (t) -> {
                    TheDropper.timeVar = t.getSecondsLeft();

                    if(t.getSecondsLeft() == t.getTotalSeconds() - 1) GameMessages.announceTopPlayers();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 6) GameMessages.announceTeamPlayers();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 11) GameMessages.announceTeamLeaderboard();
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameEndTimer.scheduleTimer();


    }

    /**
     * Cancels all timers if they aren't null
     * Used for cancelling the game
     * @return cancellable - whether any timer was running (was a game in progress?)
     */
    public boolean cancelTimers() {
        boolean cancel = false;

        if(pregameTimer != null) {
            pregameTimer.cancelTimer();
            cancel = true;
        }
        if(gameTimer != null) {
            gameTimer.cancelTimer();
            cancel = true;
        }
        if(gameEndTimer != null) {
            gameEndTimer.cancelTimer();
            cancel = true;
        }
        return cancel;
    }

}
