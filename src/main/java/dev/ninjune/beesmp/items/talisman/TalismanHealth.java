package dev.ninjune.beesmp.items.talisman;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TalismanHealth extends Talisman
{
    @Override
    public String getName()
    {
        return "§eTalisman of Health";
    }

    @Override
    public String getID()
    {
        return "talisman_health";
    }

    @Override
    public Material getMaterial() { return Material.RED_DYE; }

    public List<String> getTalismanLore() { return new ArrayList<String>(){{add("§bGrants 2 hearts.");}}; }

    @Override
    public void runEverySecond()
    {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        for(Player player : players)
        {
            boolean found = false;
            for(ItemStack item : player.getInventory())
                if(isThis(item))
                    found = true;

            if(found)
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(24);
            else
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        }
    }
}
