package dev.ninjune.beesmp.items;

import dev.ninjune.beesmp.BeeSMP;
import dev.ninjune.beesmp.managers.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class EnderPearlCrossbow extends BeeSMPItem
{
    private final HashSet<String> consumed = new HashSet<>();
    private final HashSet<String> drawing = new HashSet<>();

    public EnderPearlCrossbow()
    {
        BeeSMP.runEveryTick(() -> Bukkit.getOnlinePlayers().forEach(player -> {
            Inventory inventory = player.getInventory();
            for(ItemStack item : inventory)
            {
                if(!ItemManager.isCustomItem(item, "ender_pearl_crossbow") ||
                        !((CrossbowMeta) Objects.requireNonNull(item.getItemMeta())).hasChargedProjectiles())
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
        }));
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

    @Override
    public List<String> getLore()
    {
        return List.of("§8§oConsumes 2 epearls", "§bSends an ender pearl on a further trajectory.");
    }

    @Override
    public List<Enchantment> getApplicableEnchants()
    {
        return List.of(Enchantment.QUICK_CHARGE);
    }

    @Override
    public List<Recipe> getRecipes()
    {
        ShapedRecipe recipe = genRecipe()
                .shape("doo","oc.","o.e")
                .setIngredient('e', Material.ENDER_EYE)
                .setIngredient('c', Material.CROSSBOW)
                .setIngredient('d', Material.DIAMOND)
                .setIngredient('o', Material.OBSIDIAN);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return List.of(recipe);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        //On interact
        if(!isThis(e.getItem()))
            return;
        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            if(drawing.contains(e.getPlayer().getName()) ||
                    ((CrossbowMeta) Objects.requireNonNull(Objects.requireNonNull(e.getItem()).getItemMeta())).hasChargedProjectiles()
            )
                return;
            boolean hasTwoPearls = e.getPlayer().getInventory().containsAtLeast(new ItemStack(Material.ENDER_PEARL), 2);

            if(!hasTwoPearls)
                e.setCancelled(true);
            else
                drawing.add(e.getPlayer().getName());
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

        if (isThis(item))
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
