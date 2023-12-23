package io.github.cardsandhuskers.thedropper;

import io.github.cardsandhuskers.teams.Teams;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.thedropper.commands.*;
import io.github.cardsandhuskers.thedropper.objects.Placeholder;
import io.github.cardsandhuskers.thedropper.objects.StatCalculator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TheDropper extends JavaPlugin {
    public static TeamHandler handler;
    public static int timeVar = 0, numRounds;
    public static double multiplier;
    public static State gameState = State.GAME_STARTING;
    public StatCalculator statCalculator;

    
    @Override
    public void onEnable() {
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
            System.out.println("Could not find PlaceholderAPI!");
            //Bukkit.getPluginManager().disablePlugin(this);
        }

        handler = Teams.handler;



        // Plugin startup logic
        getCommand("setDropperButton").setExecutor(new SaveButtonLocationCommand(this));
        getCommand("setDropperLevelSpawn").setExecutor(new SaveLevelSpawnPointCommand(this));

        StartGameCommand startGameCommand = new StartGameCommand(this);
        getCommand("startDropper").setExecutor(startGameCommand);
        getCommand("setDropperSpawn").setExecutor(new SetWorldSpawnCommand(this));
        getCommand("setDropperLobby").setExecutor(new SetLobbyCommand(this));
        getCommand("setDropperDiamondChest").setExecutor(new SaveChestLocationCommand(this));

        getCommand("cancelDropper").setExecutor(new CancelGameCommand(this, startGameCommand));
        getCommand("reloadDropper").setExecutor(new ReloadConfigCommand(this));


        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        statCalculator = new StatCalculator(this);
        try {
            statCalculator.calculateStats();
        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            this.getLogger().severe("ERROR Calculating Stats!\n" + str);
        }

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
