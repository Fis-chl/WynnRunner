/*
 * Copyright © Wynnrunner 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.mixin;

import com.fischl.wynnrunner.commands.WynnrunnerCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.core.consumers.commands.ClientCommandManager;
import com.wynntils.core.consumers.commands.Command;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommandManager.class)
public abstract class ClientCommandManagerMixin {
    @Final
    @Shadow
    private List<Command> commandInstanceSet;

    @Final
    @Shadow
    private CommandDispatcher<CommandSourceStack> clientDispatcher;

    @Shadow
    public abstract void addNode(
            RootCommandNode<SharedSuggestionProvider> root, CommandNode<? extends SharedSuggestionProvider> node);

    @Shadow
    protected abstract void registerCommand(Command command);

    @Inject(method = "registerAllCommands", at = @At("TAIL"))
    public void registerAllWynnrunnerCommands(CallbackInfo ci) {
        registerCommand(new WynnrunnerCommand());
    }
}
