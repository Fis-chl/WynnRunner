/*
 * Copyright © Wynnrunner 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.mixin;

import com.fischl.wynnrunner.commands.WynnrunnerCommand;
import com.wynntils.core.consumers.commands.ClientCommandManager;
import com.wynntils.core.consumers.commands.Command;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommandManager.class)
public abstract class ClientCommandManagerMixin {
    @Shadow
    protected abstract void registerCommand(Command command);

    @Inject(method = "registerAllCommands", at = @At("TAIL"))
    public void registerAllWynnrunnerCommands(CallbackInfo ci) {
        registerCommand(new WynnrunnerCommand());
    }
}
