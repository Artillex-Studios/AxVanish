package com.artillexstudios.axvanish.api.group.capabilities;

import com.artillexstudios.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axapi.packet.PacketEvent;
import com.artillexstudios.axapi.packet.PacketEvents;
import com.artillexstudios.axapi.packet.PacketListener;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundSoundWrapper;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axapi.utils.position.ImmutableBlockPosition;
import com.artillexstudios.axvanish.api.AxVanishAPI;
import com.artillexstudios.axvanish.api.group.packet.ClientboundBlockEventWrapper;
import com.artillexstudios.axvanish.api.users.User;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SilentOpenCapability extends VanishCapability implements Listener {
    private static final Set<ImmutableBlockPosition> silentViewers = ConcurrentHashMap.newKeySet();

    public SilentOpenCapability(Map<String, Object> config) {
        super(config);
        registerPacketListener();
    }

    private void registerPacketListener() {
        PacketEvents.INSTANCE.addListener(new PacketListener() {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.type() == ClientboundPacketTypes.BLOCK_EVENT) {
                    ClientboundBlockEventWrapper wrapper = new ClientboundBlockEventWrapper(event);
                    if (!silentViewers.contains(wrapper.getPosition().immutable())) {
                        return;
                    }

                    event.cancelled(true);
                } else if (event.type() == ClientboundPacketTypes.SOUND) {
                    ClientboundSoundWrapper wrapper = new ClientboundSoundWrapper(event);
                    ImmutableBlockPosition pos = new ImmutableBlockPosition(wrapper.getX(), wrapper.getY(), wrapper.getZ());
                    LogUtils.debug("Position: {}", pos);
                    if (!silentViewers.contains(pos)) {
                        return;
                    }

                    event.cancelled(true);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
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

        if (!(block.getState() instanceof Container)) {
            return;
        }

        event.setCancelled(true);
        silentViewers.add(new ImmutableBlockPosition(block.getX(), block.getY(), block.getZ()));

        Scheduler.get().runAt(block.getLocation(), () -> {
            if (block.getState() instanceof Container container) {
                player.openInventory(container.getInventory());
            }

            Scheduler.get().runLaterAt(block.getLocation(), () -> {
                silentViewers.remove(new ImmutableBlockPosition(block.getX(), block.getY(), block.getZ()));
            }, 10L);
        });
    }
}
