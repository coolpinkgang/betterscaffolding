package com.romangraef.betterscaffolding

import com.romangraef.betterscaffolding.registries.BBlock
import com.romangraef.betterscaffolding.registries.BItems
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import java.util.logging.Logger

object BetterScaffolding : ModInitializer {
    const val modid: String = "betterscaffolding"
    val logger = Logger.getLogger(modid)

    val registries = listOf(
        BItems, BBlock
    )

    fun id(string: String) = Identifier(modid, string)

    val itemGroup = FabricItemGroupBuilder.build(id("general")) { ItemStack(BItems.pole) }

    override fun onInitialize() {
        logger.info("Loaded better scaffolding")
        registries.forEach { it.registerAll() }
    }
}


