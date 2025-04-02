package com.artillexstudios.axvanish.api.group.capabilities;

import com.artillexstudios.axvanish.api.AxVanishAPI;
import com.artillexstudios.axvanish.api.users.User;
import com.artillexstudios.axapi.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SilentOpenCapability extends VanishCapability implements Listener {
    private static final Map<UUID, Inventory> inventories = new HashMap<>();
    private static final Map<UUID, Location> locations = new HashMap<>();

    public SilentOpenCapability(Map<String, Object> config) {
        super(config);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        User user = AxVanishAPI.instance().userOrThrow(player);
        if (!user.hasCapability(VanishCapabilities.SILENT_OPEN)) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        event.setCancelled(true);
        Location location = clickedBlock.getLocation();

        Scheduler.get().runAt(location, task -> {
            Block block = location.getBlock();

            if (!(block.getState() instanceof Container container)) {
                return;
            }

            Inventory original = container.getInventory();
            InventoryType type = original.getType();
            String title = container.getCustomName() == null ? type.getDefaultTitle() : container.getCustomName();

            Inventory copy;
            if (type == InventoryType.BARREL || type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST) {
                copy = Bukkit.createInventory(null, original.getSize(), title);
            } else {
                copy = Bukkit.createInventory(null, type, title);
            }

            copy.setContents(original.getContents());
            inventories.put(player.getUniqueId(), original);
            locations.put(player.getUniqueId(), location);
            player.openInventory(copy);
        });
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        Inventory original = inventories.remove(playerId);
        Location location = locations.remove(playerId);
        if (original == null || location == null) return;

        Scheduler.get().runAt(location, task ->
                original.setContents(event.getView().getTopInventory().getContents())
        );
    }
}
