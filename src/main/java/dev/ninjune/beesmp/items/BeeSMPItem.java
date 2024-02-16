package dev.ninjune.beesmp.items;

import dev.ninjune.beesmp.BeeSMP;
import dev.ninjune.beesmp.managers.ItemManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// TODO: listener should be removed from items
public abstract class BeeSMPItem extends ItemStack implements Cloneable, Listener
{
    private final ItemEvents events = new ItemEvents();

    public BeeSMPItem()
    {
        setType(getMaterial());
        setAmount(1);

        setNBT("id", this.getID());
        setNBT("uuid", UUID.randomUUID().toString());
        setNBT("worldBound", "true");

        ItemMeta meta = this.getItemMeta();
        assert meta != null;

        meta.setDisplayName(getName());
        meta.setLore(getLore());
        this.setItemMeta(meta);

        if(!getRecipes().isEmpty())
            getRecipes().forEach(Bukkit::addRecipe);
    }

    public abstract String getName();
    public abstract String getID();
    public abstract Material getMaterial();
    public List<String> getLore() { return new ArrayList<>(); }
    public List<Recipe> getRecipes() { return new ArrayList<>(); }
    public List<Enchantment> getApplicableEnchants() { return new ArrayList<>(); }
    /**
     * For children: gets the recipe namespace.
     * @return recipe namespace.
     */
    protected final NamespacedKey getRecipeNamespace() { return new NamespacedKey(BeeSMP.getPlugin(BeeSMP.class), getID()); }
    /**
     * Gets the (string) NBT of this/child item.
     * @param id id of the value you wish to retrieve.
     * @return NBT value.
     */
    public final String getNBT(String id)
    {
        return ItemManager.getNBT(this, id);
    }

    public final ItemEvents getEvents()
    {
        return events;
    }

    public boolean isPlacable() { return false; }
    /**
     *
     * @return Whether or not the item should be teleported and saved on death.
     */
    @NotNull
    public final Boolean isWorldBound()
    {
        return Objects.equals(getNBT("worldBound"), "true");
    }

    /**
     * Compares the IDs of an ItemStack and a custom item.
     * @param item The item to compare with
     * @return Whether or not the items are the same.
     */
    protected final Boolean isThis(ItemStack item)
    {
        return ItemManager.isCustomItem(item, getID());
    }

    public final void setWorldBound(boolean worldBound) { setNBT("worldBound", worldBound ? "true" : "false"); }
    /**
     * Sets a (string) NBT of this/child item.
     * @param id id of the value you wish to change
     * @param value String value
     */
    protected final void setNBT(String id, String value)
    {
        ItemManager.setNBT(this, id, value);
    }

    protected void useAnvil (PrepareAnvilEvent event) {}

    public void onDisable () {}

    /**
     * Cloning the item changes the UUID, but nothing else.
     * @return Returns the same item with a different UUID.
     */
    @NotNull
    @Override
    public BeeSMPItem clone()
    {
        setNBT("uuid", UUID.randomUUID().toString());
        return (BeeSMPItem) super.clone();
    }

    protected ShapedRecipe genRecipe()
    {
        return new ShapedRecipe(getRecipeNamespace(), this.clone());
    }

    public class ItemEvents implements Listener
    {
        private ItemEvents() {}

        @EventHandler
        public void onEnchantmentTable(EnchantItemEvent e)
        {
            if(!isThis(e.getItem()) || getApplicableEnchants().isEmpty())
                return;
            Map<Enchantment, Integer> map = e.getEnchantsToAdd();
            final Map<Enchantment, Integer> iterableMap = new HashMap<>(map);

            iterableMap.keySet().forEach(item -> {
                if(!getApplicableEnchants().contains(item))
                    map.remove(item);
            });
        }

        @EventHandler
        public void onAnvil (PrepareAnvilEvent e)
        {
            e.getInventory().forEach(anvilItem -> {
                if(isThis(anvilItem))
                    useAnvil(e);
            });
        }

        @EventHandler
        public void onPlace(BlockPlaceEvent event)
        {
            if(isThis(event.getItemInHand()) && !isPlacable())
                event.setCancelled(true);
        }

        @EventHandler
        public void onPrepareCraft(PrepareItemCraftEvent event)
        {
            List<ItemStack> matrix = Arrays.stream(event.getInventory().getMatrix()).toList();
            ItemStack result = event.getInventory().getResult();

            for(ItemStack item : matrix)
                if(isThis(item))
                    event.getInventory().setResult(new ItemStack(Material.AIR));

            // set unique item in result
            if(result != null && isThis(result))
                event.getInventory().setResult(Objects.requireNonNull(ItemManager.findCustomItem(result)).clone());
        }

        @EventHandler
        public void onDamageByBlock(EntityDamageByBlockEvent e)
        {
            handleItemDamage(e);
        }

        @EventHandler
        public void onCombust(EntityCombustByBlockEvent e)
        {
            handleItemDamage(e);
        }

        @EventHandler
        public void onItemDespawn(ItemDespawnEvent e)
        {
            handleItemDamage(e);
        }
    }

    //TODO: Handle void death.
    private void handleItemDamage(EntityEvent e)
    {
        if(!(e.getEntity() instanceof Item item) ||
                !isThis(item.getItemStack()) ||
                ItemManager.getNBT(item.getItemStack(), "worldBound").equals("false")
        )
            return;
        double x = randBound1k(), z = randBound1k();

        Bukkit.getWorlds().forEach(world -> {
            if(world.getEnvironment() == World.Environment.NORMAL)
            {
                item.setUnlimitedLifetime(true);
                item.teleport(new Location(world, x, world.getMaxHeight(), z));
                Bukkit.broadcastMessage("A " + Objects.requireNonNull(ItemManager.findCustomItem(item.getItemStack())).getName() +
                        "§f has been lost and has been teleported to "
                        + (int) x + " " + world.getMaxHeight() + " " + (int) z + "!");
                ((Cancellable) e).setCancelled(true);
            }
        });
    }

    private static double randBound1k()
    {
        return ThreadLocalRandom.current().nextDouble(-1000, 1000+1);
    }
}
