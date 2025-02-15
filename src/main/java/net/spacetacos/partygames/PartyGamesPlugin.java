package net.spacetacos.partygames;

import net.spacetacos.partygames.commands.PartyGamesCommand;
import net.trollyloki.minigames.library.MiniGameLibraryPlugin;
import net.trollyloki.minigames.library.managers.MiniGameManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PartyGamesPlugin extends JavaPlugin {
    private MiniGameManager miniGameManager;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Plugin miniGameLibrary = getServer().getPluginManager().getPlugin("MiniGameLibrary");
        miniGameManager = ((MiniGameLibraryPlugin) miniGameLibrary).getMiniGameManager();
        getCommand("partygames").setExecutor(new PartyGamesCommand(this));
    }

    public MiniGameManager getMiniGameManager() {
        return miniGameManager;
    }

    public List<Location> getConfigLocations(String path) {
        ArrayList<Location> list = new ArrayList<>();
        for (Map<?, ?> map : getConfig().getMapList(path)) {
            list.add(new Location(getServer().getWorld((String) map.get("world")), (double)map.get("x"), (double)map.get("y"), (double)map.get("z"), (float)(double)map.get("yaw"), (float)(double)map.get("pitch")));
        }
        return list;
    }

    public List<Material> getConfigMaterials(String path) {
        ArrayList<Material> list = new ArrayList<>();
        for (String string : getConfig().getStringList(path)) {
            list.add(Material.valueOf(string));
        }
        return list;
    }

    public Location getConfigLocation(String path) {
        ConfigurationSection section = getConfig().getConfigurationSection(path);
        World world = getServer().getWorld(section.getString("world"));
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        if (section.contains("yaw") && section.contains("pitch"))
            return new Location(world, x, y, z, (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
        else
            return new Location(world, x, y, z);
    }
}

