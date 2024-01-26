package dev.ninjune.beesmp.items.talisman;

import dev.ninjune.beesmp.managers.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Objects;

public class TalismanDragonEgg extends Talisman
{
    @Override
    public String getName()
    {
        return "§dDragon Egg";
    }

    @Override
    public String getID()
    {
        return "talisman_dragon_egg";
    }

    @Override
    public Material getMaterial()
    {
        return Material.DRAGON_EGG;
    }

    @Override
    public java.util.List<String> getTalismanLore()
    {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§bThe dragon egg is now a talisman granting +2 attack damage to every weapon.");
        return lore;
    }

    @Override
    public boolean isPlacable()
    {
        return true;
    }

    @Override
    public void runEverySecond()
    {
        for(Player player : Bukkit.getOnlinePlayers())
        {
            PlayerInventory inventory = player.getInventory();

            if(!(inventory.contains(getMaterial(), 1)))
            {
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(1);
                continue;
            }
            inventory.all(Material.DRAGON_EGG).values().forEach(egg -> {
                ItemMeta meta = egg.getItemMeta();
                assert meta != null;
                meta.setDisplayName(getName());
                meta.setLore(getLore());
                egg.setItemMeta(meta);
                ItemManager.setNBT(egg, "id", getID());
            });
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(3);
        }
    }
}
