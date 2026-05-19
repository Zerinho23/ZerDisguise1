package me.libraryaddict.disguise.commands.utils;

import me.libraryaddict.disguise.gui.DisguiseGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisguiseMenuCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("libsdisguises.menu")) {
            player.sendMessage("§cNo tienes permiso para abrir el menú de disfraces.");
            return true;
        }
        DisguiseGUI.openMainMenu(player);
        return true;
    }
}
