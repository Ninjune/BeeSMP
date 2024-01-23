package dev.ninjune.beesmp.items.talisman;

public class TalismanDragonEgg extends Talisman
{
    @Override
    public String getName()
    {
        return "Dragon Egg";
    }

    @Override
    public String getID()
    {
        return "";
    }

    @Override
    public Material getMaterial()
    {
        return Material.DRAGON_EGG;
    }

    @Override
    public void runEverySecond()
    {
        for(Player player : Bukkit.getOnlinePlayers())
        {
            if(!(player.getInventory.contains(getMaterial(), 1)))
                continue;

        }
    }
}
