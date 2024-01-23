package dev.ninjune.beesmp;

import dev.ninjune.beesmp.items.*;
import dev.ninjune.beesmp.items.talisman.Talisman;
import dev.ninjune.beesmp.items.DevilsMark;
import dev.ninjune.beesmp.items.talisman.TalismanHealth;
import dev.ninjune.beesmp.items.talisman.TalismanVeinmine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ItemManager implements Listener
{
    private static final HashSet<BeeSMPItem> customItems = new HashSet<>();

    static
    {
        customItems.add(new TalismanHealth());
        customItems.add(new TalismanVeinmine());
        customItems.add(new DevilsMark());

        customItems.add(new EnderPearlCrossbow());
        customItems.add(new PufferfishCannon());

        Bukkit.getScheduler().scheduleSyncRepeatingTask(BeeSMP.getPlugin(BeeSMP.class), () -> {
            for (BeeSMPItem customItem : customItems)
                if (customItem instanceof Talisman)
                    ((Talisman) customItem).runEverySecond();
        }, 200, 20);
    }

    public static HashSet<BeeSMPItem> getCustomItems()
    {
        return customItems;
    }

    public static BeeSMPItem findCustomItem(ItemStack item)
    {
        for(BeeSMPItem customItem : customItems)
        {
            String id = getNBT(item, "id");
            if(id == null)
                continue;
            if (Objects.equals(id, customItem.getID()))
                return customItem;
        }

        return null;
    }

    public static boolean isCustomItem(ItemStack item)
    {
        return findCustomItem(item) != null;
    }

    public static boolean isCustomItem(ItemStack item, String customItemID)
    {
        if(findCustomItem(item) == null)
            return false;
        return findCustomItem(item).getID().equalsIgnoreCase(customItemID);
    }

    public static void setNBT(ItemStack item, String id, String value)
    {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        CompoundTag itemCompound = (nmsItem.hasTag()) ? nmsItem.getTag() : new CompoundTag();
        assert itemCompound != null;
        itemCompound.putString(id, value);
        nmsItem.setTag(itemCompound);
        item.setItemMeta(CraftItemStack.getItemMeta(nmsItem));
    }

    public static String getNBT(ItemStack item, String id)
    {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        CompoundTag itemCompound = (nmsItem.hasTag()) ? nmsItem.getTag() : new CompoundTag();
        assert itemCompound != null;
        return itemCompound.get(id) == null ? null : itemCompound.getString(id);
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event)
    {
        List<ItemStack> matrix = Arrays.stream(event.getInventory().getMatrix()).toList();
        for(BeeSMPItem customItem : customItems)
        {
            for(ItemStack item : matrix)
            {
                net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
                CompoundTag itemCompound = (nmsItem.hasTag()) ? nmsItem.getTag() : new CompoundTag();
                assert itemCompound != null;
                if(itemCompound.get("id") == null || !Objects.equals(itemCompound.getString("id"), customItem.getID()))
                    continue;
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event)
    {
        if(isCustomItem(event.getItemInHand()))
            event.setCancelled(true);
    }
}
