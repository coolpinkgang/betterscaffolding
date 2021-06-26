package com.romangraef.betterscaffolding.networking

import com.romangraef.betterscaffolding.entities.ForkliftEntity
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper

object ServerPlayNetworkingHandlers {

    private fun handle(
        identifier: Identifier,
        packet: ServerPlayNetworking.PlayChannelHandler
    ) {
        handlers[identifier] = packet
    }

    fun registerAll() {
        handlers.forEach { (id, act) ->
            ServerPlayNetworking.registerGlobalReceiver(id, act)
        }
    }

    val handlers: MutableMap<Identifier, ServerPlayNetworking.PlayChannelHandler> = mutableMapOf()

    init {
        handle(ModNetworkingConstants.INTERACT_FORKLIFT_PACKET_ID) { minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender ->
            val forklift = serverPlayerEntity.vehicle as? ForkliftEntity ?: return@handle
            when (packetByteBuf.readEnumConstant(ForkliftInteractions::class.java)) {
                ForkliftInteractions.FORK_UP -> forklift.forkHeight =
                    MathHelper.clamp(forklift.forkHeight + 0.05F, 0F, 1F)
                ForkliftInteractions.FORK_DOWN -> forklift.forkHeight =
                    MathHelper.clamp(forklift.forkHeight - 0.05F, 0F, 1F)
                else -> Unit // TODO
            }
            Unit
        }
    }
}