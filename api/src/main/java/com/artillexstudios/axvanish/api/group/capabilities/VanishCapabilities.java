package com.artillexstudios.axvanish.api.group.capabilities;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvanish.api.AxVanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class VanishCapabilities {
    private static final Map<String, Class<? extends VanishCapability>> capabilities = new HashMap<>();
    public static final Class<ActionBarCapability> ACTION_BAR = register("action_bar", ActionBarCapability.class);
    public static final Class<ChatCapability> CHAT = register("chat", ChatCapability.class);
    public static final Class<FlightCapability> FLIGHT = register("flight", FlightCapability.class);
    public static final Class<InvincibleCapability> INVINCIBLE = register("invincible", InvincibleCapability.class);
    public static final Class<MessageCapability> MESSAGE = register("message", MessageCapability.class);
    public static final Class<PotionEffectsCapability> POTION_EFFECTS = register("potion_effects", PotionEffectsCapability.class);
    public static final Class<PreventCapability> PREVENT = register("prevent", PreventCapability.class);
    public static final Class<SilentOpenCapability> SILENT_OPEN = register("silent_open", SilentOpenCapability.class);
    public static final Class<VanishOnJoinCapability> VANISH_ON_JOIN = register("vanish_on_join", VanishOnJoinCapability.class);

    public static <T extends VanishCapability> Class<T> register(String key, Class<T> capability) {
        capabilities.put(key, capability);
        if (Listener.class.isAssignableFrom(capability)) {
            Listener listener = create(key, new HashMap<>());
            if (listener == null) {
                LogUtils.warn("Failed to register a listener for capability with key {}!", key);
                return capability;
            }

            Bukkit.getPluginManager().registerEvents(listener, AxVanishAPI.instance().plugin());
        }

        return capability;
    }

    public static <T extends VanishCapability> Class<T> parse(String key) {
        return (Class<T>) capabilities.get(key);
    }

    public static <T extends VanishCapability> T create(String key, Map<String, Object> map) {
        Class<T> capability = (Class<T>) capabilities.get(key);
        if (capability == null) {
            LogUtils.warn("Failed to find capability with key {}! Loaded capabilities: {}", key, capabilities.keySet());
            return null;
        }

        try {
            return capability.getDeclaredConstructor(Map.class).newInstance(map);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
