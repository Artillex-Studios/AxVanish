package com.artillexstudios.axvanish.users;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvanish.api.LoadContext;
import com.artillexstudios.axvanish.api.users.User;
import com.artillexstudios.axvanish.database.DataHandler;
import com.artillexstudios.axvanish.exception.UserAlreadyLoadedException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public final class Users {
    private static final ConcurrentHashMap<UUID, User> loadedUsers = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<User> unsaved = new ConcurrentLinkedQueue<>();
    private static final Cache<@NonNull UUID, User> tempUsers = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(200)
            .build();

    public static User getUserIfLoadedImmediately(UUID uuid) {
        User user = loadedUsers.get(uuid);
        if (user != null) {
            return user;
        }

        user = tempUsers.getIfPresent(uuid);
        if (user != null) {
            return user;
        }

        Optional<User> unsavedUser = unsaved.stream().filter(u -> u.player().getUniqueId().equals(uuid)).findAny();
        if (unsavedUser.isPresent()) {
            user = unsavedUser.get();
            tempUsers.put(uuid, user);
        }

        return user;
    }

    public static CompletableFuture<User> loadUser(UUID uuid) throws UserAlreadyLoadedException {
        if (loadedUsers.containsKey(uuid)) {
            loadedUsers.compute(uuid, (k, loaded) -> loaded);
            LogUtils.warn("User was already loaded, falling back to already created user instance!");
            throw new UserAlreadyLoadedException();
        }

        User user = tempUsers.getIfPresent(uuid);
        if (user != null) {
            tempUsers.invalidate(uuid);
            loadedUsers.put(uuid, user);
            return CompletableFuture.completedFuture(user);
        }

        return getUser(uuid, LoadContext.FULL);
    }

    public static void loadWithContext(User user, LoadContext context) {
        switch (context) {
            case FULL -> loadedUsers.put(user.player().getUniqueId(), user);
            case TEMPORARY -> tempUsers.put(user.player().getUniqueId(), user);
        }
    }

    public static CompletableFuture<User> getUser(UUID uuid, LoadContext loadContext) {
        User user = getUserIfLoadedImmediately(uuid);
        if (user != null) {
            return CompletableFuture.completedFuture(user);
        }

        return DataHandler.loadUser(uuid, loadContext);
    }

    public static List<User> online() {
        return new ObjectArrayList<>(loadedUsers.values());
    }

    public static List<User> vanished() {
        ObjectArrayList<User> vanished = new ObjectArrayList<>();
        for (User user : loadedUsers.values()) {
            if (user.onlinePlayer() != null && user.vanished()) {
                vanished.add(user);
            }
        }

        return vanished;
    }

    public static User disconnect(UUID uuid) {
        User user = loadedUsers.remove(uuid);

        if (user != null) {
            tempUsers.put(uuid, user);
        } else {
            unsaved.removeIf(u -> u.player().getUniqueId().equals(uuid));
        }

        return user;
    }

    public static void markUnsaved(User user) {
        if (unsaved.contains(user)) {
            return;
        }

        unsaved.add(user);
    }

    public static ObjectArrayList<User> unsaved() {
        ObjectArrayList<User> users = new ObjectArrayList<>(unsaved);
        unsaved.clear();
        return users;
    }
}
