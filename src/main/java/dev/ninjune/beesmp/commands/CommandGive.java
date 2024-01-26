package dev.ninjune.beesmp.commands;

import dev.ninjune.beesmp.items.BeeSMPItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.ninjune.beesmp.managers.ItemManager.getCustomItems;

public class CommandGive extends BeeSMPCommand
{
    @Override
    public boolean execute(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if(!(commandSender instanceof Player sender) || strings.length < 2)
            return false;
        Player player = sender;
        if(strings.length > 2)
            for(Player p : Bukkit.getOnlinePlayers())
                if(p.getName().equalsIgnoreCase(strings[2]))
                    player = p;

        for(BeeSMPItem item : getCustomItems())
        {
            if(strings[1].toLowerCase().equals(item.getID()))
            {
                if(strings.length > 3)
                    item.setAmount(Integer.parseInt(strings[3]));
                else
                    item.setAmount(1);

                if(strings.length > 4)
                    item.setWorldBound(Objects.equals(strings[4], "true"));

                player.getInventory().addItem(item.clone());
            }
        }
        return true;
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"give"};
    }

    @Override
    public List<String> tabComplete(String[] strings)
    {
        ArrayList<String> values = new ArrayList<>();

        if(strings.length == 2)
        {
            for(BeeSMPItem item : getCustomItems())
            {
                if (item.getID().startsWith(strings[1].toLowerCase()))
                    values.add(item.getID());
            }
        }
        else if(strings.length == 3)
        {
            for(Player player : Bukkit.getOnlinePlayers())
                if(player.getName().toLowerCase().startsWith(strings[2].toLowerCase()))
                    values.add(player.getName());
        }
        else if(strings.length == 4)
        {
            if(strings[3].isEmpty())
                values.add("<amount>");
        }
        else if(strings.length == 5)
        {
            if(strings[4].isEmpty())
                values.add("<worldBound = true>");
            else
            {
                values.add("true");
                values.add("false");
            }
        }


        return values;
    }
}
