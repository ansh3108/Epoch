package com.example.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.ServerTickManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class StasisCoreItem extends Item {
    public StasisCoreItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            ServerTickManager tickManager = serverWorld.getServer().getTickManager();

            if (user.isSneaking()) {
                if (tickManager.isFrozen()) {
                    tickManager.step();
                    user.sendMessage(Text.literal("Chronos Anchor: +1 Tick Step").withColor(0xFF34D399), true);
                    world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 1.0f, 2.0f);
                } else {
                    user.sendMessage(Text.literal("Error: World must be in Stasis to step ticks.").withColor(0xFFEF4444), true);
                }
            } else {
                boolean isFrozen = tickManager.isFrozen();
                tickManager.setFrozen(!isFrozen);

                if (!isFrozen) {
                    user.sendMessage(Text.literal("Chronos Anchor: GLOBAL STASIS ENGAGED").withColor(0xFF3B82F6), true);
                    world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 0.5f);
                } else {
                    user.sendMessage(Text.literal("Chronos Anchor: Time Flow Restored").withColor(0xFF34D399), true);
                    world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            }
        }

        return ActionResult.SUCCESS;
    }
}
