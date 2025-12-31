package com.artillexstudios.axvanish.command;

import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvanish.AxVanishPlugin;
import com.artillexstudios.axvanish.api.AxVanishAPI;
import com.artillexstudios.axvanish.api.context.VanishContext;
import com.artillexstudios.axvanish.api.context.VanishSource;
import com.artillexstudios.axvanish.api.context.source.CommandVanishSource;
import com.artillexstudios.axvanish.api.context.source.ConsoleVanishSource;
import com.artillexstudios.axvanish.api.context.source.ForceVanishSource;
import com.artillexstudios.axvanish.api.context.source.ReloadVanishSource;
import com.artillexstudios.axvanish.api.users.User;
import com.artillexstudios.axvanish.config.Config;
import com.artillexstudios.axvanish.config.Groups;
import com.artillexstudios.axvanish.config.Language;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.setting.ManagerSetting;

import java.util.ArrayList;
import java.util.List;

public final class AxVanishCommand {

    private final AxVanishPlugin plugin;

    public AxVanishCommand(AxVanishPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        PaperCommandManager<CommandSourceStack> manager;
        try {
            manager = PaperCommandManager.builder()
                    .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                    .buildOnEnable(this.plugin);
            manager.settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true);
        } catch (Exception e) {
            LogUtils.error("Failed to initialize command manager", e);
            return;
        }

        manager.command(manager.commandBuilder("vanish", "v", "axvanish")
                .permission("axvanish.vanish")
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    if (!(sender instanceof Player player)) {
                        return;
                    }

                    User user = AxVanishAPI.instance().getUserIfLoadedImmediately(player);
                    if (user == null) {
                        MessageUtils.sendMessage(player, Language.prefix, Language.error.userNotLoaded);
                        return;
                    }
                    if (Config.debug) {
                        LogUtils.debug("Debug is working!");
                    }

                    VanishContext vanishContext = new VanishContext.Builder()
                            .withSource(user)
                            .withSource(CommandVanishSource.INSTANCE)
                            .build();
                    boolean previous = user.vanished();
                    if (!user.update(!user.vanished(), vanishContext)) {
                        LogUtils.info("Failed to change state!");
                        // The user's visibility was not changed because an event was cancelled
                        return;
                    }

                    if (previous) {
                        MessageUtils.sendMessage(player, Language.prefix, Language.unVanish.unVanish);
                        AxVanishAPI.instance().online()
                                .stream()
                                .filter(other -> other.canSee(user) && other != user)
                                .forEach(other -> MessageUtils.sendMessage(other.onlinePlayer(), Language.prefix, Language.unVanish.broadcast, Placeholder.unparsed("player", player.getName())));
                    } else {
                        MessageUtils.sendMessage(player, Language.prefix, Language.vanish.vanish);
                        AxVanishAPI.instance().online()
                                .stream()
                                .filter(other -> other.canSee(user) && other != user)
                                .forEach(other -> MessageUtils.sendMessage(other.onlinePlayer(), Language.prefix, Language.vanish.broadcast, Placeholder.unparsed("player", player.getName())));
                    }
                })
        );

        manager.command(manager.commandBuilder("vanish", "v", "axvanish")
                .literal("toggle")
                .permission("axvanish.command.toggle.other")
                .required("player", OfflinePlayerParser.offlinePlayerParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    OfflinePlayer offlinePlayer = context.get("player");

                    VanishSource source = sender instanceof Player player ? AxVanishAPI.instance().getUserIfLoadedImmediately(player) : ConsoleVanishSource.INSTANCE;

                    AxVanishAPI.instance().user(offlinePlayer.getUniqueId()).thenAccept(user -> {
                        if (source instanceof User senderUser) {
                            if (senderUser.group() != null && user.group() != null && senderUser.group().priority() < user.group().priority()) {
                                MessageUtils.sendMessage(sender, Language.prefix, Language.error.notHighEnoughGroup);
                                return;
                            }
                        }

                        VanishContext vanishContext = new VanishContext.Builder()
                                .withSource(source)
                                .withSource(CommandVanishSource.INSTANCE)
                                .build();

                        boolean previous = user.vanished();
                        if (!user.update(!user.vanished(), vanishContext)) {
                            LogUtils.info("Failed to change state!");
                            return;
                        }

                        String targetName = user.player() != null ? user.player().getName() : offlinePlayer.getName();
                        if (targetName == null) {
                            targetName = "Unknown";
                        }

                        if (previous) {
                            MessageUtils.sendMessage(sender, Language.prefix, Language.unVanish.unVanish);
                            String finalTargetName = targetName;
                            AxVanishAPI.instance().online()
                                    .stream()
                                    .filter(other -> other.canSee(user) && other != user)
                                    .forEach(other -> MessageUtils.sendMessage(other.onlinePlayer(), Language.prefix, Language.unVanish.broadcast, Placeholder.unparsed("player", finalTargetName)));
                        } else {
                            MessageUtils.sendMessage(sender, Language.prefix, Language.vanish.vanish);
                            String finalTargetName1 = targetName;
                            AxVanishAPI.instance().online()
                                    .stream()
                                    .filter(other -> other.canSee(user) && other != user)
                                    .forEach(other -> MessageUtils.sendMessage(other.onlinePlayer(), Language.prefix, Language.vanish.broadcast, Placeholder.unparsed("player", finalTargetName1)));
                        }
                    });
                })
        );

        manager.command(manager.commandBuilder("vanish", "v", "axvanish")
                .literal("admin")
                .permission("axvanish.command.admin")
                .literal("version")
                .permission("axvanish.command.admin.version")
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    MessageUtils.sendMessage(sender, Language.prefix, "<green>You are running <white>AxVanish</white> version <white><version></white> on <white><implementation></white> version <white><implementation-version></white> (Implementing API version <white><api-version></white>)",
                            Placeholder.unparsed("version", this.plugin.getDescription().getVersion()),
                            Placeholder.unparsed("implementation", Bukkit.getName()),
                            Placeholder.unparsed("implementation-version", Bukkit.getVersion()),
                            Placeholder.unparsed("api-version", Bukkit.getBukkitVersion())
                    );
                })
        );

        manager.command(manager.commandBuilder("vanish", "v", "axvanish")
                .literal("admin")
                .permission("axvanish.command.admin")
                .literal("reload")
                .permission("axvanish.command.admin.reload")
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    long start = System.nanoTime();
                    List<String> failed = new ArrayList<>();

                    if (!Config.reload()) {
                        failed.add("config.yml");
                    }

                    if (!Language.reload()) {
                        failed.add("language/" + Language.lastLanguage + ".yml");
                    }

                    if (!Groups.reload()) {
                        failed.add("groups.yml");
                    }

                    for (User user : AxVanishAPI.instance().online()) {
                        if (user.vanished()) {
                            user.update(false, new VanishContext.Builder()
                                    .withSource(ReloadVanishSource.INSTANCE)
                                    .withSource(ForceVanishSource.INSTANCE)
                                    .build()
                            );
                        }
                    }

                    if (failed.isEmpty()) {
                        MessageUtils.sendMessage(sender, Language.prefix, Language.reload.success, Placeholder.unparsed("time", Long.toString((System.nanoTime() - start) / 1_000_000)));
                    } else {
                        MessageUtils.sendMessage(sender, Language.prefix, Language.reload.fail, Placeholder.unparsed("time", Long.toString((System.nanoTime() - start) / 1_000_000)), Placeholder.unparsed("files", String.join(", ", failed)));
                    }
                })
        );
    }
}
