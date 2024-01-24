package dev.ninjune.beesmp.items;

import dev.ninjune.beesmp.BeeSMP;
import dev.ninjune.beesmp.Data;
import dev.ninjune.beesmp.ItemManager;
import org.apache.commons.lang3.tuple.MutablePair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

import java.nio.file.Path;
import java.util.*;

public class Multitool extends BeeSMPItem
{
    private final static Path SAVE_PATH = Path.of(BeeSMP.DATA_FOLDER.toString() + "/multitool.data");
    private HashMap<UUID, Queue<ItemStack>> multitoolMap = new HashMap<>();

    public Multitool()
    {
        Data d = new Data(multitoolMap);
        d.loadData(SAVE_PATH);
        if(d.getData() != null)
            multitoolMap = (HashMap<UUID, Queue<ItemStack>>) d.getData();
    }

    @Override
    public void onDisable()
    {
        new Data(multitoolMap).saveData(SAVE_PATH);
    }

    @Override
    public String getName()
    {
        return "§eMultitool";
    }

    @Override
    public String getID()
    {
        return "multitool";
    }

    @Override
    public Material getMaterial()
    {
        return Material.STICK;
    }

    @Override
    public List<String> getLore()
    {
        List<String> lore = new ArrayList<>();
        lore.add("§bUse this in an anvil to combine with another tool.");
        lore.add("§bWhen breaking a block, the multitool will switch to the most effective tool.");
        lore.add("§bWhen put in the right side of an anvil, returns the items in order given.");
        lore.add("§cWarning: don't add enchants to this directly, they will get deleted.");
        return lore;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        if(e.getAction() != Action.LEFT_CLICK_BLOCK ||
                !ItemManager.isCustomItem(e.getItem(), getID()) ||
                e.getClickedBlock() == null
        )
            return;
        Queue<ItemStack> tools = multitoolMap.get(ItemManager.getUUID(e.getItem()));
        if(tools.isEmpty())
            return;
        ItemStack preferredTool = getPreferredTool(e.getClickedBlock(), tools);

        if(preferredTool == null)
            return;

        transform(Objects.requireNonNull(e.getItem()), preferredTool);
    }

    @Override
    public void useAnvil(PrepareAnvilEvent e)
    {
        AnvilInventory inventory = e.getInventory();

        if(ItemManager.isCustomItem(inventory.getItem(0), getID()))
        {
            // left item is multitool
            if(inventory.getItem(1) == null)
                return;
            if(!EnchantmentTarget.TOOL.includes(inventory.getItem(1)))
                return;
            UUID uuid = ItemManager.getUUID(inventory.getItem(0));
            Queue<ItemStack> queue = multitoolMap.get(uuid);

            if(queue == null)
                queue = new LinkedList<>();

            queue.offer(Objects.requireNonNull(inventory.getItem(1)).clone());
            Objects.requireNonNull(inventory.getItem(1)).setAmount(0);
            multitoolMap.put(uuid, queue);
        }
        else if (ItemManager.isCustomItem(inventory.getItem(1), getID()))
        {
            // right item is multitool
            if(inventory.getItem(0) != null)
                return;
            ItemStack multitool = Objects.requireNonNull(inventory.getItem(1));
            UUID uuid = ItemManager.getUUID(inventory.getItem(1));
            Queue<ItemStack> queue = multitoolMap.get(uuid);

            if(queue == null)
                queue = new LinkedList<>();

            if(!queue.isEmpty())
            {
                ItemStack polledItem = queue.poll();
                ItemStack nextItem = queue.peek();
                if(nextItem == null)
                    nextItem = new ItemStack(Material.STICK, 1);
                /*
                Damageable polledItemMeta = (Damageable) polledItem.getItemMeta();
                polledItemMeta.setDamage(((Damageable) multitool.getItemMeta()).getDamage());
                polledItem.setItemMeta(polledItemMeta);*/


                inventory.setItem(0, polledItem);
                transform(multitool, Objects.requireNonNull(nextItem));
                if(nextItem.getItemMeta() instanceof Damageable nextItemMeta)
                {
                    Damageable multitoolMeta = (Damageable) multitool.getItemMeta();
                    multitoolMeta.setDamage(nextItemMeta.getDamage());
                    multitool.setItemMeta(multitoolMeta);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event)
    {
        PlayerInventory inventory = event.getPlayer().getInventory();
        ItemStack multitool = inventory.getItem(inventory.getHeldItemSlot());
        if(!ItemManager.isCustomItem(multitool, getID()))
            return;
        ItemStack preferredTool = getPreferredTool(event.getBlock(), multitoolMap.get(ItemManager.getUUID(multitool)));
        Damageable mtMeta = (Damageable) multitool.getItemMeta();
        assert mtMeta != null;
        Damageable ptMeta = (Damageable) preferredTool.getItemMeta();
        assert ptMeta != null;

        ptMeta.setDamage(mtMeta.getDamage());
        preferredTool.setItemMeta(ptMeta);
    }

    private static Float getBreakingSpeed(Block block, ItemStack itemStack) {
        return itemStack == null || block == null ? null : CraftItemStack.asNMSCopy(itemStack).getDestroySpeed(((CraftBlockState) block.getState()).getHandle());
    }

    private static void transform(ItemStack multitool, ItemStack mutation)
    {
        assert multitool != null;
        multitool.setType(mutation.getType());
        multitool.getEnchantments().keySet().forEach(multitool::removeEnchantment);
        multitool.addEnchantments(mutation.getEnchantments());
        Damageable meta = (Damageable) multitool.getItemMeta();
        Damageable mutationMeta = (Damageable) mutation.getItemMeta();
        assert meta != null;
        assert mutationMeta != null;
        meta.setDisplayName("§eMultitool");

        mutation.setItemMeta(mutationMeta);
        multitool.setItemMeta(meta);
    }

    private static ItemStack getPreferredTool(Block block, Queue<ItemStack> tools)
    {
        MutablePair<Float, ItemStack> pair =
                new MutablePair<>(getBreakingSpeed(block, tools.peek()), tools.peek());

        for(ItemStack tool : tools)
        {
            float speed = getBreakingSpeed(block, tool);
            if(speed > pair.left)
            {
                pair.left = speed;
                pair.right = tool;
            }
        }

        return pair.right;
    }
}
