package ch.hekates.koalicraft;

import ch.hekates.koalicraft.listeners.SpawnElytraListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Koalicraft extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new SpawnElytraListener(this), this);

    }

}
