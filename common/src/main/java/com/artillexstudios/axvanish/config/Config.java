package com.artillexstudios.axvanish.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.config.annotation.PostProcess;
import com.artillexstudios.axapi.config.annotation.Serializable;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvanish.database.DatabaseType;
import com.artillexstudios.axvanish.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config implements ConfigurationPart {

    private static final Config INSTANCE = new Config();
    public static Database database = new Database();

    @Serializable
    public static class Database {
        @Comment("h2, sqlite or mysql")
        public DatabaseType type = DatabaseType.H2;
        public String address = "127.0.0.1";
        public int port = 3306;
        public String database = "admin";
        public String username = "admin";
        public String password = "admin";
        public Pool pool = new Pool();

        @Serializable
        public static class Pool {
            public int maximumPoolSize = 10;
            public int minimumIdle = 10;
            public int maximumLifetime = 1800000;
            public int keepaliveTime = 0;
            public int connectionTimeout = 5000;

            @PostProcess
            public void postProcess() {
                if (maximumPoolSize < 1) {
                    LogUtils.warn("Maximum database pool size is lower than 1! This is not supported! Defaulting to 1.");
                    maximumPoolSize = 1;
                }
            }
        }
    }

    @Comment("""
            How many threads should we use for database queries?
            Don't change it, if you don't know what it does!
            """)
    public static int asyncUtilsThreadCount = 1;
    @Comment("""
            The cooldown between sending the player the prohibited action message.
            This is in milliseconds.
            """)
    public static int messageCooldown = 10000;

    @Comment("""
            What language file should we load from the lang folder?
            You can create your own aswell! We would appreciate if you
            contributed to the plugin by creating a pull request with your translation!
            """)
    public static String language = "en_US";
    @Comment("""
            If we should send debug messages in the console
            You shouldn't enable this, unless you want to see what happens in the code
            """)
    public static boolean debug = false;
    @Comment("Do not touch!")
    public static int configVersion = 1;
    private YamlConfiguration config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        Path path = FileUtils.PLUGIN_DIRECTORY.resolve("config.yml");
        if (Files.exists(path)) {
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.of(path, Config.class).configVersion(1, "config-version").withDumperOptions(options -> {
            }).build();
        }

        this.config.load();
        return true;
    }
}
