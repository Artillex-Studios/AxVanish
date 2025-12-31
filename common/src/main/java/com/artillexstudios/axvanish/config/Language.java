package com.artillexstudios.axvanish.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.config.annotation.Ignored;
import com.artillexstudios.axapi.config.annotation.Named;
import com.artillexstudios.axapi.config.annotation.Serializable;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvanish.AxVanishPlugin;
import com.artillexstudios.axvanish.utils.FileUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Language implements ConfigurationPart {
    private static final Path LANGUAGE_DIRECTORY = FileUtils.PLUGIN_DIRECTORY.resolve("language");
    private static final Language INSTANCE = new Language();
    @Comment("The prefix we should use before messages sent by the plugin.")
    public static String prefix = "<b><gradient:#CB2D3E:#EF473A>AxVanish</gradient></b> ";
    public static Reload reload = new Reload();
    public static Error error = new Error();
    public static Vanish vanish = new Vanish();
    @Named("unvanish")
    public static UnVanish unVanish = new UnVanish();

    @Serializable
    public static class Reload {
        public String success = "<#00FF00>Successfully reloaded the configurations of the plugin in <white><time></white>ms!";
        public String fail = "<#FF0000>There were some issues while reloading file(s): <white><files></white>! Please check out the console for more information! <br>Reload done in: <white><time></white>ms!";
    }

    @Serializable
    public static class Error {
        public String userNotLoaded = "<#FF0000>Your userdata has not loaded yet! Please try again in a moment!";
        public String vanished = "<#FF0000>You can't do that while vanished!";
        public String notHighEnoughGroup = "<#FF0000>Your group's priority isn't high enough to change that user's vanish state-";
    }

    @Serializable
    public static class Vanish {
        public String vanish = "<#00FF00>You have successfully vanished!";
        public String broadcast = "<#00FF00><player> has vanished!";
    }

    @Serializable
    public static class UnVanish {
        @Named("unvanish")
        public String unVanish = "<#00FF00>You have successfully unvanished!";
        public String broadcast = "<#00FF00><player> has unvanished!";
        public String hadNoVanishPermission = "<white><player></white> <#FF0000>has no vanish permission, but joined with vanish! We have <white>unvanished</white> them!";
    }

    @Comment("Do not touch!")
    public static int configVersion = 1;
    @Ignored
    public static String lastLanguage;
    private YamlConfiguration config = null;

    public static boolean reload() {
        if (Config.debug) {
            LogUtils.debug("Reload called on language!");
        }
        FileUtils.copyFromResource("language");

        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        if (Config.debug) {
            LogUtils.debug("Refreshing language");
        }
        Path path = LANGUAGE_DIRECTORY.resolve(Config.language + ".yml");
        boolean shouldDefault = false;
        if (Files.exists(path)) {
            if (Config.debug) {
                LogUtils.debug("File exists");
            }
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        } else {
            shouldDefault = true;
            path = LANGUAGE_DIRECTORY.resolve("en_US.yml");
            LogUtils.error("No language configuration was found with the name {}! Defaulting to en_US...", Config.language);
        }

        // The user might have changed the config
        if (this.config == null || (lastLanguage != null && lastLanguage.equalsIgnoreCase(Config.language))) {
            lastLanguage = shouldDefault ? "en_US" : Config.language;
            if (Config.debug) {
                LogUtils.debug("Set lastLanguage to {}", lastLanguage);
            }
            InputStream defaults = AxVanishPlugin.instance().getResource("language/" + lastLanguage + ".yml");
            if (defaults == null) {
                if (Config.debug) {
                    LogUtils.debug("Defaults are null, defaulting to en_US.yml");
                }
                defaults = AxVanishPlugin.instance().getResource("language/en_US.yml");
            }

            if (Config.debug) {
                LogUtils.debug("Loading config from file {} with defaults {}", path, defaults);
            }

            this.config = YamlConfiguration.of(path, Language.class)
                    .configVersion(1, "config-version")
                    .withDefaults(defaults)
                    .build();
        }

        this.config.load();
        return true;
    }
}
