package com.artillexstudios.axvanish.api.group.capabilities;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axvanish.api.AxVanishAPI;
import com.artillexstudios.axvanish.api.users.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SilentOpenCapability extends VanishCapability implements Listener {
    private static final Map<UUID, Container> trackedInventories = new ConcurrentHashMap<>();

    public SilentOpenCapability(Map<String, Object> config) {
        super(config);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        User user = AxVanishAPI.instance().userOrThrow(player);
        if (!user.hasCapability(VanishCapabilities.SILENT_OPEN)) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null
                || block.getType() == Material.BLAST_FURNACE
                || block.getType() == Material.BREWING_STAND
                || block.getType() == Material.FURNACE
                || block.getType() == Material.HOPPER
                || block.getType() == Material.SMOKER) {
            return;
        }

        event.setCancelled(true);
        Scheduler.get().runAt(block.getLocation(), () -> {
            if (!(block.getState() instanceof Container container)) {
                return;
            }

            Inventory original = container.getInventory();
            String title = container.getCustomName() != null ? container.getCustomName() : original.getType().getDefaultTitle();

            Inventory fakeInventory = Bukkit.createInventory(null, original.getSize(), title);
            fakeInventory.setContents(original.getContents());

            trackedInventories.put(player.getUniqueId(), container);
            player.openInventory(fakeInventory);
        });
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Container container = trackedInventories.remove(uuid);
        if (container == null) {
            return;
        }

        Inventory source = container.getInventory();
        Inventory view = event.getView().getTopInventory();
        ItemStack[] newContents = view.getContents();

        for (int i = 0; i < source.getSize(); i++) {
            source.setItem(i, i < newContents.length ? newContents[i] : null);
        }
    }
}
