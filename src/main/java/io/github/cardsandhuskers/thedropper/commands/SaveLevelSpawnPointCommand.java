package io.github.cardsandhuskers.thedropper.commands;

import io.github.cardsandhuskers.thedropper.TheDropper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SaveLevelSpawnPointCommand implements CommandExecutor {

    private TheDropper plugin;

    public SaveLevelSpawnPointCommand(TheDropper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof  Player p && p.isOp()) {
            if(args.length > 0) {
                Location l = p.getLocation();
                int level;
                try {
                    level = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    p.sendMessage(ChatColor.RED + "ERROR: Argument must be an integer");
                    return false;
                }
                plugin.getConfig().set("levels." + level, l);
                plugin.saveConfig();
                p.sendMessage("Location set to " + l.toString());

            } else {
                p.sendMessage(ChatColor.RED + "ERROR: Must specify a Level number");
            }
        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "ERROR: You do not have sufficient permission to do this");
        } else {
            System.out.println(ChatColor.RED + "ERROR: Cannot run from console");
        }

        return true;
    }
}
