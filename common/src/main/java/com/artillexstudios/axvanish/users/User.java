package com.artillexstudios.axvanish.users;

import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.utils.Cooldown;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axvanish.AxVanishPlugin;
import com.artillexstudios.axvanish.api.context.VanishContext;
import com.artillexstudios.axvanish.api.context.source.ForceVanishSource;
import com.artillexstudios.axvanish.api.event.UserPreVanishStateChangeEvent;
import com.artillexstudios.axvanish.api.event.UserVanishStateChangeEvent;
import com.artillexstudios.axvanish.api.group.Group;
import com.artillexstudios.axvanish.config.Config;
import com.artillexstudios.axvanish.config.Language;
import com.artillexstudios.axvanish.database.DataHandler;
import com.artillexstudios.axvanish.utils.VanishStateManager;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class User implements com.artillexstudios.axvanish.api.users.User {
    private static final Cooldown<User> cancelCooldown = new Cooldown<>();
    private final OfflinePlayer offlinePlayer;
    private Group group;
    private Player onlinePlayer;
    private boolean vanished;
    private final VanishStateManager stateManager;

    public User(OfflinePlayer player, Player onlinePlayer, Group group, boolean vanished) {
        this.onlinePlayer = onlinePlayer;
        this.offlinePlayer = player;
        this.group = group;
        this.vanished = vanished;
        this.stateManager = AxVanishPlugin.instance().stateManagerFactory().create(this);
    }

    public void group(Group group) {
        this.group = group;
    }

    public void onlinePlayer(Player onlinePlayer) {
        this.onlinePlayer = onlinePlayer;
    }

    @Override
    public Player onlinePlayer() {
        return this.onlinePlayer;
    }

    @Override
    public OfflinePlayer player() {
        return this.offlinePlayer;
    }

    @Override
    public Group group() {
        return this.group;
    }

    @Override
    public boolean update(boolean vanished, VanishContext context) {
        boolean prev = this.vanished;
        UserPreVanishStateChangeEvent event = new UserPreVanishStateChangeEvent(this, this.vanished, vanished, context);
        if (event.call() || context.getSource(ForceVanishSource.class) != null) {
            if (vanished) {
                this.vanished = true;
            }

            new UserVanishStateChangeEvent(this, prev, vanished, context).call();
            if (!vanished) {
                this.vanished = false;
            }

            this.stateManager.updateViewers(this.vanished);
            if (this.vanished != prev) {
                DataHandler.save(this);
            }
            return true;
        }

        this.stateManager.updateViewers(this.vanished);
        return false;
    }

    @Override
    public boolean vanished() {
        return this.vanished;
    }

    @Override
    public boolean canSee(com.artillexstudios.axvanish.api.users.User user) {
        if (this.group() == null) {
            if (Config.debug) {
                LogUtils.debug("Can see, because the group is null and vanished: {}", user.vanished());
            }
            return !user.vanished();
        }

        if (user.group() == null) {
            return !user.vanished();
        }

        if (this.group().priority() >= user.group().priority()) {
            return true;
        }

        return !user.vanished();
    }

    @Override
    public void message(Component message) {
        Player player = this.onlinePlayer();
        if (player == null) {
            return;
        }

        NMSHandlers.getNmsHandler().sendMessage(player, message);
    }

    @Override
    public void cancelMessage() {
        Player player = this.onlinePlayer();
        if (player == null) {
            return;
        }

        if (cancelCooldown.hasCooldown(this)) {
            return;
        }

        MessageUtils.sendMessage(player, Language.prefix, Language.error.vanished);
        cancelCooldown.addCooldown(this, Config.messageCooldown);
    }
}
