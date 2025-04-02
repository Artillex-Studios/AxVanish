package com.artillexstudios.axvanish.api.group.capabilities;

import com.artillexstudios.axapi.utils.RandomStringGenerator;
import com.artillexstudios.axvanish.api.AxVanishAPI;
import com.artillexstudios.axvanish.api.users.User;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PreventCapability extends VanishCapability implements Listener {
    private final List<String> prevented;

    public PreventCapability(List<String> prevented) {
        super(null);
        List<String> transformed = new ArrayList<>();
        if (prevented != null) {
            for (String s : prevented) {
                transformed.add(s.toLowerCase(Locale.ENGLISH));
            }
        }

        this.prevented = prevented == null ? null : transformed;
    }

    public PreventCapability(Map<String, Object> config) {
        this((List<String>) config.get("actions"));
    }

    public boolean prevents(String action) {
        return prevented.contains(action);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        User user = AxVanishAPI.instance().getUserIfLoadedImmediately(event.getPlayer());
        if (user == null) {
            return;
        }

        PreventCapability capability = user.capability(VanishCapabilities.PREVENT);
        if (capability == null) {
            return;
        }

        if (capability.prevents("block_break")) {
            event.setCancelled(true);
            user.cancelMessage();
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        User user = AxVanishAPI.instance().getUserIfLoadedImmediately(event.getPlayer());
        if (user == null) {
            return;
        }

        PreventCapability capability = user.capability(VanishCapabilities.PREVENT);
        if (capability == null) {
            return;
        }

        if (capability.prevents("block_place")) {
            event.setCancelled(true);
            user.cancelMessage();
        }
    }

    @EventHandler
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        User user = AxVanishAPI.instance().getUserIfLoadedImmediately(player);
        if (user == null) {
            return;
        }

        PreventCapability capability = user.capability(VanishCapabilities.PREVENT);
        if (capability == null) {
            return;
        }

        if (capability.prevents("block_change")) {
            event.setCancelled(true);
            user.cancelMessage();
        }
    }

    @EventHandler
    public void onEntityTargetEvent(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) {
            return;
        }
        User user = AxVanishAPI.instance().getUserIfLoadedImmediately(player);
        if (user == null) {
            return;
        }

        PreventCapability capability = user.capability(VanishCapabilities.PREVENT);
        if (capability == null) {
            return;
        }

        if (capability.prevents("entity_target")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        User user = AxVanishAPI.instance().getUserIfLoadedImmediately(player);
        if (user == null) {
            return;
        }

        PreventCapability capability = user.capability(VanishCapabilities.PREVENT);
        if (capability == null) {
            return;
        }

        if (capability.prevents("damage")) {
            event.setCancelled(true);
            user.cancelMessage();
        }
    }

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        User user = AxVanishAPI.instance().getUserIfLoadedImmediately(player);
        if (user == null) {
            return;
        }

        PreventCapability capability = user.capability(VanishCapabilities.PREVENT);
        if (capability == null) {
            return;
        }

        if (capability.prevents("pickup")) {
            event.setCancelled(true);
            user.cancelMessage();
        }
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        User user = AxVanishAPI.instance().getUserIfLoadedImmediately(player);
        if (user == null) {
            return;
        }

        PreventCapability capability = user.capability(VanishCapabilities.PREVENT);
        if (capability == null) {
            return;
        }

        if (capability.prevents("hunger")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        User user = AxVanishAPI.instance().getUserIfLoadedImmediately(event.getPlayer());
        if (user == null) {
            return;
        }

        PreventCapability capability = user.capability(VanishCapabilities.PREVENT);
        if (capability == null) {
            return;
        }

        if (capability.prevents("item_drop")) {
            event.setCancelled(true);
            user.cancelMessage();
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block != null && block.getState() instanceof Container) {
            return;
        }

        User user = AxVanishAPI.instance().getUserIfLoadedImmediately(event.getPlayer());
        if (user == null) {
            return;
        }

        PreventCapability capability = user.capability(VanishCapabilities.PREVENT);
        if (capability == null) {
            return;
        }

        if (capability.prevents("interact")) {
            event.setCancelled(true);
            user.cancelMessage();
        }
    }

    @EventHandler
    public void onTabCompleteEvent(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player)) {
            return;
        }

        User completer = AxVanishAPI.instance().getUserIfLoadedImmediately(player);
        if (completer == null) {
            return;
        }

        // TODO: User AsyncTabCompleteEvent on papers -> maybe need an api to register events based on paper/spigot
        List<String> vanishedPlayers = AxVanishAPI.instance().vanished()
                .stream()
                .filter(user -> {
                    PreventCapability capability = user.capability(VanishCapabilities.PREVENT);
                    if (capability != null && capability.prevents("tab_complete")) {
                        return user.onlinePlayer() != null && !completer.canSee(user);
                    }

                    return false;
                })
                .map(user -> user.onlinePlayer().getName())
                .toList();

        Iterator<String> completionIterator = event.getCompletions().iterator();

        while (completionIterator.hasNext()) {
            String next = completionIterator.next();
            if (vanishedPlayers.contains(next)) {
                completionIterator.remove();
            }
        }
    }


    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        User executor = AxVanishAPI.instance().getUserIfLoadedImmediately(event.getPlayer());
        if (executor == null) {
            return;
        }

        List<String> vanishedPlayers = AxVanishAPI.instance().vanished()
                .stream()
                .filter(user -> {
                    PreventCapability capability = user.capability(VanishCapabilities.PREVENT);
                    if (capability != null && capability.prevents("command_use")) {
                        return user.onlinePlayer() != null && !executor.canSee(user);
                    }

                    return false;
                })
                .map(user -> user.onlinePlayer().getName())
                .toList();

        for (String vanishedPlayer : vanishedPlayers) {
            event.setMessage(event.getMessage().replace(vanishedPlayer, RandomStringGenerator.lowercase().generate(5)));
        }
    }
}
