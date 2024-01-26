package dev.ninjune.beesmp.managers;

import dev.ninjune.beesmp.commands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;


public class CommandManager
{
    public static CommandManagerExecutor executor = new CommandManagerExecutor();
    public static CommandManagerTabComplete tabCompletor = new CommandManagerTabComplete();
    private static final HashSet<BeeSMPCommand> commands = new HashSet<>();

    static
    {
        registerCommand(CommandToggle.getInstance());
        registerCommand(new CommandGive());
        registerCommand(new CommandInfo());
        registerCommand(new CommandPlaytime());
    }

    public static void registerCommand(BeeSMPCommand command)
    {
        commands.add(command);
    }
    public static HashSet<BeeSMPCommand> getCommands() { return commands; }

    public static class CommandManagerExecutor implements CommandExecutor
    {
        @Override
        public boolean onCommand(@NotNull CommandSender commandSender,
                                 @NotNull Command command,
                                 @NotNull String s,
                                 String[] strings
        )
        {
            if(strings[0] != null)
            {
                for(BeeSMPCommand c : commands)
                {
                    if(Objects.equals(c.getAliases()[0], strings[0]) &&
                            (c.getPermission() == null || commandSender.hasPermission(c.getPermission()))
                    )
                        return c.execute(commandSender, command, s, strings);
                }
            }

            commandSender.sendMessage("Failed to find command or there was an error running that command!");
            return false;
        }
    }

    public static class CommandManagerTabComplete implements TabCompleter
    {
        @Override
        public List<String> onTabComplete(@NotNull CommandSender commandSender,
                                          @NotNull Command command,
                                          @NotNull String s,
                                          String[] strings
        )
        {
            ArrayList<String> values = new ArrayList<>();
            if(strings.length < 1)
                return null;

            for(BeeSMPCommand c : commands)
            {
                if(!(c.getPermission() == null || commandSender.hasPermission(c.getPermission())))
                    continue;
                for(String alias : c.getAliases())
                {

                    if(!Objects.equals(alias, strings[0]) && alias.startsWith(strings[0]))
                        values.add(alias);
                    else if(Objects.equals(alias, strings[0]) && c.tabComplete(strings) != null)
                        values.addAll(c.tabComplete(strings));
                }
            }

            return values;
        }
    }
}

