package dev.ninjune.beesmp.managers;

import dev.ninjune.beesmp.BeeSMP;
import dev.ninjune.beesmp.items.*;
import dev.ninjune.beesmp.items.talisman.*;
import dev.ninjune.beesmp.items.DevilsMark;
import dev.ninjune.beesmp.util.InitShutdownListener;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ItemManager implements Listener, InitShutdownListener
{
    private static final HashSet<BeeSMPItem> customItems = new HashSet<>();
    private static final ItemManager instance = new ItemManager();

    ItemManager()
    {
        customItems.add(new TalismanHealth());
        customItems.add(new TalismanVeinmine());
        customItems.add(new TalismanSpeed());
        customItems.add(new TalismanDragonEgg());

        customItems.add(new DevilsMark());
        customItems.add(new EnderPearlCrossbow());
        customItems.add(new PufferfishCannon());
        customItems.add(new Multitool());
        customItems.add(new Blowgun());
        Bukkit.getOnlinePlayers().forEach(ItemManager::discoverAllCustomRecipes);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(BeeSMP.getPlugin(BeeSMP.class), () -> {
            for (BeeSMPItem customItem : customItems)
                if (customItem instanceof Talisman)
                    ((Talisman) customItem).runEverySecond();
        }, 0, 20);
    }

    public static ItemManager getInstance()
    {
        return instance;
    }

    public static HashSet<BeeSMPItem> getCustomItems()
    {
        return customItems;
    }

    public static BeeSMPItem findCustomItem(ItemStack item)
    {
        if(item == null)
            return null;
        for(BeeSMPItem customItem : customItems)
        {
            String id = getNBT(item, "id");
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
        return Objects.requireNonNull(findCustomItem(item)).getID().equalsIgnoreCase(customItemID);
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

    public static void damageWithUnbreaking(ItemStack item)
    {
        ItemMeta meta = item.getItemMeta();
        Integer unbreakingLevel = item.getEnchantments().get(Enchantment.DURABILITY);
        if(unbreakingLevel == null)
            unbreakingLevel = 0;
        if(meta instanceof Damageable dmgable &&
                ThreadLocalRandom.current().nextInt() % (1+unbreakingLevel) == 0
        )
        {
            if(dmgable.getDamage() > item.getType().getMaxDurability())
                item.setAmount(0);
            dmgable.setDamage(dmgable.getDamage()+1);
            item.setItemMeta(dmgable);
        }

    }

    @NotNull
    public static String getNBT(ItemStack item, String id)
    {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        CompoundTag itemCompound = (nmsItem.hasTag()) ? nmsItem.getTag() : new CompoundTag();
        assert itemCompound != null;
        return itemCompound.get(id) == null ? "" : itemCompound.getString(id);
    }

    public static UUID getUUID(ItemStack item)
    {
        return UUID.fromString(ItemManager.getNBT(item, "uuid"));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        // no invicibility for u bitch
        ((CraftPlayer) e.getPlayer()).getHandle().spawnInvulnerableTime = 0;
        discoverAllCustomRecipes(e.getPlayer());
    }

    public static void discoverAllCustomRecipes(Player player)
    {
        customItems.forEach(customItem -> {
            HashSet<NamespacedKey> discoverables = new HashSet<>();

            customItem.getRecipes().forEach(recipe -> {
                if(recipe instanceof CraftingRecipe craftingRecipe)
                    discoverables.add(craftingRecipe.getKey());
            });

            player.discoverRecipes(discoverables);
        });
    }

    @Override
    public void onEnable()
    {

    }

    @Override
    public void onDisable()
    {
        customItems.forEach(BeeSMPItem::onDisable);
    }
}
