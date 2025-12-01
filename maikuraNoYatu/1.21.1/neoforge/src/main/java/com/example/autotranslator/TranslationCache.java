package com.example.autotranslator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TranslationCache {
    private static final String FILE_NAME = "translation_cache.json";
    private static final Gson gson = new Gson();
    private static Map<String, String> cache = new HashMap<>();

    public static void load() {
        try (Reader reader = new FileReader(FILE_NAME)) {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            cache = gson.fromJson(reader, type);
            if (cache == null) cache = new HashMap<>();
        } catch (IOException e) {
            cache = new HashMap<>();
        }
    }

    public static void save() {
        try (Writer writer = new FileWriter(FILE_NAME)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String original) {
        return cache.get(original);
    }

    public static void put(String original, String translated) {
        cache.put(original, translated);
    }
    public static String translateText(String text) {
        String cached = TranslationCache.get(text);
        if (cached != null) {
            return cached;
        }

        String translated = TranslationUtil.translateIfNeededAsync(text).join(); // 既存の翻訳処理
        TranslationCache.put(text, translated);
        return translated;
    }
}