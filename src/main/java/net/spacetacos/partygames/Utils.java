package net.spacetacos.partygames;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.function.Predicate;

public class Utils {

    public static int reset(PartyGamesPlugin plugin, String path, Predicate<Block> predicate, Material material) {
        Location pos1 = plugin.getConfigLocation(path + ".pos1");
        Location pos2 = plugin.getConfigLocation(path + ".pos2");

        int count = 0;
        // Loops through each block in the rectangle
        for (int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++) {
            for (int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++) {

                // Gets the block at the location
                Location location = new Location(pos1.getWorld(), x, pos1.getBlockY(), z);
                Block block = location.getBlock();

                // Checks if the block at the location ends with the selected block type
                if (predicate.test(block)) {
                    // Sets the block to water
                    block.setType(material);
                }

                // Checks if the block at the location is water
                if (block.getType() == material) {
                    // Counts the amount of water in the circle
                    count++;
                }
            }
        }
        return count;
    }

}
