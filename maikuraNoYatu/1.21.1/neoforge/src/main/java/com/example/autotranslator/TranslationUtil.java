package com.example.autotranslator;

import java.util.concurrent.CompletableFuture;

import com.example.autotranslator.debug.TranslationDebugLogger;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class TranslationUtil {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double JP_THRESHOLD  = 0.3;
    private static final String PREFIX = TranslationDebugLogger.DEBUG ? "翻訳済：" : "";


    public static CompletableFuture<String> translateIfNeededAsync(String s) {
        if (skip(s)) {
            LOGGER.info("[TranslationUtil] 翻訳がスキップされました{}", s);
            return completed(s);
        }
        LOGGER.info("[TranslationUtil] 通常通り翻訳されました{}", s);
        return TranslationManager.translateAsync(s)
                .thenApply(t -> PREFIX + t);
    }

    private static boolean skip(String s) {
        return isTranslated(s) || isMostlyJP(s);
    }

    private static boolean isTranslated(String s) {
        if (!TranslationDebugLogger.DEBUG) return false;
        return s.startsWith(PREFIX);
    }

    private static boolean isMostlyJP(String s) {
        long total = s.length();
        long jp = s.codePoints().filter(TranslationUtil::isJP).count();
        return jp > total * JP_THRESHOLD;
    }

    private static boolean isJP(int c) {
        return (c >= 0x3040 && c <= 0x309F) ||
                (c >= 0x30A0 && c <= 0x30FF) ||
                (c >= 0x4E00 && c <= 0x9FFF);
    }

    private static <T> CompletableFuture<T> completed(T s) {
        return CompletableFuture.completedFuture(s);
    }
}