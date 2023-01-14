package io.github.cardsandhuskers.thedropper.commands;

import io.github.cardsandhuskers.thedropper.TheDropper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        //FileConfiguration file = YamlConfiguration.loadConfiguration("config.yml");


        return true;
    }
}
