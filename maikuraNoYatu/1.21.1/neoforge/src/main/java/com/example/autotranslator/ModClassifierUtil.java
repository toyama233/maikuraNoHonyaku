package com.example.autotranslator;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.example.autotranslator.debug.TranslationDebugLogger.logOnce;

public class ModClassifierUtil {

    private static final Set<String> BLACKLIST = new HashSet<>();
    private static final Set<String> TRANSLATION_BLACKLIST = new HashSet<>();
    private static final Set<String> TEXTFIELD_BLACKLIST = new HashSet<>();

    static {
        // Minecraft 本体
        BLACKLIST.add("net.minecraft.");
        BLACKLIST.add("cpw.");
        BLACKLIST.add("com.mojang.");
        BLACKLIST.add("net.minecraft.client.gui.Font");
        BLACKLIST.add("net.minecraft.client.gui.GuiGraphics");

        // Java 標準
        BLACKLIST.add("java.");
        BLACKLIST.add("javax.");
        BLACKLIST.add("sun.");
        BLACKLIST.add("jdk.");

        BLACKLIST.add("net.neoforged.");
        BLACKLIST.add("org.spongepowered.asm.");
        BLACKLIST.add("com.example.autotranslator.");

        //翻訳の対象のブラックリスト(前方一致)
        TRANSLATION_BLACKLIST.add("mezz.jei.");
        TRANSLATION_BLACKLIST.add("journeymap.");

        //翻訳対象のブラックリスト(部分一致)
        TEXTFIELD_BLACKLIST.add("Text");
        TEXTFIELD_BLACKLIST.add("Search");
    }

    public static List<ModInfo> findTranslatableMods(int maxCount) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        List<ModInfo> result = new ArrayList<>();

        for (int i = 0; i < stack.length && result.size() < maxCount; i++) {
            StackTraceElement element = stack[i];
            String className = element.getClassName();

            if (isBlacklisted(className)) {
                continue;
            }
            if (shouldIgnoreTranslation(className)) {
                if (result.isEmpty()) {
                    result = List.of();
                }
                return result;
            }

            ModInfo modInfo = new ModInfo(
                    className,
                    element.getMethodName(),
                    extractModPackage(className)
            );

            result.add(modInfo);
        }

        return result;
    }

    private static boolean isBlacklisted(String className) {
        if (className == null || className.isEmpty()) {
            return true;
        }

        for (String prefix : BLACKLIST) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    private static boolean shouldIgnoreTranslation(String className) {
        if (className == null || className.isEmpty()) {
            return true;
        }
        for (String prefix : TRANSLATION_BLACKLIST) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        for (String prefix : TEXTFIELD_BLACKLIST) {
            if (className.contains(prefix)) {
                logOnce("スキップされました" + prefix + "　クラス名{}" + className);
                return true;
            }
        }
        return false;
    }

    private static String extractModPackage(String className) {
        if (className == null) return "Unknown";

        String[] parts = className.split("\\.");

        if (parts.length >= 4) {
            return parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
        } else if (parts.length >= 3) {
            return parts[0] + "." + parts[1] + "." + parts[2];
        } else if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        }

        return className;
    }

    public static class ModInfo {
        public final String fullClassName;
        public final String methodName;
        public final String modPackage;

        public ModInfo(String fullClassName, String methodName, String modPackage) {
            this.fullClassName = fullClassName;
            this.methodName = methodName;
            this.modPackage = modPackage;
        }

        public String getSimpleClassName() {
            int lastDot = fullClassName.lastIndexOf('.');
            return lastDot >= 0 ? fullClassName.substring(lastDot + 1) : fullClassName;
        }

        @Override
        public String toString() {
            return modPackage + " [" + getSimpleClassName() + "." + methodName + "()]";
        }

    }
}