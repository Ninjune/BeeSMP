package dev.ninjune.beesmp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Objects;

public class BeeSMP extends JavaPlugin
{
    public static final Path DATA_FOLDER = Path.of("./plugins/BeeSMP/").toAbsolutePath();
    public FileConfiguration config = this.getConfig();

    @Override
    public void onEnable() {
        getLogger().info("Hello world!");

        config.addDefault("allow-end", true);
        config.options().copyDefaults(true);
        saveConfig();
        Objects.requireNonNull(this.getCommand("bsmp")).setExecutor(CommandManager.executor);
        Objects.requireNonNull(this.getCommand("bsmp")).setTabCompleter(CommandManager.tabCompletor);

        getServer().getPluginManager().registerEvents(new ItemManager(), this);
        ItemManager.getCustomItems().forEach(item -> {
            getServer().getPluginManager().registerEvents(item, this);
        });
        CommandManager.getCommands().forEach(command -> {
            getServer().getPluginManager().registerEvents(command, this);
        });
    }

    @Override
    public void onDisable() {
        ItemManager.disable();
    }
}