package dev.ninjune.beesmp.items;

import dev.ninjune.beesmp.managers.ItemManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DevilsMark extends BeeSMPItem
{
    private long lastUse;
    @Override
    public String getName()
    {
        return "§eDevil's Mark";
    }

    @Override
    public String getID()
    {
        return "devil_mark";
    }

    @Override
    public Material getMaterial()
    {
        return Material.BOOK;
    }

    @Override
    public List<String> getLore()
    {
        List<String> lore = new ArrayList<>();
        lore.add("§8§oConsumable");
        lore.add("§bUse this on a player to bind that player.");
        lore.add("§bUse again to tp the bound player to yourself.");
        lore.add("§bNote that there are many debuffs when utilizing this item.");
        lore.add("");
        lore.add("§bBound to: Blank");
        return lore;
    }

    @Override
    public List<Recipe> getRecipes()
    {
        ShapedRecipe recipe = new ShapedRecipe(getRecipeNamespace(), this.clone());
        recipe.shape("r*r","|b|",".s.");
        recipe.setIngredient('r', Material.REDSTONE);
        recipe.setIngredient('*', Material.NETHER_STAR);
        recipe.setIngredient('|', Material.ECHO_SHARD);
        recipe.setIngredient('b', Material.BOOK);
        //recipe.setIngredient('.', Material.AIR);
        recipe.setIngredient('s', Material.SOUL_SAND);
        return List.of(recipe);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        if(e.getItem() == null ||
                (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) ||
                !ItemManager.isCustomItem(e.getItem(), getID())
        )
            return;
        if(System.currentTimeMillis() - lastUse < 1000)
        {
            e.setCancelled(true);
            return;
        }
        lastUse = System.currentTimeMillis();

        String boundPlayer = ItemManager.getNBT(e.getItem(), "bound");
        boolean changeLore = false;
        if(!boundPlayer.isEmpty() && !boundPlayer.equals("#blank")) // bound
        {
            boolean found = false;

            for(Player player : Bukkit.getOnlinePlayers())
            {
                if(!player.getName().equals(boundPlayer))
                    continue;
                found = true;
                player.teleport(e.getPlayer());
                e.getPlayer().addPotionEffects(new ArrayList<>() {{
                    add(new PotionEffect(PotionEffectType.WEAKNESS, 1200, 1));
                }});
                break;
            }

            if(found)
            {
                e.getItem().setType(Material.BOOK);
                ItemManager.setNBT(e.getItem(), "bound", "#blank");
                boundPlayer = "Blank";
                changeLore = true;
                e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), "minecraft:entity.ender_dragon.growl",
                        SoundCategory.HOSTILE, 0.5F, 1);
                // delete item
                e.getItem().setAmount(0);
            }
        }
        else
        {
            Entity result = getNearestEntityInSight(e.getPlayer(), 4);

            if(result instanceof Player player)
            {
                ItemManager.setNBT(e.getItem(), "bound", player.getName());
                e.getItem().setType(Material.WRITTEN_BOOK);
                e.getPlayer().addPotionEffects(new ArrayList<>() {{
                    add(new PotionEffect(PotionEffectType.SLOW, 1200, 1));
                }});
                boundPlayer = player.getName();
                changeLore = true;
            }
        }

        if(changeLore)
        {
            ItemMeta meta = e.getItem().getItemMeta();
            assert meta != null;
            List<String> lore = meta.getLore();
            assert lore != null;

            if (lore.size() >= 6)
            {
                lore.set(5, "§bBound to: " + boundPlayer);
                meta.setLore(lore);
                e.getItem().setItemMeta(meta);
            }
        }
    }

    @Nullable
    public static Entity getNearestEntityInSight(Player player, int range) {
        ArrayList<Entity> entities = (ArrayList<Entity>) player.getNearbyEntities(range, range, range);
        ArrayList<Block> sightBlock = (ArrayList<Block>) player.getLineOfSight(null, range);
        ArrayList<Location> sight = new ArrayList<>();

        for (Block block : sightBlock)
            sight.add(block.getLocation());

        for (Location location : sight)
        {
            for (Entity entity : entities)
            {
                if (Math.abs(entity.getLocation().getX() - location.getX()) < 1.3 &&
                        Math.abs(entity.getLocation().getY() - location.getY()) < 1.5 &&
                        Math.abs(entity.getLocation().getZ() - location.getZ()) < 1.3
                )
                            return entity;
            }
        }

        return null;
    }
}
