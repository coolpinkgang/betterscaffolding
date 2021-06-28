package com.romangraef.betterscaffolding.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.context.CommandContext
import com.romangraef.betterscaffolding.BetterScaffolding
import com.romangraef.betterscaffolding.items.ForkliftCertification
import com.romangraef.betterscaffolding.registries.BItems
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

object ForkliftCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>, dedicated: Boolean) {
        val forklift = literal<ServerCommandSource>("forklift")
        forklift.then(certifySubcommand())

        dispatcher.register(forklift)
    }

    fun certifySubcommand() = literal<ServerCommandSource>("certify")
        .requires { it.hasPermissionLevel(2) }
        .executes {
            forkliftCertify(it, null)
        }
        .then(argument("target", EntityArgumentType.player())
            .executes {
                forkliftCertify(
                    it,
                    EntityArgumentType.getPlayer(it, "target")
                )
            })

    private fun forkliftCertify(
        ctx: CommandContext<ServerCommandSource>,
        forWhom: ServerPlayerEntity?,
    ): Int {
        val source = ctx.source.entity as? ServerPlayerEntity
        if (source == null) {
            ctx.source.sendError(BetterScaffolding.error("notcertifiable"))
            return 0
        }
        val itemStack = BItems.license.createLicense(forWhom ?: source)
        source.giveItemStack(itemStack)
        return 1
    }
}