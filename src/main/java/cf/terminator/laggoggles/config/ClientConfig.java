package cf.terminator.laggoggles.config;

import cf.terminator.laggoggles.util.ColorBlindMode;

public class ClientConfig extends ConfigBase {
    public ConfigGroup client = group(0, "client",
            "Client-only settings - If you're looking for general settings, look inside your worlds serverconfig folder!");

    public ConfigInt GRADIENT_MAXED_OUT_AT_MICROSECONDS = i(25, 1 ,  "GRADIENT_MAXED_OUT_AT_MICROSECONDS",
            "Define the number of microseconds at which an object is marked with a deep red colour for WORLD lag.");

    public ConfigInt GRADIENT_MAXED_OUT_AT_NANOSECONDS_FPS = i(50000, 1 ,  "GRADIENT_MAXED_OUT_AT_NANOSECONDS_FPS",
            "Define the number of microseconds at which an object is marked with a deep red colour for FPS lag.");

    public ConfigInt MINIMUM_AMOUNT_OF_MICROSECONDS_THRESHOLD = i(1, 1 ,  "MINIMUM_AMOUNT_OF_MICROSECONDS_THRESHOLD",
            "What is the minimum amount of microseconds required before an object is tracked in the client?\n" +
                    "This is only for WORLD lag.\n" +
                    "This also affects the analyze results window");
    public ConfigEnum<ColorBlindMode> COLORS = e(ColorBlindMode.GREEN_TO_RED, "COLORS", "If you're colorblind, change this to fit your needs.\n" +
            "Available options:\n" +
            "- GREEN_TO_RED\n" +
            "- BLUE_TO_RED\n" +
            "- GREEN_TO_BLUE");

    @Override
    public String getName() {
        return "client";
    }
}
