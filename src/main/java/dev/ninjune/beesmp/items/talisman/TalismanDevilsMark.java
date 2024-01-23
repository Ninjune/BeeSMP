package dev.ninjune.beesmp.items.talisman;

import dev.ninjune.beesmp.ItemManager;
import net.minecraft.nbt.StringTag;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/*
* tps bound player to user & applies weakness to user
* book til bind player > written book
*/
public class TalismanDevilsMark extends Talisman
{
    @Override
    public String getName()
    {
        return "§eDevil's Mark";
    }

    @Override
    public String getID()
    {
        return "talisman_devil_mark";
    }

    @Override
    public Material getMaterial()
    {
        return Material.BOOK;
    }

    @Override
    public List<String> getLore()
    {
        List<String> lore = new ArrayList<>();
        lore.add("§bUse this on a player to bind that player.");
        lore.add("§bUse again to tp the bound player to yourself.");
        lore.add("§bNote that using this will debuff you for 1 minute with weakness.");
        lore.add("");
        lore.add("§bBound to: Blank");
        return lore;
    }

    @Override
    public void runEverySecond()
    {
        HashSet<ImmutablePair<Player, ItemStack>> pairs = findItemsInInventories();
        pairs.forEach(pair -> {
            ItemStack item = pair.right;
            Player player = pair.left;
            String boundPlayer = ItemManager.getNBT(item, "bound");
            if(boundPlayer != null)
            {
                item.setType(Material.WRITTEN_BOOK);
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                assert lore != null;
                if(lore.size() >= 5)
                {
                    lore.set(4, "§bBound to: " + boundPlayer);
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }

            }
            else
            {
                item.setType(Material.BOOK);
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                assert lore != null;
                if(lore.size() >= 5)
                {
                    lore.set(4, "§bBound to: Blank");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
            }
        });
    }
}
