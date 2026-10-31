package net.scratch221171.simpletoolbelt.client.keymap;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.core.UUIDUtil;
import net.neoforged.fml.loading.FMLPaths;
import net.scratch221171.simpletoolbelt.Const;

public final class ToolbeltKeyMap {
    public record SlotRef(UUID beltId, int slot) {
        public static final Codec<SlotRef> CODEC = RecordCodecBuilder.create(i -> i.group(
                        UUIDUtil.STRING_CODEC.fieldOf("belt").forGetter(SlotRef::beltId),
                        Codec.INT.fieldOf("slot").forGetter(SlotRef::slot))
                .apply(i, SlotRef::new));
    }

    private static final Codec<Map<String, SlotRef>> CODEC = Codec.unboundedMap(Codec.STRING, SlotRef.CODEC);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, SlotRef> BINDINGS = new LinkedHashMap<>();
    private static boolean loaded = false;

    private ToolbeltKeyMap() {}

    public static Optional<SlotRef> get(InputConstants.Key key) {
        ensureLoaded();
        return Optional.ofNullable(BINDINGS.get(key.getName()));
    }

    public static Optional<InputConstants.Key> getKey(UUID beltId, int slot) {
        ensureLoaded();
        SlotRef target = new SlotRef(beltId, slot);
        for (Map.Entry<String, SlotRef> e : BINDINGS.entrySet()) {
            if (e.getValue().equals(target)) {
                return Optional.of(InputConstants.getKey(e.getKey()));
            }
        }
        return Optional.empty();
    }

    public static void bind(InputConstants.Key key, UUID beltId, int slot) {
        ensureLoaded();
        SlotRef ref = new SlotRef(beltId, slot);
        BINDINGS.values().removeIf(ref::equals);
        BINDINGS.put(key.getName(), ref);
        save();
    }

    public static void unbind(UUID beltId, int slot) {
        ensureLoaded();
        if (BINDINGS.values().removeIf(new SlotRef(beltId, slot)::equals)) {
            save();
        }
    }

    private static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve(Const.MOD_ID + "-keymap.json");
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        Path p = path();
        if (!Files.exists(p)) return;
        try (Reader r = Files.newBufferedReader(p)) {
            CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(r))
                    .resultOrPartial(err -> Const.LOGGER.error("Failed to load keymap: {}", err))
                    .ifPresent(BINDINGS::putAll);
        } catch (IOException | JsonParseException e) {
            Const.LOGGER.error("Failed to read keymap", e);
        }
    }

    private static void save() {
        try {
            JsonElement json = CODEC.encodeStart(JsonOps.INSTANCE, BINDINGS).getOrThrow();
            Files.writeString(path(), GSON.toJson(json));
        } catch (IOException e) {
            Const.LOGGER.error("Failed to save keymap", e);
        }
    }
}
