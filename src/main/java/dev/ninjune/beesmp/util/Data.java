package dev.ninjune.beesmp.util;

import org.bukkit.Bukkit;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Data
{
    public static final String DATA_FOLDER = "./plugins/BeeSMP/";
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
        filePath = Path.of(DATA_FOLDER + filePath.toString());
        String stringPath = filePath.toAbsolutePath().toString();

        try
        {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(
                    new GZIPOutputStream(
                            new FileOutputStream(stringPath)
                    )
            );

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
        filePath = Path.of(DATA_FOLDER + filePath.toString());
        String stringPath = filePath.toAbsolutePath().toString();

        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(
                    new GZIPInputStream(
                            new FileInputStream(stringPath)
                    )
            );

            Object o = in.readObject();

            if(o == null)
                throw new FileNotFoundException();

            s = (Serializable) o;
            in.close();
        } catch (ClassNotFoundException | IOException e) {
            Bukkit.getLogger().warning("Tried to read file " + stringPath);
            if(e instanceof FileNotFoundException)
            {
                saveData(Path.of(stringPath));
                Bukkit.getLogger().info("Created the file " + stringPath);
            }
            else
                e.printStackTrace();
        }
    }

    public Serializable getData() { return s; }
}
