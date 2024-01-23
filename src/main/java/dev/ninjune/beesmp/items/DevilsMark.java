package dev.ninjune.beesmp.items;

import dev.ninjune.beesmp.ItemManager;
import dev.ninjune.beesmp.items.BeeSMPItem;
import net.minecraft.nbt.StringTag;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DevilsMark extends BeeSMPItem
{
    @Override
    public String getName()
    {
        return "§eDevil's Mark";
    }

    @Override
    public String getID()
    {
        return "devil_mark";
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
    public void onInteract(PlayerInteractEvent e)
    {
        if(e.getItem() == null ||
                (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) ||
                !ItemManager.isCustomItem(e.getItem(), getID())
        )
            return;

        String boundPlayer = ItemManager.getNBT(e.getItem(), "bound");
        if(boundPlayer != null) // bound
        {
            boolean found = false;

            for(Player player : Bukkit.getOnlinePlayers())
            {
                if(player.getName() != boundPlayer)
                    continue;
                found = true;
                player.teleport(e.getPlayer());
                e.getPlayer().addPotionEffects(new PotionEffect(PotionEffectType.WEAKNESS, 2400, 2));
                break;
            }

            if(found)
            {
                e.getItem().setType(Material.BOOK);
                boundPlayer = "Blank";
            }
        }
        else
        {
            RayTraceResult result = e.getPlayer().rayTraceBlocks(4, FluidCollisionMode.NEVER);

            if(result.getEntity() != null && result.getEntity() instanceof Player player)
            {
                ItemManager.setNBT(e.getItem(), "bound", player.getName());
                e.getItem().setType(Material.WRITTEN_BOOK);
            }
        }

        ItemMeta meta = e.getItem().getItemMeta();
        List<String> lore = meta.getLore();
        assert lore != null;

        if (lore.size() >= 5)
        {
            lore.set(4, "§bBound to: " + boundPlayer);
            meta.setLore(lore);
            e.getItem().setItemMeta(meta);
        }
    }
}
