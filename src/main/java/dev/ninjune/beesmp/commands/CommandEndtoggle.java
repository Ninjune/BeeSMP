package dev.ninjune.beesmp.commands;

import dev.ninjune.beesmp.BeeSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CommandEndtoggle extends BeeSMPCommand
{
    private static final String[] aliases = new String[]{"endtoggle"};

    @Override
    public boolean execute(CommandSender commandSender, Command command, String s, String[] strings)
    {
        FileConfiguration config = BeeSMP.getPlugin(BeeSMP.class).config;
        config.set("allow-end", !config.getBoolean("allow-end"));
        commandSender.sendMessage("[BeeSMP] Endtoggle set to: " + config.getBoolean("allow-end"));
        BeeSMP.getPlugin(BeeSMP.class).saveConfig();
        return true;
    }

    @Override
    public String[] getAliases()
    {
        return aliases;
    }

    @EventHandler
    public void onEnterPortal(PlayerPortalEvent event)
    {
        boolean allowEnd = BeeSMP.getPlugin(BeeSMP.class).config.getBoolean("allow-end");
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL &&
                !allowEnd)
        {
            event.setCancelled(true);
        }
    }
}