package dev.ninjune.beesmp.items.talisman;

import dev.ninjune.beesmp.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public List<String> getLore() { return new ArrayList<String>(){{add("§bGrants 2 hearts.");}}; }

    @Override
    public void runEverySecond()
    {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        AtomicBoolean found = new AtomicBoolean(false);

        for(Player player : players)
        {
            player.getInventory().forEach(item -> {
                if(ItemManager.isCustomItem(item, "talisman_health"))
                    found.set(true);
            });

            if(found.get())
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(24);
            else
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        }
    }
}
