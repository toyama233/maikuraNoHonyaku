package com.example.autotranslator;

import net.minecraft.client.gui.screens.Screen;

public final class ScreenContext {

    private static volatile String currentScreen = "UNKNOWN";

    public static void set(Screen screen) {
        if (screen != null) {
            currentScreen = screen.getClass().getName();
        }
    }

    public static String get() {
        return currentScreen;
    }
}