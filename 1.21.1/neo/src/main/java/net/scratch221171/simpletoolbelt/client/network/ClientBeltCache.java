package net.scratch221171.simpletoolbelt.client.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.network.RequestBeltContentsPayload;
import net.scratch221171.simpletoolbelt.common.network.SyncBeltContentsPayload;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;

// client/ClientBeltCache.java
public final class ClientBeltCache {
    private static final ToolbeltContents EMPTY = ToolbeltContents.DEFAULT;
    private static final Map<UUID, ToolbeltContents> CACHE = new HashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST = new HashMap<>();
    private static final int REQUEST_INTERVAL_TICKS = 40;

    public static void handleSync(SyncBeltContentsPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Const.LOGGER.info("Received belt sync: id={}, contents={}", payload.id(), payload.contents());
            CACHE.put(payload.id(), payload.contents());
        });
    }

    public static ToolbeltContents read(ItemStack stack) {
        UUID id = stack.get(STDataComponents.BELT_ID.get());
        return id == null ? EMPTY : read(id);
    }

    /** client用キャッシュ */
    public static ToolbeltContents read(UUID id) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            long now = level.getGameTime();
            Long last = LAST_REQUEST.get(id);
            if (last == null || now - last >= REQUEST_INTERVAL_TICKS) {
                LAST_REQUEST.put(id, now);
                PacketDistributor.sendToServer(new RequestBeltContentsPayload(id));
            }
        }
        return CACHE.getOrDefault(id, EMPTY);
    }

    public static void clear() {
        CACHE.clear();
        LAST_REQUEST.clear();
    }
}
