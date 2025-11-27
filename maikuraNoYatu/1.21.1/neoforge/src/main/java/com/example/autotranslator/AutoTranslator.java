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
    private static boolean sending = true;

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
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(AutoTranslator.class);
        }
    }

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        if (sending) {
            String original = event.getMessage();
            event.setCanceled(true);
            TranslationUtil.translateIfNeededAsync(original).thenAccept(translated -> {
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().player.connection.sendChat(translated);
                    sending = false;
                });

            });
        } else {
            sending = false;
            return;
        }
    }
}