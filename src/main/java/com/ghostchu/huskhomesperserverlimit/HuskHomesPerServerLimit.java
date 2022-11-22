package com.ghostchu.huskhomesperserverlimit;


import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.event.HomeSaveEvent;
import net.william278.huskhomes.player.BukkitPlayer;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Server;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class HuskHomesPerServerLimit extends JavaPlugin implements Listener {
    private Server server;
    private HuskHomesAPI huskHomesAPI;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        reloadConfig();
        Plugin huskHomesInitInstance = Bukkit.getPluginManager().getPlugin("HuskHomes");
        if (huskHomesInitInstance == null) {
            getLogger().severe("HuskHomes didn't installed on this server, " +
                    "please check the installation of HuskHomes! Plugin disabling...");
        }
        if (huskHomesInitInstance instanceof HuskHomesAPI) {
            huskHomesAPI = (HuskHomesAPI) huskHomesInitInstance;
        } else {
            getLogger().severe("HuskHomes mismatched with excepted, " +
                    "please check the versions both HuskHomes and HuskHomesPerServerLimit! Plugin disabling...");
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.server = huskHomesAPI.getServer();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler(ignoreCancelled = true)
    public void onHomeSet(HomeSaveEvent event) {
        Player player = Bukkit.getPlayer(event.getHome().owner.uuid);
        if (player == null) {
            // Player not exists or not online on this server
            return;
        }
        try {
            List<Home> homesGlobal = huskHomesAPI.getUserHomes(BukkitPlayer.adapt(player)).get();

            List<Home> homesLocal = homesGlobal.stream().filter(home -> home.server.equals(this.server)).collect(Collectors.toList());
            int ownsOnThisServer = homesLocal.size();
            // 区分大小写
            boolean sameHomeOnLocalAlreadyExists = homesLocal.stream().anyMatch(home -> home.uuid.equals(event.getHome().uuid));
            if (!sameHomeOnLocalAlreadyExists) {
                ownsOnThisServer++;
            }
            int limitLocal = getConfig().getInt("slots");
            if (ownsOnThisServer > limitLocal && !player.hasPermission("huskhomesperserverlimit.bypass")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.out-of-slot")).replace("{slots}", String.valueOf(limitLocal)));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
