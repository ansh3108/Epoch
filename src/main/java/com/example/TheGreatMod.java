package com.example;

import com.example.engine.ChronosEngine;
import com.example.item.StasisCoreItem;
import com.example.item.TimeInABottleItem;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TheGreatMod implements ModInitializer {
    public static final String MOD_ID = "the-great-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final AttachmentType<Long> PASSIVE_TIME_ATTACHMENT = AttachmentRegistry.createPersistent(
            Identifier.of(MOD_ID, "passive_time"),
            Codec.LONG
    );

    public static final RegistryKey<Item> BOTTLE_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "time_in_a_bottle"));
    public static final RegistryKey<Item> ANCHOR_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "stasis_core"));

    public static final Item TIME_IN_A_BOTTLE = new TimeInABottleItem(new Item.Settings()
            .registryKey(BOTTLE_KEY)
            .maxCount(1)
            .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.literal("Temporal Capacitor").formatted(Formatting.AQUA),
                    Text.literal("Passively absorbs ambient server ticks.").formatted(Formatting.DARK_GRAY),
                    Text.empty(),
                    Text.literal("Sneak + Right-Click").formatted(Formatting.WHITE).append(Text.literal(" on a block to").formatted(Formatting.GRAY)),
                    Text.literal("overclock its tick rate.").formatted(Formatting.GRAY)
            )))
    );

    public static final Item STASIS_CORE = new StasisCoreItem(new Item.Settings()
            .registryKey(ANCHOR_KEY)
            .maxCount(1)
            .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.literal("Global Stasis Anchor").formatted(Formatting.BLUE),
                    Text.literal("Intercepts the master server clock.").formatted(Formatting.DARK_GRAY),
                    Text.empty(),
                    Text.literal("Right-Click").formatted(Formatting.WHITE).append(Text.literal(" to toggle global stasis.").formatted(Formatting.GRAY)),
                    Text.literal("Sneak + Right-Click").formatted(Formatting.WHITE).append(Text.literal(" to advance by 1 tick.").formatted(Formatting.GRAY))
            )))
    );

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, BOTTLE_KEY.getValue(), TIME_IN_A_BOTTLE);
        Registry.register(Registries.ITEM, ANCHOR_KEY.getValue(), STASIS_CORE);
        
        ChronosEngine.initialize();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                boolean hasBottle = false;
                for (int i = 0; i < player.getInventory().size(); i++) {
                    if (player.getInventory().getStack(i).isOf(TIME_IN_A_BOTTLE)) {
                        hasBottle = true;
                        break;
                    }
                }

                if (hasBottle) {
                    long currentTicks = player.getAttachedOrCreate(PASSIVE_TIME_ATTACHMENT, () -> 0L);
                    if (currentTicks < 1728000L) {
                        player.setAttached(PASSIVE_TIME_ATTACHMENT, currentTicks + 1);
                    }
                }
            }
        });

        LOGGER.info("Chronos Engine booted up.");
    }
}
