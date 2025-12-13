package com.artillexstudios.axvanish.api.group.capabilities;

import java.util.Map;

/**
 * Marker capability that indicates users in the group should automatically
 * be set to vanished when they join the server (assuming they have permission).
 *
 * No event listeners are required here; the join logic checks for the presence
 * of this capability on the user's group.
 */
public final class VanishOnJoinCapability extends VanishCapability {

    public VanishOnJoinCapability(Map<String, Object> config) {
        super(config);
    }
}
