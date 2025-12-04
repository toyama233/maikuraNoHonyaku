package com.example.autotranslator;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class TranslationManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HttpClient httpClient = HttpClient.newHttpClient().
            connectTimeout()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * 非同期で翻訳するメソッド
     * @param text 元の文字列
     * @return CompletableFuture<String> 翻訳後文字列
     */
    public static CompletableFuture<String> translateAsync(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // URLエンコード
                String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
                String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=ja&dt=t&q=" + encoded;

                LOGGER.info("[Translator] 翻訳開始: {}", text);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "Mozilla/5.0")
                        .GET()
                        .timeout(Duration.ofSeconds(30))  // タイムアウトを設定
                        .build();

                LOGGER.info("[Translator]リクエスト送信前: " + url);

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                LOGGER.info("[Translator]レスポンス受信: ステータス=" + response.statusCode());

                String body = response.body();

                LOGGER.info("[Translator]ボディ取得完了: 長さ=" + (body != null ? body.length() : "null"));

//                HttpRequest request = HttpRequest.newBuilder()
//                        .uri(URI.create(url))
//                        .GET()
//                        .build();
//
//                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//                String body = response.body();

                LOGGER.info("[Translator] json: {}", body);

//                String translated = body.split("\"")[1];
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