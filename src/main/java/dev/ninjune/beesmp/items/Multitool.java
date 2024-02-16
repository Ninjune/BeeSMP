package dev.ninjune.beesmp.items;

import dev.ninjune.beesmp.BeeSMP;
import dev.ninjune.beesmp.util.Data;
import dev.ninjune.beesmp.managers.ItemManager;
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
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

import java.nio.file.Path;
import java.util.*;

public class Multitool extends BeeSMPItem
{
    private final static Path SAVE_PATH = Path.of("multitool.data");
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
                !isThis(e.getItem()) ||
                e.getClickedBlock() == null
        )
            return;
        Queue<ItemStack> tools = multitoolMap.get(ItemManager.getUUID(e.getItem()));
        ItemStack preferredTool;
        if(tools.isEmpty())
            preferredTool = new ItemStack(Material.STICK, 1);
        else
            preferredTool = getPreferredTool(e.getClickedBlock(), tools);

        if(preferredTool == null)
            return;

        transform(Objects.requireNonNull(e.getItem()), preferredTool);
        if(preferredTool.getItemMeta() instanceof Damageable preferredToolMeta)
        {
            Damageable multitoolMeta = (Damageable) e.getItem().getItemMeta();
            assert multitoolMeta != null;
            multitoolMeta.setDamage(preferredToolMeta.getDamage());
            e.getItem().setItemMeta(multitoolMeta);
        }
    }

    @Override
    public void useAnvil(PrepareAnvilEvent e)
    {
        AnvilInventory inventory = e.getInventory();
        ItemStack leftItem = inventory.getItem(0);
        ItemStack rightItem = inventory.getItem(1);

        if(isThis(leftItem) && isThis(rightItem))
            return;

        if(isThis(leftItem))
        {
            // left item is multitool
            if(rightItem == null)
                return;
            if(!EnchantmentTarget.TOOL.includes(rightItem))
                return;
            UUID uuid = ItemManager.getUUID(leftItem);
            Queue<ItemStack> queue = multitoolMap.get(uuid);

            if(queue == null)
                queue = new LinkedList<>();

            queue.offer(Objects.requireNonNull(rightItem).clone());
            Objects.requireNonNull(rightItem).setAmount(0);
            multitoolMap.put(uuid, queue);
        }
        else if (isThis(rightItem))
        {
            // right item is multitool
            if(leftItem != null)
                return;
            ItemStack multitool = Objects.requireNonNull(rightItem);
            UUID uuid = ItemManager.getUUID(rightItem);
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
                    assert multitoolMeta != null;
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
        assert multitool != null;
        if(!ItemManager.isCustomItem(multitool, getID()))
            return;
        ItemStack preferredTool = getPreferredTool(event.getBlock(), multitoolMap.get(ItemManager.getUUID(multitool)));
        if(preferredTool == null)
            return;

        // run next tick to let damage calculate
        Bukkit.getScheduler().runTaskLater(BeeSMP.getPlugin(BeeSMP.class), () -> {
            Damageable mtMeta = (Damageable) multitool.getItemMeta();
            assert mtMeta != null;
            Damageable ptMeta = (Damageable) preferredTool.getItemMeta();
            assert ptMeta != null;

            ptMeta.setDamage(mtMeta.getDamage());
            preferredTool.setItemMeta(ptMeta);
        }, 1);
    }

    @EventHandler
    public void onToolBreak(PlayerItemBreakEvent e)
    {
        if(!isThis(e.getBrokenItem()))
            return;
        ItemStack multitool = e.getBrokenItem();
        Queue<ItemStack> queue = multitoolMap.get(ItemManager.getUUID(e.getBrokenItem()));

        for(ItemStack item : queue)
        {
            if(Objects.equals(item.getEnchantments(), multitool.getEnchantments()) &&
                item.getType() == multitool.getType()
            )
            {
                queue.remove(item);
                // run one tick later
                Bukkit.getScheduler().runTaskLater(BeeSMP.getPlugin(BeeSMP.class), () -> multitool.setAmount(1), 1);
                break;
            }
        }

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
