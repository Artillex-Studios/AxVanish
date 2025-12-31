package com.artillexstudios.axvanish.database;

import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvanish.api.LoadContext;
import com.artillexstudios.axvanish.config.Config;
import com.artillexstudios.axvanish.users.User;
import com.artillexstudios.axvanish.users.Users;
import org.bukkit.Bukkit;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public final class DataHandler {
    private static final Table<Record> users = DSL.table("axvanish_users");
    private static final Field<Integer> id = DSL.field("id", int.class);
    private static final Field<UUID> uuid = DSL.field("uuid", UUID.class);
    private static final Field<Boolean> vanished = DSL.field("vanished", boolean.class);
    private static final Executor ASYNC_EXECUTOR = AsyncUtils::submit;

    public static CompletionStage<Void> setup() {
        ArrayList<CompletableFuture<Integer>> futures = new ArrayList<>();

        CompletionStage<Integer> teams = DatabaseConnector.getInstance().context().createTableIfNotExists(users)
                .column(id, SQLDataType.INTEGER.identity(true))
                .column(uuid, SQLDataType.UUID)
                .column(vanished, SQLDataType.BOOLEAN)
                .primaryKey(id)
                .executeAsync()
                .exceptionallyAsync(throwable -> {
                    LogUtils.error("An unexpected error occurred while running users table creation query!", throwable);
                    return 0;
                });

        futures.add(teams.toCompletableFuture());

        if (Config.database.type == DatabaseType.SQLITE) {
            CompletableFuture<Integer> pragma = new CompletableFuture<>();
            AsyncUtils.submit(() -> {
                DatabaseConnector.getInstance().context().fetch("PRAGMA journal_mode=WAL;");
                DatabaseConnector.getInstance().context().execute("PRAGMA synchronous = off;");
                DatabaseConnector.getInstance().context().execute("PRAGMA page_size = 32768;");
                DatabaseConnector.getInstance().context().fetch("PRAGMA mmap_size = 30000000000;");
                pragma.complete(1);
            });
            futures.add(pragma);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public static CompletableFuture<com.artillexstudios.axvanish.api.users.User> loadUser(UUID uuid, LoadContext context) {
        return CompletableFuture.supplyAsync(() -> {
            Result<Record> select = DatabaseConnector.getInstance().context()
                    .select()
                    .from(users)
                    .where(DataHandler.uuid.eq(uuid))
                    .limit(1)
                    .fetch();

            if (!select.isEmpty()) {
                Record record = select.getFirst();
                boolean vanished = record.get(DataHandler.vanished);
                User user = new User(Bukkit.getOfflinePlayer(uuid), null, null, vanished);
                Users.loadWithContext(user, context);
                return user;
            }

            DatabaseConnector.getInstance().context()
                    .insertInto(users)
                    .set(DataHandler.uuid, uuid)
                    .set(DataHandler.vanished, false)
                    .execute();

            User user = new User(Bukkit.getOfflinePlayer(uuid), null, null, false);
            Users.loadWithContext(user, context);
            return (com.artillexstudios.axvanish.api.users.User) user;
        }, ASYNC_EXECUTOR).exceptionallyAsync(throwable -> {
            LogUtils.error("Failed to load user data for player {}! Falling back to default user!", uuid, throwable);
            User user = new User(Bukkit.getOfflinePlayer(uuid), null, null, false);
            Users.loadWithContext(user, context);
            return user;
        }, ASYNC_EXECUTOR);
    }

    public static void save(User user) {
        CompletableFuture.runAsync(() -> DatabaseConnector.getInstance().context()
                .update(users)
                .set(DataHandler.vanished, user.vanished())
                .where(DataHandler.uuid.eq(user.player().getUniqueId()))
                .execute(), ASYNC_EXECUTOR).exceptionallyAsync(throwable -> {
            LogUtils.error("Failed to save user data for player {}!", user.player().getName(), throwable);
            return null;
        }, ASYNC_EXECUTOR);
    }
}
