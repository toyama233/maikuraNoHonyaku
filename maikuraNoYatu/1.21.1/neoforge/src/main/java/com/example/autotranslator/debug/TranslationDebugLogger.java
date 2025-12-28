package com.example.autotranslator.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationDebugLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("AutoTranslator");

    // すでに出力した文字列
    private static final Set<String> LOGGED =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void logOnce(String text) {
        if (text == null || text.isBlank()) return;

        // add が true を返した時だけ未登録
        if (LOGGED.add(text)) {
            LOGGER.info("[GUI TEXT] {}", text);
        }
    }
}