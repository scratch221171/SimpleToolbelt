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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.network.SyncBeltContentsPayload;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import org.jspecify.annotations.NonNull;

public class ToolbeltStorage extends SavedData {
    private static final String TAG = "belts";
    private static final String NAME = Const.MOD_ID + "_" + TAG;
    private static final Codec<Map<UUID, ToolbeltContents>> BELTS_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, ToolbeltContents.CODEC);

    private static final ToolbeltContents EMPTY = ToolbeltContents.DEFAULT;

    private final Map<UUID, ToolbeltContents> belts = new HashMap<>();

    public static ToolbeltStorage get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(ToolbeltStorage::new, ToolbeltStorage::load, null), NAME);
    }

    public ToolbeltContents get(UUID id) {
        return belts.getOrDefault(id, EMPTY);
    }

    public void set(UUID id, ToolbeltContents contents) {
        belts.put(id, contents);
        setDirty();
    }

    public static void update(ServerPlayer player, UUID id, ToolbeltContents contents) {
        Const.LOGGER.info(
                "Sending belt sync: player={}, id={}, contents={}",
                player.getGameProfile().getName(),
                id,
                contents);
        get(player.server).set(id, contents);
        PacketDistributor.sendToPlayer(player, new SyncBeltContentsPayload(id, contents));
    }

    /** 初回使用時にUUIDを初期化　ServerOnly */
    public static UUID ensureId(ItemStack stack) {
        UUID id = stack.get(STDataComponents.BELT_ID.get());
        if (id == null) {
            id = UUID.randomUUID();
            stack.set(STDataComponents.BELT_ID.get(), id);
        }
        return id;
    }

    public void cleanUpData() {
        belts.entrySet().removeIf(entry -> ToolbeltContents.isEmpty(entry.getValue()));
    }

    public static boolean playerHasBelt(Player player, UUID id) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(STItems.TOOLBELT.get()) && id.equals(s.get(STDataComponents.BELT_ID.get()))) {
                return true;
            }
        }
        return false;
    }

    public Set<UUID> getIds() {
        return Collections.unmodifiableSet(belts.keySet());
    }

    public boolean contains(UUID id) {
        return belts.containsKey(id);
    }

    @Override
    public @NonNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        cleanUpData();
        tag.put(TAG, BELTS_CODEC.encodeStart(ops, belts).getOrThrow());
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
