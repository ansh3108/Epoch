package com.example.mixin;

import com.example.TheGreatMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void accumulatePassiveTime(CallbackInfo ci) {
        if (((Object) this) instanceof ServerPlayerEntity serverPlayer) {
            Long currentTime = serverPlayer.getAttachedOrCreate(TheGreatMod.PASSIVE_TIME_ATTACHMENT, () -> 0L);
            serverPlayer.setAttached(TheGreatMod.PASSIVE_TIME_ATTACHMENT, currentTime + 1L);
        }
    }
}


