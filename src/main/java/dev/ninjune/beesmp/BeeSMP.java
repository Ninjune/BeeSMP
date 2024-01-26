package dev.ninjune.beesmp;

import dev.ninjune.beesmp.commands.CommandToggle;
import dev.ninjune.beesmp.managers.CommandManager;
import dev.ninjune.beesmp.managers.ItemManager;
import dev.ninjune.beesmp.managers.ObjectiveManager;
import dev.ninjune.beesmp.util.InitShutdownListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BeeSMP extends JavaPlugin
{
    private static final Set<Runnable> tickRunners = new HashSet<>();
    private static final Set<InitShutdownListener> initShutdownListeners = new HashSet<>();

    @Override
    public void onEnable() {
        getLogger().info("Hello world!");
        initShutdownListeners.add(CommandToggle.getInstance());

        initShutdownListeners.forEach(InitShutdownListener::onEnable);

        Objects.requireNonNull(this.getCommand("bsmp")).setExecutor(CommandManager.executor);
        Objects.requireNonNull(this.getCommand("bsmp")).setTabCompleter(CommandManager.tabCompletor);

        ObjectiveManager.init();
        getServer().getPluginManager().registerEvents(new ItemManager(), this);
        ItemManager.getCustomItems().forEach(item -> {
            getServer().getPluginManager().registerEvents(item.getEvents(), this);
        });
        CommandManager.getCommands().forEach(command -> {
            getServer().getPluginManager().registerEvents(command, this);
        });

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            tickRunners.forEach(Runnable::run);
        }, 0, 1);
    }

    @Override
    public void onDisable() {
        initShutdownListeners.forEach(InitShutdownListener::onDisable);
        ItemManager.disable();
    }

    public static void runEveryTick(Runnable runnable)
    {
        tickRunners.add(runnable);
    }
}