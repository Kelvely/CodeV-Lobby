package net.codevmc.lobby;

import io.hekate.cluster.ClusterNode;
import io.hekate.core.HekateFutureException;
import net.codevmc.lobby.hekate.ClusterManager;
import net.codevmc.util.PlayerDataUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public final class Lobby extends JavaPlugin {

    private ClusterManager clusterManager;

    private static Lobby instance;

    private boolean isBukkitInitialized = false;

    private PlayerDataUtils playerDataUtils;

    public PlayerDataUtils getPlayerDataUtils() {
        return playerDataUtils;
    }

    public static Lobby getInstance() {
        return instance;
    }

    public boolean isBukkitInitialized() {
        return isBukkitInitialized;
    }

    @Override
    public void onEnable() {
        if(!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
        FileConfiguration configuration = getConfig();



        try {
            playerDataUtils = new PlayerDataUtils(getServer());
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            getLogger().log(Level.SEVERE, "Cannot find the player data folder! This plugin is designed for paper 1.13.2!", ex);
            getServer().shutdown();
        }

        // TODO Launch database player loading service

        String clusterName = configuration.getString("cluster.clusterName");
        String nodeName = configuration.getString("cluster.nodeName");
        clusterManager = new ClusterManager(clusterName, nodeName, this);
        getLogger().log(Level.INFO, "Joining cluster "+ clusterName +"...");
        try {
            clusterManager.instantiate();
        } catch (HekateFutureException ex) {
            getLogger().log(Level.SEVERE, "Unable to join the cluster topology "+ clusterName +"!");
            getServer().shutdown();
            return;
        }

        //List<ClusterNode> clusterNodeList = clusterManager.getHekate().cluster().topology().nodes();
        getLogger().log(Level.INFO, "Joined cluster "+ clusterName +", which has " + clusterManager.getHekate().cluster().topology().size() + " node(s).");
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> isBukkitInitialized = true);
        instance = this;
    }

    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
