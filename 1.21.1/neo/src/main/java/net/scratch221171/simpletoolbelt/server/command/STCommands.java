package net.scratch221171.simpletoolbelt.server.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltStorage;

// common/STCommands.java
@EventBusSubscriber(modid = Const.MOD_ID)
public class STCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(Commands.literal(Const.MOD_ID)
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("list").executes(STCommands::listBelts))
                        .then(Commands.literal("restore")
                                .then(Commands.argument("uuid", UuidArgument.uuid())
                                        .suggests(STCommands::suggestBeltIds)
                                        .executes(STCommands::restoreBelt))));
    }

    private static int listBelts(CommandContext<CommandSourceStack> ctx) {
        ToolbeltStorage storage = ToolbeltStorage.get(ctx.getSource().getServer());
        Set<UUID> ids = storage.getIds();

        if (ids.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(Const.LangKey.Commands.NOT_FOUND));
            return 0;
        }
        ctx.getSource()
                .sendSuccess(() -> Component.translatable(Const.LangKey.Commands.PRINT_ALL, ids.toString()), false);
        return ids.size();
    }

    private static int restoreBelt(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        UUID id = UuidArgument.getUuid(ctx, "uuid");
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.translatable("argument.entity.notfound.player"));
            return 0;
        }

        ToolbeltContents contents =
                ToolbeltStorage.get(ctx.getSource().getServer()).getOrNull(id);
        if (contents == null) {
            ctx.getSource().sendFailure(Component.translatable(Const.LangKey.Commands.NOT_FOUND));
            return 0;
        } else {
            ItemStack stack =
                    new ItemStack((contents.pagesSize() > 1 ? STItems.NETHERITE_TOOLBELT : STItems.TOOLBELT).get());
            stack.set(STDataComponents.BELT_ID.get(), id);
            boolean added = player.getInventory().add(stack);
            if (!added) {
                player.drop(stack, false);
            }

            ctx.getSource()
                    .sendSuccess(() -> Component.translatable(Const.LangKey.Commands.RESTORED, id.toString()), true);
            return 1;
        }
    }

    private static CompletableFuture<Suggestions> suggestBeltIds(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {

        ToolbeltStorage.get(ctx.getSource().getServer()).getIds().forEach(id -> builder.suggest(id.toString()));
        return builder.buildFuture();
    }
}
