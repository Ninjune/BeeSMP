package dev.ninjune.beesmp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.util.List;

public abstract class BeeSMPCommand implements Listener
{
    public abstract boolean execute(CommandSender commandSender, Command command, String s, String[] strings);
    public abstract String[] getAliases();
    public List<String> tabComplete(String[] strings) { return null; }
    public String getPermission() { return "beesmp.main"; }
}