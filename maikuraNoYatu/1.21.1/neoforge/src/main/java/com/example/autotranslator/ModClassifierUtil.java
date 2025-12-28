package com.example.autotranslator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        TRANSLATION_BLACKLIST.add("mezz.jei.");                      // JEI
        TRANSLATION_BLACKLIST.add("journeymap.");                    // JourneyMap

        //翻訳対象のブラックリスト(部分一致)
        TEXTFIELD_BLACKLIST.add("Text");
        TEXTFIELD_BLACKLIST.add("Search");
    }

    /**
     * スタックトレースから翻訳対象MODを特定
     *
     * @return MOD情報（パッケージ名 + メソッド名）、見つからなければ null
     */
    public static List<ModInfo> findTranslatableMods(int maxCount) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        List<ModInfo> result = new ArrayList<>();

        for (int i = 0; i < stack.length && result.size() < maxCount; i++) {
            StackTraceElement element = stack[i];
            String className = element.getClassName();

            // ブラックリストに該当する場合はスキップ
            if (isBlacklisted(className)) {
                continue;
            }
            if (shouldIgnoreTranslation(className)) {
                if (result.isEmpty()) {
                    result = List.of();
                }
                return result;
            }

            // 翻訳対象のMODを発見
            ModInfo modInfo = new ModInfo(
                    className,
                    element.getMethodName(),
                    extractModPackage(className)
            );

            result.add(modInfo);
        }

        return result;
    }

    /**
     * クラス名がブラックリストに該当するか判定
     */
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

    /**
     * クラス名からMODのルートパッケージを推定
     * 例: "dev.ftb.mods.ftbquests.gui.QuestScreen" → "dev.ftb.mods.ftbquests"
     */
    private static String extractModPackage(String className) {
        if (className == null) return "Unknown";

        String[] parts = className.split("\\.");

        // 最初の3-4セグメントを取得
        if (parts.length >= 4) {
            return parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
        } else if (parts.length >= 3) {
            return parts[0] + "." + parts[1] + "." + parts[2];
        } else if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        }

        return className;
    }

    /**
     * ブラックリストに追加（動的）
     */
    public static void addToBlacklist(String packagePrefix) {
        BLACKLIST.add(packagePrefix);
    }

    /**
     * MOD情報を保持するクラス
     */
    public static class ModInfo {
        public final String fullClassName;
        public final String methodName;
        public final String modPackage;

        public ModInfo(String fullClassName, String methodName, String modPackage) {
            this.fullClassName = fullClassName;
            this.methodName = methodName;
            this.modPackage = modPackage;
        }

        /**
         * 短縮形のクラス名を取得
         * 例: "dev.ftb.mods.ftbquests.gui.QuestScreen" → "QuestScreen"
         */
        public String getSimpleClassName() {
            int lastDot = fullClassName.lastIndexOf('.');
            return lastDot >= 0 ? fullClassName.substring(lastDot + 1) : fullClassName;
        }

        /**
         * ログ用の文字列
         */
        @Override
        public String toString() {
            return modPackage + " [" + getSimpleClassName() + "." + methodName + "()]";
        }

        /**
         * 短縮形
         */
        public String toShortString() {
            return getSimpleClassName() + "." + methodName + "()";
        }

        public String toFullString() {
            return fullClassName + "." + methodName + "()";
        }
    }
}