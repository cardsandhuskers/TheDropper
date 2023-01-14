package io.github.cardsandhuskers.thedropper;

import io.github.cardsandhuskers.teams.Teams;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.thedropper.commands.*;
import io.github.cardsandhuskers.thedropper.objects.Placeholder;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TheDropper extends JavaPlugin {
    public static TeamHandler handler;
    public static int timeVar = 0;
    public static double multiplier;
    public static PlayerPointsAPI ppAPI;
    public static State gameState = State.GAME_STARTING;
    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            this.ppAPI = PlayerPoints.getInstance().getAPI();
        } else {
            System.out.println("Could not find PlayerPointsAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }


        //Placeholder API validation
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            new Placeholder(this).register();

        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            System.out.println("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        handler = Teams.handler;



        // Plugin startup logic
        getCommand("setDropperButton").setExecutor(new SaveButtonLocationCommand(this));
        getCommand("setDropperLevelSpawn").setExecutor(new SaveLevelSpawnPointCommand(this));
        getCommand("startDropper").setExecutor(new StartGameCommand(this));
        getCommand("setDropperSpawn").setExecutor(new SetWorldSpawnCommand(this));
        getCommand("setDropperLobby").setExecutor(new SetLobbyCommand(this));
        getCommand("setDropperDiamondChest").setExecutor(new SaveChestLocationCommand(this));


        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public enum State {
        GAME_STARTING,
        GAME_IN_PROGRESS,
        GAME_OVER
    }
}
