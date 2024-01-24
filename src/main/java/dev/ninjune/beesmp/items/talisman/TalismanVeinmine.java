package dev.ninjune.beesmp.items.talisman;

import dev.ninjune.beesmp.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TalismanVeinmine extends Talisman
{
    @Override
    public void runEverySecond()
    {
        Bukkit.getOnlinePlayers().forEach(player -> {
            AtomicBoolean found = new AtomicBoolean(false);
            Inventory inventory = player.getInventory();
            for(ItemStack item : inventory)
            {
                if(!ItemManager.isCustomItem(item, "talisman_veinmine"))
                    continue;
                if(!player.hasPermission("skript.veinmine"))
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set skript.veinmine true");
                found.set(true);
            }

            if(!found.get() && player.hasPermission("skript.veinmine"))
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set skript.veinmine false");
        });
    }

    @Override
    public String getName()
    {
        return "§eTalisman of Speedy Mining";
    }

    @Override
    public String getID()
    {
        return "talisman_veinmine";
    }

    @Override
    public Material getMaterial()
    {
        return Material.IRON_INGOT;
    }

    @Override
    public List<String> getTalismanLore()
    {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§bWill grant veinmining when you are shifting.");
        return lore;
    }
}
