package dev.ninjune.beesmp.commands;

import dev.ninjune.beesmp.items.BeeSMPItem;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static dev.ninjune.beesmp.ItemManager.getCustomItems;

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
                    item.setAmount(Integer.valueOf(strings[3]));
                else
                    item.setAmount(1);

                player.getInventory().addItem(item);
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
        if(strings.length < 2)
            return values;

        for(BeeSMPItem item : getCustomItems())
        {
            if (item.getID().startsWith(strings[1].toLowerCase()))
                values.add(item.getID());
        }

        return values;
    }
}
