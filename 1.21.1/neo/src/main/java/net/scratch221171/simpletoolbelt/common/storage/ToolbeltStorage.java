package net.scratch221171.simpletoolbelt.common.storage;

import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;
import net.scratch221171.simpletoolbelt.common.network.SyncBeltContentsPayload;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.config.ServerConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ToolbeltStorage extends SavedData {
    private static final String TAG = "belts";
    private static final String NAME = Const.MOD_ID + "_" + TAG;
    private static final Codec<Map<UUID, ToolbeltContents>> BELTS_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, ToolbeltContents.CODEC);

    private final Map<UUID, ToolbeltContents> belts = new HashMap<>();

    public static ToolbeltStorage get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(ToolbeltStorage::new, ToolbeltStorage::load, null), NAME);
    }

    @Nullable public ToolbeltContents getOrNull(UUID id) {
        return belts.get(id);
    }

    public void set(UUID id, ToolbeltContents contents) {
        belts.put(id, contents);
        setDirty();
    }

    public static void update(ServerPlayer player, UUID id, ToolbeltContents contents) {
        get(player.server).set(id, contents);
        SyncBeltContentsPayload payload = new SyncBeltContentsPayload(id, contents);
        PacketDistributor.sendToPlayer(player, payload);
        for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
            if (other != player
                    && other.containerMenu instanceof ToolbeltMenu menu
                    && menu.getBeltId().equals(id)) {
                PacketDistributor.sendToPlayer(other, payload);
            }
        }
    }

    public ToolbeltContents getOrInit(UUID id, int slots) {
        ToolbeltContents contents = belts.get(id);
        if (contents == null || contents.totalSize() < slots) {
            contents =
                    contents == null ? ToolbeltContents.create(slots) : contents.addSlot(slots - contents.totalSize());
            set(id, contents);
        }
        return contents;
    }

    public ToolbeltContents resolve(ServerPlayer player, UUID id, ItemStack stack) {
        int requiredSize = ToolbeltItem.getRequiredSize(stack);
        ToolbeltContents before = belts.get(id);
        ToolbeltContents updated = getOrInit(id, requiredSize);
        int extra = Math.min(
                (updated.totalSize() / ToolbeltItem.getPageSize(stack)) - 1,
                ServerConfig.Item.MAX_EXTRA_PAGES.getAsInt());
        if (extra > 0) stack.set(STDataComponents.EXTRA_PAGES, extra);
        if (updated != before) {
            PacketDistributor.sendToPlayer(player, new SyncBeltContentsPayload(id, updated));
        }
        return updated;
    }

    /** 初回使用時にUUIDを初期化　ServerOnly */
    public UUID ensureId(ServerPlayer player, ItemStack stack) {
        UUID id = stack.get(STDataComponents.BELT_ID);
        if (id == null) {
            id = UUID.randomUUID();
            stack.set(STDataComponents.BELT_ID, id);
        }
        resolve(player, id, stack);
        return id;
    }

    public Set<UUID> getIds() {
        return Collections.unmodifiableSet(belts.keySet());
    }

    @Override
    public @NonNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        Map<UUID, ToolbeltContents> toSave = new HashMap<>(belts);
        toSave.entrySet().removeIf(e -> e.getValue().isEmpty());
        tag.put(TAG, BELTS_CODEC.encodeStart(ops, toSave).getOrThrow());
        return tag;
    }

    private static ToolbeltStorage load(CompoundTag tag, HolderLookup.Provider registries) {
        ToolbeltStorage storage = new ToolbeltStorage();
        if (tag.contains(TAG)) {
            RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
            BELTS_CODEC
                    .parse(ops, tag.get(TAG))
                    .resultOrPartial(err -> Const.LOGGER.error("Failed to load belts: {}", err))
                    .ifPresent(storage.belts::putAll);
        }
        return storage;
    }
}
