package com.example.mixin;

import com.example.TheGreatMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    @Shadow protected abstract boolean shouldShowDebugHud();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void renderBlueprintHud(DrawContext context, CallbackInfo ci) {
        if (!this.shouldShowDebugHud()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.getCameraEntity() == null) {
            return;
        }

        Identifier fontId = Identifier.of(TheGreatMod.MOD_ID, "hud");
        Style customStyle = Style.EMPTY.withFont(new StyleSpriteSource.Font(fontId));
        
        List<Text> lines = new ArrayList<>();

        lines.add(Text.literal("Stats").setStyle(customStyle));
        lines.add(Text.literal("FPS: " + client.getCurrentFps()).setStyle(customStyle));

        LocalTime now = LocalTime.now();
        String localTime = now.format(DateTimeFormatter.ofPattern("hh:mm a"));
        lines.add(Text.literal("Local Time: " + localTime).setStyle(customStyle));

        long time = client.world.getTimeOfDay() % 24000;
        int hours = (int) ((time / 1000 + 6) % 24);
        int minutes = (int) ((time % 1000) * 60 / 1000);
        lines.add(Text.literal(String.format("Game Time: %02d:%02d", hours, minutes)).setStyle(customStyle));

        Vec3d vel = client.getCameraEntity().getVelocity();
        double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z) * 20.0;
        lines.add(Text.literal(String.format("Speed: %.1f BPS", speed)).setStyle(customStyle));

        Direction dir = client.getCameraEntity().getHorizontalFacing();
        String rawDir = dir.name();
        String dirName = rawDir.substring(0, 1).toUpperCase() + rawDir.substring(1).toLowerCase();
        String axis = dir == Direction.NORTH ? "-Z" : dir == Direction.SOUTH ? "+Z" : dir == Direction.WEST ? "-X" : "+X";
        lines.add(Text.literal("Facing: " + dirName + " (" + axis + ")").setStyle(customStyle));

        BlockPos pos = client.player.getBlockPos();
        String rawBiome = client.world.getBiome(pos).getKey().map(key -> key.getValue().getPath()).orElse("unknown");
        String[] words = rawBiome.replace("_", " ").split(" ");
        StringBuilder formattedBiome = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                formattedBiome.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        lines.add(Text.literal("Biome: " + formattedBiome.toString().trim()).setStyle(customStyle));

        String rawDim = client.world.getRegistryKey().getValue().getPath();
        String[] dimWords = rawDim.replace("_", " ").split(" ");
        StringBuilder formattedDim = new StringBuilder();
        for (String word : dimWords) {
            if (!word.isEmpty()) {
                formattedDim.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        lines.add(Text.literal("Dim: " + formattedDim.toString().trim()).setStyle(customStyle));

        int light = client.world.getLightLevel(pos);
        lines.add(Text.literal("Light: " + light).setStyle(customStyle));

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double cpuLoad = osBean.getCpuLoad();
        String cpuString = cpuLoad >= 0 ? String.format("CPU: %.1f%%", cpuLoad * 100.0) : "CPU: --%";
        lines.add(Text.literal(cpuString).setStyle(customStyle));

        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usedMem = totalMem - freeMem;
        long usedMB = usedMem / 1024L / 1024L;
        long maxMB = maxMem / 1024L / 1024L;
        int memPercent = (int) ((usedMem * 100L) / maxMem);
        lines.add(Text.literal(String.format("Alloc Mem: %d%% (%d / %d MB)", memPercent, usedMB, maxMB)).setStyle(customStyle));

        String gpuName = GL11.glGetString(GL11.GL_RENDERER);
        if (gpuName != null) {
            if (gpuName.contains("/")) {
                gpuName = gpuName.split("/")[0];
            }
            lines.add(Text.literal("GPU: " + gpuName.trim()).setStyle(customStyle));
        }

        int ping = 0;
        if (client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) {
                ping = entry.getLatency();
            }
        }
        lines.add(Text.literal("Ping: " + ping + "ms").setStyle(customStyle));

        int maxWidth = 0;
        for (Text line : lines) {
            int textWidth = client.textRenderer.getWidth(line);
            if (textWidth > maxWidth) {
                maxWidth = textWidth;
            }
        }

        int xOffset = 10;
        int yOffset = 10;
        int panelWidth = maxWidth + 16;
        int lineSpacing = 12;
        int panelHeight = (lines.size() * lineSpacing) + 6;

        int bgDark = 0x88020617;
        int borderTopLeft = 0x44FFFFFF;
        int borderBottomRight = 0x22000000;

        context.fill(xOffset, yOffset, xOffset + panelWidth, yOffset + panelHeight, bgDark);
        context.fill(xOffset, yOffset, xOffset + panelWidth, yOffset + 1, borderTopLeft);
        context.fill(xOffset, yOffset, xOffset + 1, yOffset + panelHeight, borderTopLeft);
        context.fill(xOffset, yOffset + panelHeight - 1, xOffset + panelWidth, yOffset + panelHeight, borderBottomRight);
        context.fill(xOffset + panelWidth - 1, yOffset, xOffset + panelWidth, yOffset + panelHeight, borderBottomRight);

        int titleColor = 0xFFFFFFFF;
        int dataColor = 0xFF34D399; 

        for (int i = 0; i < lines.size(); i++) {
            int color = (i == 0) ? titleColor : dataColor;
            context.drawText(client.textRenderer, lines.get(i), xOffset + 8, yOffset + 8 + (i * lineSpacing), color, false);
        }

        ci.cancel();
    }
}


