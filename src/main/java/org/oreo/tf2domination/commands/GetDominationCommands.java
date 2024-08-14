package org.oreo.tf2domination.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.oreo.tf2domination.Tf2Domination;
import org.oreo.tf2domination.listeners.PlayerDeathListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetDominationCommands implements CommandExecutor, TabCompleter {

    private final Tf2Domination plugin;

    public GetDominationCommands(Tf2Domination plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Please specify a subcommand.");
            return false;
        }

        if (args[0].equalsIgnoreCase("dominating")) {
            return handleDominating(sender, args);
        }

        if (args[0].equalsIgnoreCase("dominatedBy")) {
            return handleDominatedBy(sender, args);
        }

        if (args[0].equalsIgnoreCase("top")) {
            return handleTop(sender);
        }

        sender.sendMessage("Unknown subcommand.");
        return false;
    }

    private boolean handleDominating(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("Usage: /domination dominating <player name>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return false;
        }

        int dominations = 0;
        sender.sendMessage(ChatColor.DARK_AQUA + "-----" + target.getName() + " is dominating-----");

        int killsUntilDomination = plugin.getConfig().getInt("kills-until-domination");

        for (Map<Player, List<Map<Player, Integer>>> map : PlayerDeathListener.dominationKills) {
            if (map.containsKey(target)) {
                for (Map<Player, Integer> map1 : map.get(target)) {
                    for (Map.Entry<Player, Integer> entry : map1.entrySet()) {
                        int kills = entry.getValue();
                        if (kills >= killsUntilDomination) {
                            sender.sendMessage(ChatColor.DARK_AQUA + entry.getKey().getName() + " - " + kills + " kills");
                            dominations++;
                        }
                    }
                }
            }
        }

        if (dominations <= 0) {
            sender.sendMessage(ChatColor.DARK_AQUA + "This player isn't dominating anyone");
        }

        return true;
    }

    private boolean handleDominatedBy(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("Usage: /domination dominatedBy <player name>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return false;
        }

        int dominatedByCount = 0;
        sender.sendMessage(ChatColor.DARK_AQUA + "-----" + target.getName() + " is dominated by -----");

        int killsUntilDomination = plugin.getConfig().getInt("kills-until-domination");

        for (Map<Player, List<Map<Player, Integer>>> map : PlayerDeathListener.dominationKills) {
            for (Map.Entry<Player, List<Map<Player, Integer>>> entry : map.entrySet()) {
                Player player = entry.getKey();
                List<Map<Player, Integer>> dominationList = entry.getValue();

                for (Map<Player, Integer> dominationMap : dominationList) {
                    if (dominationMap.containsKey(target)) {
                        int kills = dominationMap.get(target);
                        if (kills >= killsUntilDomination) {
                            sender.sendMessage(ChatColor.DARK_AQUA + player.getName() + " - " + "killed " + kills + " times");
                            dominatedByCount++;
                        }
                    }
                }
            }
        }

        if (dominatedByCount <= 0) {
            sender.sendMessage(ChatColor.DARK_AQUA + "This player isn't dominated by anyone");
        }

        return true;
    }

    private boolean handleTop(CommandSender sender) {
        Map<Player, Integer> dominationCounts = new HashMap<>();
        int killsUntilDomination = plugin.getConfig().getInt("kills-until-domination");

        for (Map<Player, List<Map<Player, Integer>>> map : PlayerDeathListener.dominationKills) {
            for (Map.Entry<Player, List<Map<Player, Integer>>> entry : map.entrySet()) {
                Player dominator = entry.getKey();
                int totalDominations = 0;

                for (Map<Player, Integer> dominationMap : entry.getValue()) {
                    for (Integer kills : dominationMap.values()) {
                        if (kills >= killsUntilDomination) {
                            totalDominations++;
                        }
                    }
                }

                if (totalDominations > 0) {
                    dominationCounts.put(dominator, totalDominations);
                }
            }
        }

        List<Map.Entry<Player, Integer>> sortedDominations = new ArrayList<>(dominationCounts.entrySet());
        sortedDominations.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        sender.sendMessage(ChatColor.DARK_AQUA + "----- Top 6 Players with Most Dominations -----");
        for (int i = 0; i < Math.min(6, sortedDominations.size()); i++) {
            Map.Entry<Player, Integer> entry = sortedDominations.get(i);
            sender.sendMessage(ChatColor.DARK_AQUA + ((i + 1) + ". " + entry.getKey().getName() + " - " + entry.getValue() + " dominations"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("dominating");
            completions.add("dominatedBy");
            completions.add("top");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("dominating") || args[0].equalsIgnoreCase("dominatedBy"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }

        return completions;
    }
}
