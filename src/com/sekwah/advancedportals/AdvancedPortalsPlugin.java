package com.sekwah.advancedportals;

import com.sekwah.advancedportals.DataCollector.DataCollector;
import com.sekwah.advancedportals.compat.bukkit.NMS;
import com.sekwah.advancedportals.destinations.Destination;
import com.sekwah.advancedportals.effects.WarpEffects;
import com.sekwah.advancedportals.listeners.BungeeListener;
import com.sekwah.advancedportals.listeners.FlowStopper;
import com.sekwah.advancedportals.listeners.PortalPlacer;
import com.sekwah.advancedportals.listeners.PortalProtect;
import com.sekwah.advancedportals.metrics.Metrics;
import com.sekwah.advancedportals.portals.Portal;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class AdvancedPortalsPlugin extends JavaPlugin {

    public NMS nmsAccess;

    public boolean useCustomPrefix = false;

    public String customPrefix = "\u00A7a[\u00A7eAdvancedPortals\u00A7a]";

    public String customPrefixFail = "\u00A7c[\u00A77AdvancedPortals\u00A7c]";

    public void onEnable() {

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }

        String packageName = getServer().getClass().getPackage().getName();
        String[] packageSplit = packageName.split("\\.");
        String version = packageSplit[packageSplit.length - 1];

        try {
            Class<?> nmsClass = Class.forName("com.sekwah.advancedportals.compat.bukkit." + version);
            if (NMS.class.isAssignableFrom(nmsClass)) {
                this.nmsAccess = (NMS) nmsClass.getConstructor().newInstance();


                ConfigAccessor portalConfig = new ConfigAccessor(this, "portals.yml");
                portalConfig.saveDefaultConfig();

                ConfigAccessor destinationConfig = new ConfigAccessor(this, "destinations.yml");
                destinationConfig.saveDefaultConfig();

                new Assets(this);

                // Opens a channel that messages bungeeCord
                this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

                // Loads the portal and destination editors
                new Portal(this);
                new Destination(this);

                new DataCollector(this);

                // These register the commands
                new AdvancedPortalsCommand(this);
                new DestinationCommand(this);
                new WarpCommand(this);

                new WarpEffects(this);


                // These register the listeners
                new Listeners(this);

                new FlowStopper(this);
                new PortalProtect(this);
                new PortalPlacer(this);

                Selection.LoadData(this);

                DataCollector.setupMetrics();

                this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
                this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeListener(this));

                this.getServer().getConsoleSender().sendMessage("\u00A7aAdvanced portals have been successfully enabled!");


            } else {
                this.getLogger().warning("Something went wrong, please notify the author and tell them this version v:" + version);
                this.setEnabled(false);
            }
        } catch (ClassNotFoundException e) {
            this.getLogger().warning("This version of craftbukkit is not yet supported, please notify the author and give version v:" + version);
            this.setEnabled(false);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        saveDefaultConfig();

        // thanks to the new config accessor code the config.saveDefaultConfig(); will now
        //  only copy the file if it doesnt exist!
        ConfigAccessor config = new ConfigAccessor(this, "config.yml");

        this.useCustomPrefix = config.getConfig().getBoolean("UseCustomPrefix");
        if (useCustomPrefix) {
            this.customPrefix = config.getConfig().getString("CustomPrefix").replaceAll("&", "\u00A7");
            this.customPrefixFail = config.getConfig().getString("CustomPrefixFail").replaceAll("&", "\u00A7");
        }

    }


    public void onDisable() {
        this.getServer().getConsoleSender().sendMessage("\u00A7cAdvanced portals are being disabled!");
    }


}
