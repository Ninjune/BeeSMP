package dev.ninjune.beesmp.commands;

import dev.ninjune.beesmp.managers.ItemManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// on command: present available items and their corrosponding name & lore
public class CommandInfo extends BeeSMPCommand
{
    @Override
    public boolean execute(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if(!(commandSender instanceof Player) || strings.length < 2)
            return false;
        ItemManager.getCustomItems().forEach(beeSMPItem -> {
            if(Objects.equals(beeSMPItem.getID(), strings[1]))
            {
                commandSender.sendMessage(beeSMPItem.getName() + ": \n");
                beeSMPItem.getLore().forEach(commandSender::sendMessage);
            }
        });
        return true;
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"info"};
    }

    @Override
    public List<String> tabComplete(String[] strings)
    {
        ArrayList<String> values = new ArrayList<>();
        if(strings.length == 2)
            ItemManager.getCustomItems().forEach(value -> values.add(value.getID()));


        return values;
    }

    @Override
    public String getPermission()
    {
        return null;
    }
}
