package net.scratch221171.simpletoolbelt.common.registry;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.scratch221171.simpletoolbelt.Const;

public final class STAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTRAR =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Const.MOD_ID);

    public static final Supplier<AttachmentType<Boolean>> AUTO_TOOL_SWITCH =
            REGISTRAR.register(Const.ID.Attachment.AUTO_TOOL_SWITCH, () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
