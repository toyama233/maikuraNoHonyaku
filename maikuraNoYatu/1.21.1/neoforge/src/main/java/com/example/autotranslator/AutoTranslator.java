package com.example.autotranslator;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod("autotranslator")
public class AutoTranslator {
    private static boolean sending = true;
    private static final Logger LOGGER = LogUtils.getLogger();

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
    public AutoTranslator(IEventBus modBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(AutoTranslator.class);
        }
        modBus.addListener(AutoTranslator::onCommonSetup);
    }

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        if (sending) {
            sending = false;
            event.setCanceled(true);
            String original = event.getMessage();
            TranslationCache.translateText(original).thenAccept(translated -> {
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().player.connection.sendChat(translated);
                });
            });
        } else {
            sending = true;
        }
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        TranslationCache.load();
    }
}