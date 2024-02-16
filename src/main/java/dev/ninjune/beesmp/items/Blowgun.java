package dev.ninjune.beesmp.items;

import dev.ninjune.beesmp.managers.ItemManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Blowgun extends BeeSMPItem
{
    private static final long cooldownMs = 5000;
    private final HashMap<UUID, Long> lastUses = new HashMap<>();

    @Override
    public String getName()
    {
        return "§aBlowgun";
    }

    @Override
    public String getID()
    {
        return "blowgun";
    }

    @Override
    public Material getMaterial()
    {
        return Material.GOLDEN_HOE;
    }

    @Override
    public List<Enchantment> getApplicableEnchants()
    {
        return List.of(Enchantment.DURABILITY, Enchantment.MENDING);
    }

    @Override
    public List<String> getLore()
    {
        return List.of("§bShoots darts (arrows) that blind and poison enemies.");
    }

    @Override
    public List<Recipe> getRecipes()
    {
        ShapedRecipe recipe = genRecipe();
        recipe.shape("...", "BSB", "...");
        recipe.setIngredient('B', Material.BAMBOO);
        recipe.setIngredient('S', Material.STRING);
        return List.of(recipe);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        if(!isThis(e.getItem()) || !e.getAction().toString().contains("RIGHT_CLICK"))
            return;
        if(!e.getPlayer().getInventory().contains(Material.ARROW,1 ))
            return;
        Long lastUse = lastUses.get(e.getPlayer().getUniqueId());
        if(lastUse == null)
            lastUse = 0L;

        if(System.currentTimeMillis() - lastUse < cooldownMs)
            return;

        lastUses.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
        e.setCancelled(true);

        e.getPlayer().getInventory().removeItem(new ItemStack(Material.ARROW, 1));
        Arrow arrow = e.getPlayer().launchProjectile(Arrow.class);
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 100, 0);
        PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 100, 0);
        arrow.addCustomEffect(blindness, false);
        arrow.addCustomEffect(poison, false);
        arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
        arrow.setDamage(0.1);
        ItemManager.damageWithUnbreaking(Objects.requireNonNull(e.getItem()));
    }
}
