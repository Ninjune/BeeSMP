package dev.ninjune.beesmp.managers;

import dev.ninjune.beesmp.BeeSMP;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Objects;

public class ObjectiveManager
{
    private static final HashMap<String, Objective> objectives = new HashMap<>();

    public static void init()
    {
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        objectives.put("playtime", null);
        objectives.put("deaths", null);

        scoreboard.getObjectives().forEach(objective -> {
            objectives.keySet().forEach(key -> {
                if(objective.getName().equalsIgnoreCase(key))
                    objectives.put(key, objective);
            });
        });

        objectives.keySet().forEach(key -> {
            if(objectives.get(key) == null)
            {
                Criteria criteria = Criteria.DUMMY;
                if(key.equalsIgnoreCase("deaths"))
                    criteria = Criteria.DEATH_COUNT;
                objectives.put(key, scoreboard.registerNewObjective(key, criteria, key));
            }
        });

        objectives.get("deaths").setDisplaySlot(DisplaySlot.PLAYER_LIST);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(BeeSMP.getPlugin(BeeSMP.class), () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                Score score = objectives.get("playtime").getScore(player.getName());
                score.setScore(score.getScore()+1);
            });
        }, 0, 20);
    }

    public static HashMap<String, Objective> getObjectives()
    {
        return (HashMap<String, Objective>) objectives.clone();
    }
}
