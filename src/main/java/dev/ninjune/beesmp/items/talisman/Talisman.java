package dev.ninjune.beesmp.items.talisman;

import dev.ninjune.beesmp.managers.ItemManager;
import dev.ninjune.beesmp.items.BeeSMPItem;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public abstract class Talisman extends BeeSMPItem
{
    public abstract void runEverySecond();

    protected HashSet<ImmutablePair<Player, ItemStack>> findItemsInInventories()
    {
        HashSet<ImmutablePair<Player, ItemStack>> retValue = new HashSet<>();
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        for(Player player : players)
        {
            player.getInventory().forEach(item -> {
                if(ItemManager.isCustomItem(item, getID()))
                    retValue.add(new ImmutablePair<>(player, item));
            });
        }

        return retValue;
    }

    protected List<String> getTalismanLore() { return new ArrayList<>(); }

    @Override
    public final List<String> getLore()
    {
        List<String> val = getTalismanLore();
        val.add("§cWarning: Talismans may not be moved into echests or shulkers.");
        return val;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e)
    {
        InventoryType type = e.getInventory().getType();
        ItemStack item = e.getCurrentItem();

        if(e.getClick() == ClickType.NUMBER_KEY)
            item = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());

        if(item == null ||
            !isThis(item) ||
            !(type == InventoryType.ENDER_CHEST || type == InventoryType.SHULKER_BOX)
        )
            return;

        e.setResult(Event.Result.DENY);
    }

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent e)
    {
        if(isThis(e.getItem()))
            e.setCancelled(true);
    }
}
