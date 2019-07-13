package com.denizenscript.depenizen.bukkit.properties.worldguard;

import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.depenizen.bukkit.bridges.WorldGuardBridge;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.debugging.dB;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardPlayerProperties implements Property {

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "WorldGuardPlayer";
    }

    @Override
    public void adjust(Mechanism mechanism) {
        // None
    }

    public static boolean describes(dObject object) {
        return object instanceof dPlayer
                && ((dPlayer) object).isOnline();
    }

    public static WorldGuardPlayerProperties getFrom(dObject object) {
        if (!describes(object)) {
            return null;
        }
        return new WorldGuardPlayerProperties((dPlayer) object);
    }

    public static final String[] handledTags = new String[] {
            "worldguard"
    };

    public static final String[] handledMechs = new String[] {
    }; // None

    private WorldGuardPlayerProperties(dPlayer player) {
        this.player = player.getPlayerEntity();
    }

    Player player = null;

    private StateFlag getStateFlag(String s) {
        Flag flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), s);
        return flag instanceof StateFlag ? (StateFlag) flag : null;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (!attribute.startsWith("worldguard") && !attribute.startsWith("wg")) {
            return null;
        }
        attribute = attribute.fulfill(1);

        // <--[tag]
        // @attribute <p@player.worldguard.can_build[<location>]>
        // @returns Element(Boolean)
        // @description
        // Whether WorldGuard allows to build at a location.
        // @Plugin Depenizen, WorldGuard
        // -->
        if (attribute.startsWith("can_build") && attribute.hasContext(1)) {
            dLocation location = dLocation.valueOf(attribute.getContext(1));
            if (location == null) {
                return null;
            }
            WorldGuardPlugin worldGuard = (WorldGuardPlugin) WorldGuardBridge.instance.plugin;
            return new Element(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().testBuild(BukkitAdapter.adapt(location), worldGuard.wrapPlayer(player)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.worldguard.test_flag[<name>]>
        // @returns Element(Boolean)
        // @description
        // Returns the state of a flag for that player at their location.
        // For example: .test_flag[pvp] returns 'true' when the player can be attacked.
        // @Plugin Depenizen, WorldGuard
        // -->
        if (attribute.startsWith("test_flag")) {
            if (!attribute.hasContext(1)) {
                dB.echoError("The tag p@player.worlduard.test_flag[...] must have a value.");
                return null;
            }
            StateFlag flag = getStateFlag(attribute.getContext(1));
            if (flag == null) {
                dB.echoError("The tag p@player.worlduard.test_flag[...] has an invalid value: " + attribute.getContext(1));
                return null;
            }

            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            Location loc = player.getLocation();
            int args = 1;

            // <--[tag]
            // @attribute <p@player.worldguard.test_flag[<name>].at[<location>]>
            // @returns Element(Boolean)
            // @description
            // Returns the state of a flag for that player at the specified location.
            // @Plugin Depenizen, WorldGuard
            // -->
            if (attribute.getAttribute(2).startsWith("at") && attribute.hasContext(2)) {
                loc = dLocation.valueOf(attribute.getContext(2));
                args = 2;
                if (loc == null) {
                    return null;
                }
            }
            WorldGuardPlugin worldGuard = (WorldGuardPlugin) WorldGuardBridge.instance.plugin;
            return new Element(query.testState(BukkitAdapter.adapt(loc), worldGuard.wrapPlayer(player), flag))
                    .getAttribute(attribute.fulfill(args));
        }

        return null;

    }
}
