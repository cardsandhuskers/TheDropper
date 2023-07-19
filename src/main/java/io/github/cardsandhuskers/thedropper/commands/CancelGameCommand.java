package io.github.cardsandhuskers.thedropper.commands;

import io.github.cardsandhuskers.thedropper.TheDropper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CancelGameCommand implements CommandExecutor {
    TheDropper plugin;
    StartGameCommand startGameCommand;
    public CancelGameCommand(TheDropper plugin, StartGameCommand startGameCommand) {
        this.plugin = plugin;
        this.startGameCommand = startGameCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player p && !p.isOp()) {
            p.sendMessage(ChatColor.RED + "You don't have permissions");
        } else {
            cancelGame(commandSender);
        }
        return true;
    }

    public void cancelGame(CommandSender sender) {

        boolean success = startGameCommand.cancelTimers();
        if(success) {
            HandlerList.unregisterAll(plugin);
            Location lobby = plugin.getConfig().getLocation("lobby");
            for(Player p: Bukkit.getOnlinePlayers()) {
                p.teleport(lobby);
            }

            if(sender instanceof Player p) {
                p.sendMessage(ChatColor.GREEN + "Cancelled Dropper");
            } else {
                Bukkit.getLogger().info("Cancelled Dropper");
            }
        }
        else {
            if(sender instanceof Player p) {
                p.sendMessage(ChatColor.RED + "No Game Active");
            } else {
                Bukkit.getLogger().info("No Game Active");
            }
        }
    }
}

