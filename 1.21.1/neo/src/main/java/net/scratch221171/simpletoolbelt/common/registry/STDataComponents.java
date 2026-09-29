package net.scratch221171.simpletoolbelt.common.registry;

import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.scratch221171.simpletoolbelt.Const;

public class STDataComponents {
    public static final DeferredRegister<DataComponentType<?>> REGISTRAR =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Const.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> BELT_ID =
            register(Const.ID.DataComponent.BELT_ID, b -> b.persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
            String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return REGISTRAR.register(
                name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
