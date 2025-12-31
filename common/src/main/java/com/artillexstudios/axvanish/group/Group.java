package com.artillexstudios.axvanish.group;

import com.artillexstudios.axvanish.api.group.capabilities.VanishCapabilities;
import com.artillexstudios.axvanish.api.group.capabilities.VanishCapability;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

import java.util.List;
import java.util.Map;

public final class Group implements com.artillexstudios.axvanish.api.group.Group {

    private Group parent;
    private final String name;
    private final int priority;
    private final ClassToInstanceMap<VanishCapability> capabilities;

    public Group(String name, int priority, List<Map<String, Object>> capabilities) {
        this.name = name;
        this.priority = priority;
        ClassToInstanceMap<VanishCapability> map = MutableClassToInstanceMap.create();
        for (Map<String, Object> capability : capabilities) {
            String type = (String) capability.get("type");
            if (type == null || type.isBlank()) {
                continue;
            }

            VanishCapability vanishCapability = VanishCapabilities.create(type, capability);
            if (vanishCapability == null) {
                continue;
            }

            map.put(vanishCapability.getClass(), vanishCapability);
        }
        this.capabilities = ImmutableClassToInstanceMap.copyOf(map);
    }

    public void parent(Group group) {
        this.parent = group;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public int priority() {
        return this.priority;
    }

    @Override
    public boolean hasCapability(Class<? extends VanishCapability> capability) {
        boolean has = this.capabilities.containsKey(capability);
        return has || this.parent != null && this.parent.hasCapability(capability);
    }

    @Override
    public <T extends VanishCapability> T capability(Class<T> capability) {
        VanishCapability thisCapability = this.capabilities.get(capability);
        return capability.cast(thisCapability == null && this.parent != null ? this.parent.capability(capability) : thisCapability);
    }
}
