package com.example.autotranslator;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class FormattedTextUtil {
    public static String restore(FormattedCharSequence seq) {
        StringBuilder sb = new StringBuilder();

        seq.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });

        return sb.toString();
    }
    public static FormattedCharSequence create(String text, Style style) {
        return FormattedCharSequence.forward(text, style);
    }
}
