package com.example.autotranslator;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;

@EventBusSubscriber(modid = "autotranslator", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEvents {
    private static int tickCount = 0;
    private static final int AUTOSAVE_TICKS  = 20 * 300;

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {TranslationCache.save();}

    @SubscribeEvent
    public static void onServerTick(Post event) {
        tickCount++;
        if (tickCount % AUTOSAVE_TICKS == 0) {
            TranslationCache.save();
        }
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {TranslationCache.load();}

}
