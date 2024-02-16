package dev.ninjune.beesmp.items.talisman;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class TalismanSpeed extends Talisman
{

    @Override
    public String getName()
    {
        return "§eTalisman of Speed";
    }

    @Override
    public String getID()
    {
        return "talisman_speed";
    }

    @Override
    public Material getMaterial()
    {
        return Material.SUGAR;
    }

    @Override
    public List<String> getTalismanLore() { return new ArrayList<>(){{add("§bGrants 20% more speed.");}}; }

    @Override
    public void runEverySecond()
    {
        HashSet<ImmutablePair<Player, ItemStack>> pairs = findItemsInInventories();

        for(Player player : Bukkit.getOnlinePlayers())
        {
            boolean found = false;
            for(ImmutablePair<Player, ItemStack> pair : pairs)
            {
                if(player.getName().equals(pair.left.getName()))
                {
                    found = true;
                    break;
                }
            }

            if(found)
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.12);
            else
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.1);
        }
    }
}
