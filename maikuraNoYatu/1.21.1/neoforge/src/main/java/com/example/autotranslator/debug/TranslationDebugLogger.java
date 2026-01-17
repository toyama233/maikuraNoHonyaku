package com.example.autotranslator.debug;

import com.example.autotranslator.ModClassifierUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationDebugLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("AutoTranslator");
    public static final boolean DEBUG = true;

    // すでに出力した文字列
    private static final Set<String> LOGGED =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void logOnce(String text) {
        if (!DEBUG) return;
        if (text == null || text.isBlank()) return;

        // add が true を返した時だけ未登録
        if (LOGGED.add(text)) {
            LOGGER.info("[GUI TEXT] {}", text);
        }
    }

    public static void LogStackTrace(String text, List<ModClassifierUtil.ModInfo> modList){
        if (!DEBUG) return;
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(text);
        logBuilder.append("\n  └─ Stack: ");
        final Logger LOGGER = LoggerFactory.getLogger("AutoTranslator");
        for (int i = 0; i < modList.size(); i++) {
            if (i > 0) {
                logBuilder.append(" → ");
            }
            logBuilder.append(modList.get(i).fullClassName);
            logBuilder.append(".");
            logBuilder.append(modList.get(i).methodName);
            logBuilder.append("()");
            if( i % 3 == 0){
                logBuilder.append("\n");
            }
        }
        logOnce(logBuilder.toString());
    }
}