package dev.ninjune.beesmp.items;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class BeeSMPItem extends ItemStack implements Listener
{
    public BeeSMPItem()
    {
        setType(getMaterial());
        setAmount(1);

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(this);
        CompoundTag itemCompound = (nmsItem.hasTag()) ? nmsItem.getTag() : new CompoundTag();
        assert itemCompound != null;
        itemCompound.putString("id", this.getID());
        nmsItem.setTag(itemCompound);
        this.setItemMeta(CraftItemStack.getItemMeta(nmsItem));

        ItemMeta meta = this.getItemMeta();
        assert meta != null;

        meta.setDisplayName(getName());
        meta.setLore(getLore());
        this.setItemMeta(meta);
    }

    public abstract String getName();
    public abstract String getID();
    public abstract Material getMaterial();
    public List<String> getLore() { return new ArrayList<String>(); }
}
