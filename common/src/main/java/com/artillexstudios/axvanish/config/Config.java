package com.artillexstudios.axvanish.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axvanish.AxVanishPlugin;
import com.artillexstudios.axvanish.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config implements ConfigurationPart {
    private static final Config INSTANCE = new Config();

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
            this.config = YamlConfiguration.of(path, Config.class)
                    .configVersion(1, "config-version")
                    .withDefaults(AxVanishPlugin.instance().getResource("config.yml"))
                    .withDumperOptions(options -> {
//                        options.setPrettyFlow(true);
//                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                    }).build();
        }

        this.config.load();
        return true;
    }

}
