package com.artillexstudios.axvanish.config;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Ignored;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axvanish.group.Group;
import com.artillexstudios.axvanish.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Groups {

    private static final Groups INSTANCE = new Groups();
    private YamlConfiguration config = null;
    @Ignored
    public static Map<String, Group> groups = new HashMap<>();

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        Path path = FileUtils.PLUGIN_DIRECTORY.resolve("groups.yml");
        if (Files.exists(path)) {
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.of(path)
                    .withDefaults(AxPlugin.getPlugin(AxPlugin.class).getResource("groups.yml"))
                    .configVersion(1, "config-version")
                    .withDumperOptions(options -> {
                        options.setPrettyFlow(true);
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                    }).build();
        }

        this.config.load();
        Map<String, Group> groups = new HashMap<>();
        Map<String, Map<String, Object>> section = this.config.getMap("groups");
        if (section == null) {
            return false;
        }

        for (Map.Entry<String, Map<String, Object>> entry : section.entrySet()) {
            groups.put(entry.getKey(), new Group(entry.getKey(), (Integer) entry.getValue().getOrDefault("priority", 1), (List<Map<String, Object>>) entry.getValue().getOrDefault("capabilities", List.of())));
        }

        for (Map.Entry<String, Map<String, Object>> entry : section.entrySet()) {
            String parent = (String) entry.getValue().get("parent");
            if (parent == null) {
                continue;
            }

            Group parentGroup = groups.get(parent);
            if (parentGroup == null) {
                continue;
            }

            groups.get(entry.getKey()).parent(parentGroup);
        }

        Groups.groups = groups;
        return true;
    }
}
