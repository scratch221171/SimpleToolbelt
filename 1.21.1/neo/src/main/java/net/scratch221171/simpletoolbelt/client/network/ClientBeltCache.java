package net.scratch221171.simpletoolbelt.client.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;
import net.scratch221171.simpletoolbelt.common.network.RequestBeltContentsPayload;
import net.scratch221171.simpletoolbelt.common.network.SyncBeltContentsPayload;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;

public final class ClientBeltCache {
    private static final Map<UUID, ToolbeltContents> CACHE = new HashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST = new HashMap<>();
    private static final int REQUEST_INTERVAL_TICKS = 40;

    public static void handleSync(SyncBeltContentsPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            CACHE.put(payload.id(), payload.contents());
            if (ctx.player().containerMenu instanceof ToolbeltMenu menu
                    && menu.getBeltId().equals(payload.id())) {
                menu.applyClientSync(payload.contents());
            }
        });
    }

    public static Optional<ToolbeltContents> read(ItemStack stack) {
        return Optional.ofNullable(stack.get(STDataComponents.BELT_ID)).flatMap(ClientBeltCache::read);
    }

    /** client用キャッシュ */
    public static Optional<ToolbeltContents> read(UUID id) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            long now = level.getGameTime();
            Long last = LAST_REQUEST.get(id);
            if (last == null || now - last >= REQUEST_INTERVAL_TICKS) {
                LAST_REQUEST.put(id, now);
                PacketDistributor.sendToServer(new RequestBeltContentsPayload(id));
            }
        }
        return Optional.ofNullable(CACHE.get(id));
    }

    public static void clear() {
        CACHE.clear();
        LAST_REQUEST.clear();
    }
}
