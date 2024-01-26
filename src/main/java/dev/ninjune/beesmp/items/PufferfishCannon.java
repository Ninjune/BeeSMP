package dev.ninjune.beesmp.items;

import dev.ninjune.beesmp.BeeSMP;
import dev.ninjune.beesmp.managers.ItemManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPufferFish;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PufferfishCannon extends BeeSMPItem
{
    private final HashMap<UUID, Long> lastUses = new HashMap<>();
    private final HashMap<UUID, CraftPufferFish> activePufferfish = new HashMap<>();
    //private final HashMap<UUID, > = new HashMap<>();
    private static final int COOLDOWN_TIME = 5000;
    private static final double VELOCITY_MULTIPLIER = 1.25;

    @Override
    public String getName()
    {
        return "§ePufferfish Cannon";
    }

    @Override
    public String getID()
    {
        return "gun_pufferfish";
    }

    @Override
    public Material getMaterial()
    {
        return Material.GOLDEN_HOE;
    }

    @Override
    public List<Recipe> getRecipes()
    {
        ShapedRecipe recipe = new ShapedRecipe(getRecipeNamespace(), this.clone());
        recipe.shape("pds", ",sc", "..d");
        recipe.setIngredient('p', Material.PUFFERFISH_BUCKET);
        recipe.setIngredient('d', Material.DIAMOND);
        recipe.setIngredient('s', Material.SPONGE);
        recipe.setIngredient(',', Material.PRISMARINE);
        recipe.setIngredient('c', Material.CROSSBOW);

        return List.of(recipe);
    }

    @Override
    public List<Enchantment> getApplicableEnchants()
    {
        return List.of(Enchantment.DURABILITY, Enchantment.MENDING);
    }

    private static class CannonPufferfish extends Pufferfish
    {
        public CannonPufferfish(EntityType<? extends Pufferfish> entitytypes, Level world)
        {
            super(entitytypes, world);
        }

        // Override the tick method to modify the jumping behavior
        @Override
        public void tick() {
            if(onGround)
                onGround = false;
            setAirSupply(999);
            super.tick();
        }
    }

    @EventHandler
    public void onUnload(PluginDisableEvent event)
    {
        for(CraftPufferFish craftPufferFish : activePufferfish.values())
            craftPufferFish.getHandle().remove(Entity.RemovalReason.DISCARDED);
        activePufferfish.clear();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        if(e.getItem() == null)
            return;
        if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if(!ItemManager.isCustomItem(e.getItem(), getID()))
            return;
        Long lastUse = lastUses.get(e.getPlayer().getUniqueId());
        if(lastUse != null && System.currentTimeMillis() - lastUse <= COOLDOWN_TIME)
            return;
        lastUses.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
        UUID playerUUID = e.getPlayer().getUniqueId();
        if(activePufferfish.get(playerUUID) != null)
        {
            activePufferfish.get(playerUUID).getHandle().remove(Entity.RemovalReason.DISCARDED);
            activePufferfish.remove(playerUUID);
        }

        Vector velocity = getVelocityFromAngles(e.getPlayer().getLocation().getYaw(), e.getPlayer().getLocation().getPitch(), VELOCITY_MULTIPLIER);

        CraftPlayer craftPlayer = (CraftPlayer) e.getPlayer();
        ServerPlayer serverPlayer = craftPlayer.getHandle();

        CannonPufferfish mcPufferfish = new CannonPufferfish(EntityType.PUFFERFISH, serverPlayer.level());
        Location pos = e.getPlayer().getEyeLocation().add(new Vector(velocity.getX()/VELOCITY_MULTIPLIER,
                velocity.getY()/VELOCITY_MULTIPLIER,
                velocity.getZ()/VELOCITY_MULTIPLIER)
        );
        mcPufferfish.setPos(pos.getX(), pos.getY(), pos.getZ());
        CraftPufferFish pufferfish = (CraftPufferFish) mcPufferfish.getBukkitEntity();

        pufferfish.setVelocity(velocity);
        pufferfish.setMetadata("cannonFish", new FixedMetadataValue(BeeSMP.getPlugin(BeeSMP.class), Boolean.TRUE));
        pufferfish.setMetadata("shooter", new FixedMetadataValue(BeeSMP.getPlugin(BeeSMP.class), e.getPlayer().getUniqueId().toString()));
        pufferfish.setPuffState(3);
        pufferfish.setGravity(false);
        pufferfish.setCustomName(craftPlayer.getName() + "'s Pufferfish");
        pufferfish.setCustomNameVisible(false);

        mcPufferfish = (CannonPufferfish) pufferfish.getHandle();

        serverPlayer.level().getMinecraftWorld().addFreshEntity(mcPufferfish);
        activePufferfish.put(craftPlayer.getUniqueId(), pufferfish);
        ItemManager.damageWithUnbreaking(e.getItem());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        List<MetadataValue> metadata = event.getEntity().getMetadata("cannonFish");
        metadata.forEach(metadataValue -> {
            if(metadataValue.getOwningPlugin() instanceof BeeSMP)
                if(metadataValue.asBoolean())
                    event.getDrops().clear();
        });
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e)
    {
        if(isThis(e.getPlayer().getItemInUse()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event)
    {
        List<MetadataValue> metadata1 = event.getDamager().getMetadata("shooter");

        for(MetadataValue metadataValue : metadata1)
        {
            if(metadataValue.getOwningPlugin() instanceof BeeSMP)
            {
                if (event.getEntity().getUniqueId().toString().equals(metadataValue.asString()))
                {
                    event.setCancelled(true);
                    continue;
                }
                ((CraftEntity) event.getDamager()).getHandle().remove(Entity.RemovalReason.DISCARDED);
            }
        }

        List<MetadataValue> metadata2 = event.getEntity().getMetadata("shooter");
        if(!(event.getDamager() instanceof Player player))
            return;

        for(MetadataValue metadataValue : metadata2)
        {
            if(metadataValue.getOwningPlugin() instanceof BeeSMP)
            {
                event.getEntity().setVelocity(
                        getVelocityFromAngles(player.getLocation().getYaw(), player.getLocation().getPitch(), 1)
                );
                event.setCancelled(true);
            }
        }
    }

    private static Vector getVelocityFromAngles(double yaw, double pitch, double multiplier)
    {
        yaw = Math.toRadians(yaw);
        pitch = Math.toRadians(-1*pitch);
        double mod = multiplier * Math.cos(pitch);
        return new Vector(-1*mod*Math.sin(yaw),multiplier*Math.sin(pitch),mod*Math.cos(yaw));
    }
}