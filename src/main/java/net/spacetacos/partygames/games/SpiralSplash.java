package net.spacetacos.partygames.games;

import net.spacetacos.partygames.PartyGamesGame;
import net.spacetacos.partygames.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpiralSplash extends PartyGame {
    // Select what type of block is cleared from the pool at the start
    private static final String BLOCK_TYPE = "WOOL";

    // Creates maps to track each player's spawn point, material, and amount of points
    private Map<UUID, Location> spawns = new HashMap<>();
    private Map<UUID, Material> materials = new HashMap<>();
    private Map<UUID, Integer> points = new HashMap<>();

    // Sets up counter variables
    private int countWater = 0;
    private int hits = 0;

    // Begins the PartyGamesGame
    public SpiralSplash(PartyGamesGame partyGamesGame) {
        super("Spiral Splash", partyGamesGame);
    }

    // Start method
    @Override
    public void start() {
        // Retrieves the spawns and colors lists from config.yml
        List<Location> list = getPartyGamesGame().getPlugin().getConfigLocations("spiralsplash.spawns");
        List<Material> colors = getPartyGamesGame().getPlugin().getConfigMaterials("colors");

        // Clearing the water
        // Gets the corners of the rectangle surrounding the circle of water from config.yml
        countWater = Utils.reset(getPartyGamesGame().getPlugin(), "spiralsplash.watercoords",
                b -> b.getType().name().endsWith(BLOCK_TYPE), Material.WATER);

        // Counter variable
        int i = 0;
        // Loops through all online players and sets each of them up
        for (Player player : getPartyGamesGame().getOnlinePlayers()) {
            // Assigns this player a location and material
            Location location = list.get(i);
            Material material = colors.get(i);

            // Puts the player's spawn, material, and point information into maps to refer to later
            spawns.put(player.getUniqueId(), location);
            materials.put(player.getUniqueId(), material);
            points.put(player.getUniqueId(), 0);

            // Teleports player, sets their gamemode, and clears their inventory
            setupPlayer(player, location);

            i++;
        }
    }

    // End method
    @Override
    public void end() {
        // Sorts the participating players in order of how many points they have using magic
        List<Map.Entry<UUID, Integer>> placements = points.entrySet().stream().sorted((entry1, entry2) -> {
            return entry2.getValue() - entry1.getValue(); // Sorts from greatest to least
        }).collect(Collectors.toList());

        // Loops through all online players
        for (Player player : getPartyGamesGame().getOnlinePlayers()) {

            // Displays the winners in chat
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Spiral Splash Winners:");
            int i = 1;
            for (Map.Entry<UUID, Integer> place : placements) {
                player.sendMessage("#" + i + " - " + getName(place.getKey()) + " (" + place.getValue() + ")");
                i++;
            }
            // Sets gamemode of player to Creative
            player.setGameMode(GameMode.CREATIVE);
        }
        // Ends the current party game
        getPartyGamesGame().setGame(null);
        // Closes the current party game
        getPartyGamesGame().close(); //FIXME: REMEMBER TO FIX THIS LATER
    }

    // Listens for the onPlayerDamage event
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        // Checks if the cause of the player damage is from falling
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {

            // Negates fall damage
            event.setCancelled(true);

            // Saves the player and their spawn location as variables
            Player player = (Player) event.getEntity();
            Location spawn = spawns.get(player.getUniqueId());

            // Checks if they took fall damage at a y level under their spawn
            if (player.getLocation().getBlockY() < spawn.getBlockY()) {

                // Teleports the player to their spawn
                player.teleport(spawn);

                // Plays a sound effect to the player
                player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_STUNNED, 1, 1);
            }
        }
    }

    // Listens for the onPlayerMove event
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
       // Checks if the block the player is moving into is water
       Block block = event.getTo().getBlock();
       if (block.getType() == Material.WATER) {

           Player player = event.getPlayer();

           // Sets the block that the player landed in to their assigned material
           block.setType(materials.get(player.getUniqueId()));

           // Teleports the player to their spawn
           player.teleport(spawns.get(player.getUniqueId()));

           // Plays a "ding!" sound
           player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);

           // Gives the player a point and increments the global hits variable
           points.put(player.getUniqueId(), points.get(player.getUniqueId()) + 1 );
           hits++;

           // If the amount of placed blocks is equal to the amount of water total, end the game
           if (hits >= countWater) {
                end();
           }
       }

    }
}