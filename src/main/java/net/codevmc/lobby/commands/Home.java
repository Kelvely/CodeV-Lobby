package net.codevmc.lobby.commands;

import net.codevmc.lobby.Lobby;
import net.codevmc.lobby.hekate.ClusterManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Home implements CommandExecutor {

    private final Lobby lobby;

    public Home(Lobby lobby) {
        this.lobby = lobby;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0) {

        } else {

        }
        return true;
    }

}
