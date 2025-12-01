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
    private static int tickCount = 0;
    private final int AUTOSAVE_TICKS  = 20 * 300;

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
            sending = false;
            event.setCanceled(true);
            String original = event.getMessage();
            String translated = TranslationCache.translateText(original);

            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().player.connection.sendChat(translated);
            });
        }else{
            sending = true;
        }
        return;
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {TranslationCache.load();}

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {TranslationCache.save();}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        tickCount++;
        if (tickCount % (AUTOSAVE_TICKS) == 0) {
            TranslationCache.save();
        }
    }
}