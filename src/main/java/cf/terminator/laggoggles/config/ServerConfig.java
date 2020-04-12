package cf.terminator.laggoggles.config;

import cf.terminator.laggoggles.config.ConfigBase;
import cf.terminator.laggoggles.util.Perms;

public class ServerConfig extends ConfigBase {
    public ConfigGroup client = group(0, "server",
            "Server-only settings");

    public ConfigEnum<Perms.Permission> NON_OP_PERMISSION_LEVEL = e(Perms.Permission.START, "NON_OP_PERMISSION_LEVEL",
            "What's the permission level available to non-operators (Normal players)?\n" +
                    "Please note that this ONLY works on dedicated servers. If you're playing singleplayer or LAN, the FULL permission is used.\n" +
                    "Available permissions in ascending order are:\n" +
                    "   'NONE'  No permissions are granted, all functionality is denied.\n" +
                    "   'GET'   Allow getting the latest scan result, this will be stripped down to the player's surroundings\n" +
                    "   'START' Allow starting the profiler\n" +
                    "   'FULL'  All permissions are granted, teleporting to entities, blocks");

    public ConfigBool ALLOW_NON_OPS_TO_SEE_EVENT_SUBSCRIBERS = b(false, "ALLOW_NON_OPS_TO_SEE_EVENT_SUBSCRIBERS",
            "Allow normal users to see event subscribers?");

    public ConfigInt NON_OPS_MAX_PROFILE_TIME = i(20, 1 ,  "NON_OPS_MAX_PROFILE_TIME",
            "If normal users can start the profiler, what is the maximum time in seconds?");

    public ConfigInt NON_OPS_PROFILE_COOL_DOWN_SECONDS = i(120, 1 ,  "NON_OPS_PROFILE_COOL_DOWN_SECONDS",
            "If normal users can start the profiler, what is the cool-down between requests in seconds?");

    public ConfigDouble NON_OPS_MAX_HORIZONTAL_RANGE = d(50, 1, "NON_OPS_MAX_HORIZONTAL_RANGE",
            "What is the maximum HORIZONTAL range in blocks normal users can get results for?");

    public ConfigDouble NON_OPS_MAX_VERTICAL_RANGE = d(20, 1, "NON_OPS_MAX_VERTICAL_RANGE",
            "What is the maximum VERTICAL range in blocks normal users can get results for?");

    public ConfigInt NON_OPS_WHITELIST_HEIGHT_ABOVE = i(64, 1 ,  "NON_OPS_WHITELIST_HEIGHT_ABOVE",
            "From where should we range-limit blocks vertically for normal users?\n" +
                    "This will override the MAX_VERTICAL_RANGE when the block is above this Y level");

    public ConfigInt NON_OPS_REQUEST_LAST_SCAN_DATA_TIMEOUT = i(30, 1 ,  "NON_OPS_REQUEST_LAST_SCAN_DATA_TIMEOUT",
            "How often can normal users request the latest scan result in seconds?");

    @Override public String getName() {
        return "server";
    }
}
