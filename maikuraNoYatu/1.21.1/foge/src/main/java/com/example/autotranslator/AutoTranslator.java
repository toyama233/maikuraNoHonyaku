package com.example.autotranslator;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "autotranslator", value = Dist.CLIENT)
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
    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String original = event.getMessage();

        // まずキャンセルして、翻訳が終わってから送信
        event.setCanceled(true);

        TranslationManager.translateAsync(original).thenAccept(translated -> {
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().player.connection.sendChat(translated);
            });
        });
    }
}