package com.example.autotranslator;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

public class TranslationManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

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
                // 非公式 Google 翻訳APIの例
                String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=ja&dt=t&q=" + encoded;
                LOGGER.info("[Translator] 翻訳開始: {}", text); // ← ここで確認
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                LOGGER.info("[Translator] json: {}", body); // ← ここで確認
                // レスポンス例: [[[\"こんにちは\",\"Hello\",null,null,1]],null,\"en\"]
                // とりあえず正規表現で最初の翻訳文字列を抽出
                String translated = body.split("\"")[1];

                return translated;
            } catch (Exception e) {
                LOGGER.warn("翻訳中にエラー発生: {}", e.getMessage());
                return text; // 失敗した場合は元の文字列を返す
            }
        });
    }

    /**
     * 翻訳結果をチャットに表示するユーティリティ
     */
    public static void displayTranslated(String original) {
        translateAsync(original).thenAccept(translated -> {
            Minecraft.getInstance().gui.getChat().addMessage(Component.literal(translated));
        });
    }
}