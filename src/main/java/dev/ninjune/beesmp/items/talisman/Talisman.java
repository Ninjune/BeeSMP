package dev.ninjune.beesmp.items.talisman;

import dev.ninjune.beesmp.ItemManager;
import dev.ninjune.beesmp.items.BeeSMPItem;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Talisman extends BeeSMPItem
{
    public abstract void runEverySecond();

    protected HashSet<ImmutablePair<Player, ItemStack>> findItemsInInventories()
    {
        HashSet<ImmutablePair<Player, ItemStack>> retValue = new HashSet<>();
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        AtomicBoolean found = new AtomicBoolean(false);

        for(Player player : players)
        {
            player.getInventory().forEach(item -> {
                if(ItemManager.isCustomItem(item, getID()))
                    retValue.add(new ImmutablePair<>(player, item));
            });
        }

        return retValue;
    }
}
