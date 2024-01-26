package dev.ninjune.beesmp.commands;

import dev.ninjune.beesmp.managers.ObjectiveManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandPlaytime extends BeeSMPCommand
{

    @Override
    public boolean execute(CommandSender commandSender, Command command, String s, String[] strings)
    {
        int playtimeSeconds = ObjectiveManager.getObjectives().get("playtime")
                .getScore(commandSender.getName()).getScore(),
                hours = playtimeSeconds / 3600,
                minutes = (playtimeSeconds % 3600) / 60,
                seconds = playtimeSeconds % 60;

        commandSender.sendMessage("§aYou have " + hours + " hours of playtime!");
        return true;
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"playtime"};
    }

    @Override
    public String getPermission()
    {
        return null;
    }
}
