package dev.ninjune.beesmp.items;

import dev.ninjune.beesmp.BeeSMP;
import dev.ninjune.beesmp.managers.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

import java.util.HashMap;
import java.util.HashSet;

public class EnderPearlCrossbow extends BeeSMPItem
{
    private final HashSet<String> consumed = new HashSet<>();
    private final HashSet<String> drawing = new HashSet<>();

    public EnderPearlCrossbow()
    {
        BeeSMP.runEveryTick(() -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                Inventory inventory = player.getInventory();
                for(ItemStack item : inventory)
                {
                    if(!ItemManager.isCustomItem(item, "ender_pearl_crossbow") ||
                            !((CrossbowMeta) item.getItemMeta()).hasChargedProjectiles())
                        continue;
                    if(!drawing.contains(player.getName()) || consumed.contains(player.getName()))
                        continue;

                    int consume = 2;
                    HashMap<Integer, ? extends ItemStack> epearls = player.getInventory().all(Material.ENDER_PEARL);
                    for(ItemStack pearl : epearls.values())
                    {
                        while(consume > 0 && pearl.getAmount() > 0)
                        {
                            pearl.setAmount(pearl.getAmount() - 1);
                            consume--;
                        }
                    }

                    drawing.remove(player.getName());
                    consumed.add(player.getName());
                }
            });
        });
    }

    @Override
    public String getName()
    {
        return "§bEnder Crossbow";
    }

    @Override
    public String getID()
    {
        return "ender_pearl_crossbow";
    }

    @Override
    public Material getMaterial()
    {
        return Material.CROSSBOW;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        //On interact
        if(e.getItem() == null)
            return;
        if(ItemManager.isCustomItem(e.getItem(), "ender_pearl_crossbow"))
        {
            if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
            {
                if(drawing.contains(e.getPlayer().getName()) ||
                        ((CrossbowMeta) e.getItem().getItemMeta()).hasChargedProjectiles()
                )
                    return;
                boolean hasTwoPearls = e.getPlayer().getInventory().containsAtLeast(new ItemStack(Material.ENDER_PEARL), 2);

                if(!hasTwoPearls)
                    e.setCancelled(true);
                else
                    drawing.add(e.getPlayer().getName());
            }
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event)
    {
        if (!(event.getEntity() instanceof Player player))
            return;
        if(!consumed.contains(player.getName()))
            return;
        ItemStack item = event.getBow();
        assert item != null;

        if (ItemManager.isCustomItem(item, "ender_pearl_crossbow"))
        {
            event.setCancelled(true);
            consumed.remove(player.getName());
            player.getInventory().addItem(new ItemStack(Material.ARROW));
            CrossbowMeta crossbowMeta = (CrossbowMeta) item.getItemMeta();
            assert crossbowMeta != null;
            crossbowMeta.setChargedProjectiles(null);

            EnderPearl enderPearl = player.launchProjectile(EnderPearl.class);
            enderPearl.setVelocity(event.getProjectile().getVelocity());
        }
    }
}
