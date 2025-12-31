package com.artillexstudios.axvanish.utils;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axapi.utils.mutable.MutableInteger;
import com.artillexstudios.axvanish.api.AxVanishAPI;
import com.artillexstudios.axvanish.api.users.User;
import com.artillexstudios.axvanish.config.Config;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public final class VanishStateManager {

    private final JavaPlugin plugin;
    private final User user;
    private final CountDownLatch latch = new CountDownLatch(1);

    public VanishStateManager(JavaPlugin plugin, User user) {
        this.plugin = plugin;
        this.user = user;
        this.latch.countDown();
    }

    public void updateViewers(boolean current) {
        try {
            this.latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Player player = this.user.onlinePlayer();
        if (player == null) {
            return;
        }

        if (Config.debug) {
            LogUtils.debug("Called updateViewers for user {}, current: {}", player.getName(), current);
        }

        if (current ^ !player.isVisibleByDefault()) {
            if (Config.debug) {
                LogUtils.debug("Vanish state changed!");
            }
            if (current) {
                player.setMetadata("vanished", new FixedMetadataValue(this.plugin, true));
            } else {
                player.removeMetadata("vanished", this.plugin);
            }
            player.setVisibleByDefault(!current);
        }

        List<User> onlineUsers = AxVanishAPI.instance().online();
        MutableInteger playerCounter = new MutableInteger(onlineUsers.size());
        AtomicInteger counter = new AtomicInteger();
        for (User online : onlineUsers) {
            Player onlinePlayer = online.onlinePlayer();
            if (onlinePlayer == null) {
                playerCounter.set(playerCounter.intValue() - 1);
                continue;
            }

            Runnable retired = () -> this.count(playerCounter, counter);

            // We want to be sure that only people who can see the player
            // can see the player. This is not a mistake
            if (Config.debug) {
                LogUtils.debug("Can {} see {}: {}", onlinePlayer.getName(), player.getName(), online.canSee(this.user));
            }
            if (online.canSee(this.user)) {
                Scheduler.get().run(onlinePlayer, task -> {
                    onlinePlayer.showPlayer(this.plugin, player);
                    this.count(playerCounter, counter);
                }, retired);
            } else {
                Scheduler.get().run(onlinePlayer, task -> {
                    onlinePlayer.hidePlayer(this.plugin, player);
                    this.count(playerCounter, counter);
                }, retired);
            }

            if (Config.debug) {
                LogUtils.debug("Can {} see {}: {}", player.getName(), onlinePlayer.getName(), this.user.canSee(online));
            }
            if (this.user.canSee(online)) {
                Scheduler.get().run(onlinePlayer, task -> {
                    player.showPlayer(this.plugin, onlinePlayer);
                    this.count(playerCounter, counter);
                }, retired);
            } else {
                Scheduler.get().run(onlinePlayer, task -> {
                    player.hideEntity(this.plugin, onlinePlayer);
                    this.count(playerCounter, counter);
                }, retired);
            }
        }
    }

    private void count(MutableInteger mutableInteger, AtomicInteger integer) {
        if (integer.incrementAndGet() < mutableInteger.intValue()) {
            return;
        }

        this.latch.countDown();
    }
}
