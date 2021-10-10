package net.spacetacos.partygames.games;

import net.spacetacos.partygames.PartyGamesGame;
import net.spacetacos.partygames.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class AceOfSpades extends PartyGame {

    private static final String BLOCK_TYPE = "WOOL";

    private Map<UUID, Material> materials = new HashMap<>();
    private Map<UUID, Integer> points = new HashMap<>();

    private int countGrass = 0;
    private int claims = 0;

    public AceOfSpades(PartyGamesGame partyGamesGame) {
        super("Ace of Spades", partyGamesGame);
    }

    @Override
    public void start() {
        Location spawn = getPartyGamesGame().getPlugin().getConfigLocation("aceofspades.spawn");
        List<Material> colors = getPartyGamesGame().getPlugin().getConfigMaterials("colors");

        ItemStack hoe = new ItemStack(Material.NETHERITE_HOE);
        ItemMeta meta = hoe.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Claiming Hoe");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Right click to claim land!"));
        hoe.setItemMeta(meta);

        countGrass = Utils.reset(getPartyGamesGame().getPlugin(), "aceofspades.area",
                b -> b.getType().name().endsWith(BLOCK_TYPE), Material.GRASS_BLOCK);

        // Counter variable
        int i = 0;
        // Loops through all online players and sets each of them up
        for (Player player : getPartyGamesGame().getOnlinePlayers()) {
            // Assigns this player a location and material
            Material material = colors.get(i);

            // Puts the player's spawn, material, and point information into maps to refer to later
            materials.put(player.getUniqueId(), material);
            points.put(player.getUniqueId(), 0);

            // Teleports player, sets their gamemode, and clears their inventory
            setupPlayer(player, spawn);
            player.getInventory().addItem(hoe);

            i++;
        }
    }

    @Override
    public void end() {
        // Sorts the participating players in order of how many points they have using magic
        List<Map.Entry<UUID, Integer>> placements = points.entrySet().stream().sorted((entry1, entry2) -> {
            return entry2.getValue() - entry1.getValue(); // Sorts from greatest to least
        }).collect(Collectors.toList());

        // Loops through all online players
        for (Player player : getPartyGamesGame().getOnlinePlayers()) {

            // Displays the winners in chat
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Ace of Spades Winners:");
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

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock().getType() == Material.GRASS_BLOCK
                && event.getItem() != null && event.getItem().getType().name().endsWith("HOE")) {

            event.setCancelled(true);

            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            Material material = materials.get(player.getUniqueId());
            int currentPoints = points.get(player.getUniqueId());

            if (currentPoints == 0 || hasAdjacent(block, material)) {

                points.put(player.getUniqueId(), currentPoints + 1);
                block.setType(material);
                player.playSound(block.getLocation(), Sound.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1, 1);
                claims++;
                if (claims >= countGrass)
                    end();

            } else {
                player.sendMessage(ChatColor.RED + "You can only claim adjacent to your own land!");
            }

        }
    }

    private static final BlockFace[] ADJACENT_FACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private boolean hasAdjacent(Block block, Material adjacent) {
        for (BlockFace face : ADJACENT_FACES) {
            if (block.getRelative(face).getType() == adjacent)
                return true;
        }
        return false;
    }

}
