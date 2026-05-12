package com.example.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    @Shadow protected abstract boolean shouldShowDebugHud();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void renderBlueprintHud(DrawContext context, CallbackInfo ci) {
        if (!this.shouldShowDebugHud()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        int xOffset = 10;
        int yOffset = 10;
        int panelWidth = 160;
        int panelHeight = 58;

        int bgDark = 0x770F172A;
        int borderTopLeft = 0x55FFFFFF;
        int borderBottomRight = 0x33000000;

        context.fill(xOffset, yOffset, xOffset + panelWidth, yOffset + panelHeight, bgDark);
        
        context.fill(xOffset, yOffset, xOffset + panelWidth, yOffset + 1, borderTopLeft);
        context.fill(xOffset, yOffset, xOffset + 1, yOffset + panelHeight, borderTopLeft);
        
        context.fill(xOffset, yOffset + panelHeight - 1, xOffset + panelWidth, yOffset + panelHeight, borderBottomRight);
        context.fill(xOffset + panelWidth - 1, yOffset, xOffset + panelWidth, yOffset + panelHeight, borderBottomRight);

        int titleColor = 0xFFFFFF;
        int dataColor = 0x38BDF8;

        context.drawText(client.textRenderer, "Stats", xOffset + 8, yOffset + 8, titleColor, true);

        String fps = "FPS: " + client.getCurrentFps();
        context.drawText(client.textRenderer, fps, xOffset + 8, yOffset + 22, dataColor, true);

        if (client.getCameraEntity() != null) {
            String coords = String.format("XYZ: %.1f / %.1f / %.1f", 
                client.getCameraEntity().getX(), 
                client.getCameraEntity().getY(), 
                client.getCameraEntity().getZ());
            context.drawText(client.textRenderer, coords, xOffset + 8, yOffset + 34, dataColor, true);
        }

        BlockPos pos = client.player.getBlockPos();
        String rawBiome = client.world.getBiome(pos).getKey().map(key -> key.getValue().getPath()).orElse("unknown");
        
        String[] words = rawBiome.replace("_", " ").split(" ");
        StringBuilder formattedBiome = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                formattedBiome.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        
        String biomeText = "Biome: " + formattedBiome.toString().trim();
        context.drawText(client.textRenderer, biomeText, xOffset + 8, yOffset + 46, dataColor, true);

        ci.cancel();
    }
}


