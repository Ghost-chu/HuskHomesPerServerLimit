package com.ghostchu.huskhomesperserverlimit;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.api.HuskHomesAPI;
import me.william278.huskhomes2.api.events.PlayerSetHomeEvent;
import me.william278.huskhomes2.teleport.points.Home;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;

public final class HuskHomesPerServerLimit extends JavaPlugin implements Listener {
    private String serverName;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.serverName = HuskHomes.getSettings().getServerID();
        HuskHomes huskHomes = (HuskHomes) Bukkit.getPluginManager().getPlugin("HuskHomes");
        if (huskHomes != null) {
            if (!huskHomes.isEnabled()) {
                getLogger().warning("HuskHomes is not enabled, disabling...");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.serverName = HuskHomes.getSettings().getServerID();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler(ignoreCancelled = true)
    public void onHomeSet(PlayerSetHomeEvent event) {
        Player player = event.getPlayer();
        List<Home> playerHomes = HuskHomesAPI.getInstance().getHomes(player);
        List<Home> playerHomesLocal = playerHomes.stream().filter(home -> home.getServer().equals(serverName)).collect(Collectors.toList());
        int ownsOnThisServer = playerHomesLocal.size();
        // 区分大小写
        boolean sameHomeNameOnLocalAlreadyExists = playerHomesLocal.stream().anyMatch(home->home.getName().equals(event.getHome().getName()));
        if (!sameHomeNameOnLocalAlreadyExists)
            ownsOnThisServer++;
        int limitLocal = getConfig().getInt("slots");
        if (ownsOnThisServer > limitLocal && !player.hasPermission("huskhomesperserverlimit.bypass")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.out-of-slot")).replace("{slots}",String.valueOf(limitLocal)));
        }
    }
}
