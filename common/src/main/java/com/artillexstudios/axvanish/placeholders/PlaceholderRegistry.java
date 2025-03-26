package com.artillexstudios.axvanish.placeholders;

import com.artillexstudios.axapi.placeholders.ParseContext;
import com.artillexstudios.axapi.placeholders.Placeholders;
import com.artillexstudios.axvanish.api.AxVanishAPI;
import com.artillexstudios.axvanish.api.users.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public enum PlaceholderRegistry {
    INSTANCE;

    public void register() {
        Placeholders.registerTransformer(OfflinePlayer.class, User.class, player -> AxVanishAPI.instance().getUserIfLoadedImmediately(player));

        Placeholders.register("online", ctx -> Integer.toString(Bukkit.getOnlinePlayers().size() - AxVanishAPI.instance().vanished().size()), ParseContext.PLACEHOLDER_API);

        Placeholders.register("vanished", ctx -> Integer.toString(AxVanishAPI.instance().vanished().size()), ParseContext.PLACEHOLDER_API);

        Placeholders.register("vanished_players", ctx -> String.join(", ", AxVanishAPI.instance().vanished()
                .stream()
                .map(user -> user.onlinePlayer().getName())
                .toList()
        ), ParseContext.PLACEHOLDER_API);

        Placeholders.register("state", ctx -> {
            User user = ctx.resolve(User.class);
            return Boolean.toString(user.vanished());
        }, ParseContext.PLACEHOLDER_API);
    }
}
