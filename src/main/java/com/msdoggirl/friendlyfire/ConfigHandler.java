package com.msdoggirl.friendlyfire;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHandler {

    private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<List<String>> PROTECTED_MOBS;

    private static CommentedFileConfig fileConfig;
    public static final ForgeConfigSpec CONFIG;

    static {
        PROTECTED_MOBS = builder
                .comment("List of protected mobs that players cannot harm")
                .define("protectedMobs", new ArrayList<>());

        CONFIG = builder.build();
    }

    public static void load(Path path) {
        fileConfig = CommentedFileConfig.builder(path).autosave().build();
        fileConfig.load();
        CONFIG.setConfig(fileConfig);
    }

    public static void save() {
        if (fileConfig != null) {
            fileConfig.save();
        }
    }

    public static List<String> getProtectedMobs() {
        return new ArrayList<>(PROTECTED_MOBS.get());
    }

    public static void setProtectedMobs(List<String> mobs) {
        System.out.println("Saving protected mobs: " + mobs);
        PROTECTED_MOBS.set(mobs);
        save();
    }
}
