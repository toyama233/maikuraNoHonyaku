package com.example.autotranslator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class TranslationCache {
    private static final String FILE_NAME = "translation_cache.json";
    private static final Gson gson = new Gson();
    private static Map<String, String> cache = new HashMap<>();
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void load() {
        try (Reader reader = new FileReader(FILE_NAME)) {
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            cache = gson.fromJson(reader, type);
            if (cache == null) cache = new HashMap<>();
            LOGGER.info("[TranslationCache.load] キャッシュをロードしました");
        } catch (IOException e) {
            cache = new HashMap<>();
        }
    }

    public static void save() {
        try (Writer writer = new FileWriter(FILE_NAME)) {
            gson.toJson(cache, writer);
            LOGGER.info("[TranslationCache.save] キャッシュをセーブしました");
        } catch (IOException e) {
            LOGGER.warn("翻訳中にエラー発生: 例外タイプ={}, メッセージ={}", e.getClass().getName(), e.getMessage());
            LOGGER.warn("スタックトレース: ", e);
        }

    }

    public static String get(String original) {
        return cache.get(original);
    }

    public static void put(String original, String translated) {
        cache.put(original, translated);
    }

    public static CompletableFuture<String> translateText(String text) {
        String cached = TranslationCache.get(text);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return TranslationUtil.translateIfNeededAsync(text).thenApply(translated -> {
            TranslationCache.put(text, translated);
            return translated;
        });
    }
}