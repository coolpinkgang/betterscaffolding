package com.romangraef.betterscaffolding

import com.romangraef.betterscaffolding.networking.ModNetworkingConstants
import com.romangraef.betterscaffolding.networking.ServerPlayNetworkingHandlers
import com.romangraef.betterscaffolding.registries.BBlock
import com.romangraef.betterscaffolding.registries.BItems
import com.romangraef.betterscaffolding.registries.REntities
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.mob.MobEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object BetterScaffolding : ModInitializer {
    const val modid: String = "betterscaffolding"
    val logger = LoggerFactory.getLogger(BetterScaffolding::class.java)

    val registries = listOf(
        BItems, BBlock, REntities
    )

    fun id(string: String) = Identifier(modid, string)

    val itemGroup = FabricItemGroupBuilder.build(id("general")) { ItemStack(BItems.pole) }

    override fun onInitialize() {
        logger.info("Loaded better scaffolding")
        registries.forEach { it.registerAll() }
        ServerPlayNetworkingHandlers.registerAll()
    }
}


