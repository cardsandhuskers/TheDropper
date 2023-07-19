package io.github.cardsandhuskers.thedropper.commands;

import io.github.cardsandhuskers.thedropper.TheDropper;
import io.github.cardsandhuskers.thedropper.handlers.GameStageHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StartGameCommand implements CommandExecutor {
    TheDropper plugin;
    private GameStageHandler gameStageHandler;
    public StartGameCommand(TheDropper plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p) {
            if (args.length > 0) {
                try {
                    TheDropper.multiplier = Double.parseDouble(args[0]);
                    startGame();
                } catch (Exception e) {
                    p.sendMessage(ChatColor.RED + "ERROR: argument must be a double");
                }
            } else {
                TheDropper.multiplier = 1;
                startGame();
            }
        } else {
            if (args.length > 0) {
                try {
                    TheDropper.multiplier = Double.parseDouble(args[0]);
                    startGame();
                } catch (Exception e) {
                    System.out.println(ChatColor.RED + "ERROR: argument must be a double");
                }
            } else {
                startGame();
            }
        }
        return true;
    }

    public void startGame() {
        gameStageHandler = new GameStageHandler(plugin);
        gameStageHandler.start();
    }

    public boolean cancelTimers() {
        if(gameStageHandler == null) return false;
        return gameStageHandler.cancelTimers();
    }
}
