package org.oreo.tf2domination;

import org.bukkit.plugin.java.JavaPlugin;
import org.oreo.tf2domination.commands.GetDominationCommands;
import org.oreo.tf2domination.listeners.PlayerDeathListener;

public final class Tf2Domination extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);

        getCommand("domination").setExecutor(new GetDominationCommands(this));
    }
}
