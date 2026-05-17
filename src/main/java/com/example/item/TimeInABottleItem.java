package com.example.item;

import com.example.TheGreatMod;
import com.example.engine.ChronosEngine;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class TimeInABottleItem extends Item {
    public TimeInABottleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || context.getWorld().isClient()) {
            return ActionResult.SUCCESS;
        }

        ServerWorld world = (ServerWorld) context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        BlockEntity entity = world.getBlockEntity(pos);

        if (entity == null && !state.hasRandomTicks()) {
            return ActionResult.PASS;
        }

        long storedTicks = player.getAttachedOrCreate(TheGreatMod.PASSIVE_TIME_ATTACHMENT, () -> 0L);
        long baseCost = 600L; 
        int currentMultiplier = ChronosEngine.getMultiplier(world, pos);
        long cost = currentMultiplier == 1 ? baseCost : baseCost * (currentMultiplier / 2);

        if (storedTicks >= cost) {
            player.setAttached(TheGreatMod.PASSIVE_TIME_ATTACHMENT, storedTicks - cost);
            ChronosEngine.addAccelerator(world, pos);
            
            int newMultiplier = ChronosEngine.getMultiplier(world, pos);
            player.sendMessage(Text.literal("Chronos Matrix: " + newMultiplier + "x Speed").withColor(0xFF34D399), true);
            
            float pitch = 1.0f + (newMultiplier * 0.05f);
            world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0f, pitch);
            
            return ActionResult.SUCCESS;
        } else {
            player.sendMessage(Text.literal("Insufficient Passive Time. Requires: " + cost + " Ticks").withColor(0xFFEF4444), true);
            world.playSound(null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.PLAYERS, 0.5f, 0.8f);
            return ActionResult.FAIL;
        }
    }
}