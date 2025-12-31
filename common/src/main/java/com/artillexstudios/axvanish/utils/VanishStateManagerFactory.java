package com.artillexstudios.axvanish.utils;

import com.artillexstudios.axvanish.api.users.User;
import org.bukkit.plugin.java.JavaPlugin;

public final class VanishStateManagerFactory {

    private final JavaPlugin plugin;

    public VanishStateManagerFactory(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public VanishStateManager create(User user) {
        return new VanishStateManager(this.plugin, user);
    }
}
