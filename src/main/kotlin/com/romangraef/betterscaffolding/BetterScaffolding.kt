package com.romangraef.betterscaffolding

import com.romangraef.betterscaffolding.networking.ServerPlayNetworkingHandlers
import com.romangraef.betterscaffolding.registries.BBlock
import com.romangraef.betterscaffolding.registries.BItems
import com.romangraef.betterscaffolding.registries.REntities
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.ItemStack
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

    val config get() = AutoConfig.getConfigHolder(BetterScaffoldingConfig::class.java).config

    override fun onInitialize() {
        logger.info("Loaded better scaffolding")
        registries.forEach { it.registerAll() }
        ServerPlayNetworkingHandlers.registerAll()
        AutoConfig.register(BetterScaffoldingConfig::class.java, ::GsonConfigSerializer)
    }
}


