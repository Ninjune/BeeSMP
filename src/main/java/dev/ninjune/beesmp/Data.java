package dev.ninjune.beesmp;

import org.bukkit.Bukkit;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Data
{
    Serializable s;

    public Data()
    {
        this.s = null;
    };

    public Data(Serializable s)
    {
        this.s = s;
    }

    public void saveData(Path filePath)
    {

        try
        {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(filePath.toString())));

            out.writeObject(s);
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void loadData(Path filePath)
    {
        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(filePath.toString())));

            s = (Serializable) in.readObject();
            in.close();
        } catch (ClassNotFoundException | IOException e) {
            if(e instanceof FileNotFoundException)
            {
                saveData(filePath);
                Bukkit.getLogger().info("Created the file " + filePath);
            }
            else
                e.printStackTrace();

        }
    }

    public Serializable getData() { return s; }
}
