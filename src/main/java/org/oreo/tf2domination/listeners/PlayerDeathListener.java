package org.oreo.tf2domination.listeners;

import org.bukkit.ChatColor;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.oreo.tf2domination.Tf2Domination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerDeathListener implements Listener {

    public static final List<Map<Player, List<Map<Player, Integer>>>> dominationKills = new ArrayList<>();

    private final Tf2Domination plugin;

    public PlayerDeathListener(Tf2Domination plugin) {
        this.plugin = plugin;
    }

    private final String dominationSoundname = "tfnemesis";
    private final String revengeSoundname = "tfrevenge";

    @EventHandler
    public void DeathListener(PlayerDeathEvent e) {
        Player killer = e.getEntity().getKiller();

        if (killer == null) { // Make sure he died to a player
            return;
        }
        Player killed = e.getEntity();
        World world = e.getEntity().getWorld();

        int killsUntillDomination = this.plugin.getConfig().getInt("kills-until-domination");

        // Check if the killed player is already dominated by the killer
        for (Map<Player, List<Map<Player, Integer>>> map : dominationKills) {
            if (map.containsKey(killed)) {
                List<Map<Player, Integer>> list = map.get(killed);

                for (Map<Player, Integer> killsMap : list) {
                    if (killsMap.containsKey(killer)) {
                        int killCount = killsMap.get(killer);
                        if (killCount >= killsUntillDomination) {
                            // Play revenge sound
                            world.playSound(killed.getLocation(),  "crusalis:"+ revengeSoundname, SoundCategory.MASTER, 1.0f, 1.0f);
                            // Send messages
                            killer.sendMessage(ChatColor.DARK_GREEN + "You got revenge on " + killed.getName());
                            killed.sendMessage(ChatColor.DARK_RED + killer.getName() + " got revenge on you");

                            // Remove the killed player from the map after revenge
                            killsMap.remove(killer);
                            return;
                        }
                    }
                }
            }
        }

        // Check if the killer already has a hashmap
        boolean foundKiller = false;
        for (Map<Player, List<Map<Player, Integer>>> map : dominationKills) {
            if (map.containsKey(killer)) {
                List<Map<Player, Integer>> list = map.get(killer);
                boolean foundKilled = false;

                for (Map<Player, Integer> killsMap : list) {
                    if (killsMap.containsKey(killed)) {
                        int currentKills = killsMap.get(killed) + 1; // Get how many times he's been killed

                        killsMap.put(killed, currentKills); // Add one to the current kills

                        if (currentKills == killsUntillDomination) {
                            world.playSound(killed.getLocation(),  "crusalis:"+ dominationSoundname, SoundCategory.MASTER, 1.0f, 1.0f);
                            killer.sendMessage(ChatColor.DARK_GREEN + "You are dominating " + killed.getName());
                            killed.sendMessage(ChatColor.DARK_RED + killer.getName() + " is dominating you");
                        }

                        foundKilled = true;
                        break;
                    }
                }

                if (!foundKilled) { // If the killed player is not already in the map
                    Map<Player, Integer> newKillEntry = new HashMap<>();
                    newKillEntry.put(killed, 1);
                    list.add(newKillEntry);
                }

                foundKiller = true;
                break;
            }
        }

        if (!foundKiller) { // If the killer does not have a map yet
            Map<Player, Integer> newKillEntry = new HashMap<>();
            newKillEntry.put(killed, 1);
            List<Map<Player, Integer>> newList = new ArrayList<>();
            newList.add(newKillEntry);
            Map<Player, List<Map<Player, Integer>>> newMap = new HashMap<>();
            newMap.put(killer, newList);
            dominationKills.add(newMap);
        }
    }
}