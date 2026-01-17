package com.example.autotranslator.mixin;

import com.example.autotranslator.FormattedTextUtil;
import com.example.autotranslator.ModClassifierUtil;
import com.example.autotranslator.TranslationCache;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.autotranslator.debug.TranslationDebugLogger.LogStackTrace;

@Mixin(Font.class)
public class FontFormattedMixin {
    private static final Set<String> loggedTexts = new HashSet<>();
    private final int maxCount = 5;
    private static final Set<String> pendingTranslations = ConcurrentHashMap.newKeySet();

    @ModifyVariable(
            method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false
    )
//    private void onDrawFormatted(
//            FormattedCharSequence seq,
//            float x, float y, int color, boolean shadow,
//            Matrix4f matrix, MultiBufferSource buffer,
//            Font.DisplayMode mode, int light, int overlay,
//            CallbackInfoReturnable<Integer> cir
//    ) {
    private FormattedCharSequence modifySequence(FormattedCharSequence original) {

        // ① 文字を復元
//        String text = FormattedTextUtil.restore(seq);
        String text = FormattedTextUtil.restore(original);
        if (text == null || text.trim().isEmpty()) {
            return original;
        }

        // ② スタックトレースから MOD を判定
        List<ModClassifierUtil.ModInfo> modList = ModClassifierUtil.findTranslatableMods(maxCount);

        if (modList.isEmpty()) {
            return original;
        }

        String translated = TranslationCache.get(text);
        if (translated != null) {
            return FormattedCharSequence.forward(translated, Style.EMPTY);
        }

//        if (pendingTranslations.add(text)) {
//            TranslationCache.translateText(text).thenAccept(result -> {
//                TranslationCache.put(text, result);
//                pendingTranslations.remove(text);
//            });
//        }
        LogStackTrace(text, modList);
        return original;
    }
}
