package com.romangraef.betterscaffolding.networking

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs

@Environment(EnvType.CLIENT)
object ServerNetworkingPlayer {
    @JvmStatic
    fun sendForkliftInteraction(forkUp: Boolean, forkDown: Boolean, pickupBlock: Boolean) {
        val interaction = when {
            forkUp > forkDown -> ForkliftInteractions.FORK_UP
            forkDown > forkUp -> ForkliftInteractions.FORK_DOWN
            pickupBlock -> ForkliftInteractions.PICKUP_BLOCK
            else -> null
        } ?: return
        ClientPlayNetworking.send(
            ModNetworkingConstants.INTERACT_FORKLIFT_PACKET_ID,
            PacketByteBufs.create().also { it.writeEnumConstant(interaction) })
    }
}