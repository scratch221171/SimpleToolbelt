package net.scratch221171.simpletoolbelt.common.storage;

import com.mojang.serialization.Codec;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.network.SyncBeltContentsPayload;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.compat.curios.STCuriosHelper;
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
        PacketDistributor.sendToPlayer(player, new SyncBeltContentsPayload(id, contents));
    }

    public ToolbeltContents getOrInit(UUID id, int pages) {
        ToolbeltContents cur = belts.get(id);
        if (cur == null || cur.pagesSize() < pages) {
            cur = (cur == null ? ToolbeltContents.EMPTY : cur).increasePageTo(pages);
            set(id, cur);
        }
        return cur;
    }

    public ToolbeltContents resolve(ServerPlayer player, UUID id, ItemStack stack) {
        int capacity = stack.getItem() instanceof ToolbeltItem t ? t.getPageCount(stack) : 0;
        ToolbeltContents before = belts.get(id);
        ToolbeltContents cur = getOrInit(id, capacity);
        if (cur != before) {
            PacketDistributor.sendToPlayer(player, new SyncBeltContentsPayload(id, cur));
        }
        return cur;
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

    public static Optional<ItemStack> findAnyBelt(Player player) {
        return find(player, () -> STCuriosHelper.getBeltInCurios(player), stack -> true);
    }

    public static Optional<ItemStack> findBelt(Player player, UUID id) {
        return find(
                player,
                () -> STCuriosHelper.getBeltInCurios(player, id),
                stack -> id.equals(stack.get(STDataComponents.BELT_ID)));
    }

    private static Optional<ItemStack> find(
            Player player, Supplier<Optional<ItemStack>> inCurios, Predicate<ItemStack> filter) {
        Optional<ItemStack> curios = ModList.get().isLoaded("curios") ? inCurios.get() : Optional.empty();
        return curios.or(() -> {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.getItem() instanceof ToolbeltItem && filter.test(stack)) {
                    return Optional.of(stack);
                }
            }
            return Optional.empty();
        });
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
