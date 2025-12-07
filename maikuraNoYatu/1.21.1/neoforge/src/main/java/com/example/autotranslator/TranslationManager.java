package com.example.autotranslator;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class TranslationManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    public static CompletableFuture<String> translateAsync(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
                String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=ja&dt=t&q=" + encoded;

                LOGGER.info("[Translator] 翻訳開始: {}", text);

                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                request.setHeader("Referer", "https://translate.google.com/");

                LOGGER.info("[Translator]リクエスト送信前: {}", url);

                CloseableHttpResponse response = httpClient.execute(request);
                String body = EntityUtils.toString(response.getEntity());

                LOGGER.info("[Translator]ボディ取得完了: 長さ={}", body != null ? body.length() : "null");
                LOGGER.info("[Translator] json: {}", body);

                JsonElement element = JsonParser.parseString(body);
                String translated = FindTranslatString(element);
                return translated;
            } catch (Exception e) {
//                LOGGER.warn("翻訳中にエラー発生: {}", e.getMessage());
                LOGGER.warn("翻訳中にエラー発生: 例外タイプ={}, メッセージ={}",
                        e.getClass().getName(), e.getMessage());
                LOGGER.warn("スタックトレース: ", e);  // 完全なスタックトレースをログ出力
                return text;
            }
        });
    }

    public static void displayTranslated(String original) {
        translateAsync(original).thenAccept(translated -> {
            Minecraft.getInstance().gui.getChat().addMessage(Component.literal(translated));
        });
    }

    private static String FindTranslatString(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }

        Collection<JsonElement> children = Collections.emptyList();
        if (element.isJsonArray()) {
            children = element.getAsJsonArray().asList();
        } else if (element.isJsonObject()) {
            children = element.getAsJsonObject().asMap().values();
        }

        return children.stream()
                .map(TranslationManager::FindTranslatString)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }
}