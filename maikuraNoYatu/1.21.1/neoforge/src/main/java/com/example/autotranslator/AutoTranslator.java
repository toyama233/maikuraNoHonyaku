package com.example.autotranslator;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod("autotranslator")
public class AutoTranslator {

//    @SubscribeEvent
//    public static void onChat(ClientChatReceivedEvent event) {
//        // チャットメッセージの取得
//        Component message = event.getMessage();
//        String originalText = message.getString();
//
//        // 翻訳処理（あなたのクラス）
//        String translated = TranslationManager.translate(originalText);
//
//        // イベントの置き換えは不可能になったため、自前で再送信
//        event.setCanceled(true); // 元のメッセージをキャンセル
//        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(translated));
//    }
    public AutoTranslator() {
        // クライアント専用のイベントバスへ登録
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(AutoTranslator.class);
        }
    }
    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String original = event.getMessage();

        if(original.startsWith("翻訳済：")) {
            return;
        }
        // まずキャンセルして、翻訳が終わってから送信
        event.setCanceled(true);

        TranslationManager.translateAsync(original).thenAccept(translated -> {
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().player.connection.sendChat("翻訳済："+translated);
            });
        });
    }
}