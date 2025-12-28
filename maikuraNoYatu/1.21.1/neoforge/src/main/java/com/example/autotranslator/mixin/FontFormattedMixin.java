package com.example.autotranslator.mixin;

import com.example.autotranslator.FormattedTextUtil;
import com.example.autotranslator.ModClassifierUtil;
import com.example.autotranslator.ScreenContext;
import com.example.autotranslator.debug.TranslationDebugLogger;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(Font.class)
public class FontFormattedMixin {
    private static final Set<String> loggedTexts = new HashSet<>();
    private final int maxCount = 5;

    @Inject(
            method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
            at = @At("HEAD"),
            remap = false
    )
    private void onDrawFormatted(
            FormattedCharSequence seq,
            float x, float y, int color, boolean shadow,
            Matrix4f matrix, MultiBufferSource buffer,
            Font.DisplayMode mode, int light, int overlay,
            CallbackInfoReturnable<Integer> cir
    ) {

        // ① 文字を復元
        String text = FormattedTextUtil.restore(seq);
        if (text == null || text.trim().isEmpty()) {
            return; // 空文字は無視
        }

        // ② スタックトレースから MOD を判定（最大5個取得）
        List<ModClassifierUtil.ModInfo> modList = ModClassifierUtil.findTranslatableMods(maxCount);

        if (modList.isEmpty()) {
            // ブラックリストに該当 or 見つからなかった
            return;
        }

        // ③ ログ出力（同じ文字列は1回だけ）
        // 最初のMODをキーとして使用
        ModClassifierUtil.ModInfo primaryMod = modList.get(0);
        String logKey = primaryMod.modPackage + ":" + text;

        if (!loggedTexts.contains(logKey)) {
            loggedTexts.add(logKey);

            // ログ出力（複数のMOD情報を表示）
            StringBuilder logBuilder = new StringBuilder();
//            logBuilder.append("[AutoTranslator] ");
            logBuilder.append(text);
            logBuilder.append("\n  └─ Stack: ");
            final Logger LOGGER = LoggerFactory.getLogger("AutoTranslator");
            for (int i = 0; i < modList.size(); i++) {
                if (i > 0) {
                    logBuilder.append(" → ");
                }
                logBuilder.append(modList.get(i).fullClassName);
                logBuilder.append(".");
                logBuilder.append(modList.get(i).methodName);
                logBuilder.append("()");
                if( i % 3 == 0){
                    logBuilder.append("\n");
                }
            }
            LOGGER.info(logBuilder.toString());
        }
    }
}
