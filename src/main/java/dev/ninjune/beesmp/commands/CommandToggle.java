package dev.ninjune.beesmp.commands;

import dev.ninjune.beesmp.BeeSMP;
import dev.ninjune.beesmp.util.Data;
import dev.ninjune.beesmp.util.InitShutdownListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandToggle extends BeeSMPCommand implements InitShutdownListener
{
    private static final CommandToggle instance = new CommandToggle();
    private final String SAVE_PATH = "toggles.data";
    private final String[] aliases = new String[]{"toggle"};
    private HashMap<String, Boolean> toggleables = new HashMap<>();

    private CommandToggle()
    {
        toggleables.put("end", true);
        toggleables.put("elytra", true);
        BeeSMP.runEveryTick(() -> {
            if(toggleables.get("elytra"))
                return;
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.getInventory().remove(Material.ELYTRA);
            });
        });
    }

    @Override
    public boolean execute(CommandSender commandSender, Command command, String s, String[] strings)
    {
        Boolean current = toggleables.get(strings[1].toLowerCase());

        if(strings.length != 2 || current == null)
            return false;
        toggleables.put(strings[1].toLowerCase(), !current);
        commandSender.sendMessage("[BeeSMP] Toggle " + strings[1].toLowerCase() + " set to: " + !current);
        return true;
    }

    @Override
    public List<String> tabComplete(String[] strings)
    {
        ArrayList<String> values = new ArrayList<>();
        if(strings.length != 2)
            return null;
        toggleables.keySet().forEach(key -> {
            if(key.startsWith(strings[1]))
                values.add(key);
        });

        return values;
    }

    @Override
    public String[] getAliases()
    {
        return aliases;
    }

    @Override
    public void onEnable()
    {
        Data data = new Data(toggleables);
        data.loadData(Path.of(SAVE_PATH));
        toggleables = (HashMap<String, Boolean>) data.getData();
    }

    @Override
    public void onDisable()
    {
        Data data = new Data(toggleables);
        data.saveData(Path.of(SAVE_PATH));
    }

    public static CommandToggle getInstance()
    {
        return instance;
    }

    @EventHandler
    public void onEnterPortal(PlayerPortalEvent event)
    {
        boolean allowEnd = toggleables.get("end");
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL && !allowEnd)
            event.setCancelled(true);
    }
}