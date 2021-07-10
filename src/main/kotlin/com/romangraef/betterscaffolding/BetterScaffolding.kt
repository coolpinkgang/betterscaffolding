package com.romangraef.betterscaffolding

import com.romangraef.betterscaffolding.commands.ForkliftCommand
import com.romangraef.betterscaffolding.networking.ServerPlayNetworkingHandlers
import com.romangraef.betterscaffolding.registries.*
import com.romangraef.betterscaffolding.worldgen.UndeadConstructionSiteFeature
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.item.ItemStack
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.gen.GenerationStep
import org.slf4j.LoggerFactory

object BetterScaffolding : ModInitializer {
    const val modid: String = "betterscaffolding"
    val logger = LoggerFactory.getLogger(BetterScaffolding::class.java)

    val registries = listOf(
        BItems, BBlock, BEntities, BWorldGen, BSounds, BConfiguredWorldGen
    )

    fun id(string: String) = Identifier(modid, string)

    fun error(name: String, vararg args: Any) = TranslatableText("betterscaffolding.error.$name", args).styled {
        it.withColor(Formatting.DARK_RED)
    }

    val itemGroup = FabricItemGroupBuilder.build(id("general")) { ItemStack(BItems.pole) }

    val config get() = AutoConfig.getConfigHolder(BetterScaffoldingConfig::class.java).config

    override fun onInitialize() {
        logger.info("Loaded better scaffolding")
        registries.forEach { it.registerAll() }
        ServerPlayNetworkingHandlers.registerAll()
        AutoConfig.register(BetterScaffoldingConfig::class.java, ::GsonConfigSerializer)
        CommandRegistrationCallback.EVENT.register(ForkliftCommand::register)
        VillagerTrades.registerTrades()
        BiomeModifications.addFeature(
            BiomeSelectors.all(),
            GenerationStep.Feature.SURFACE_STRUCTURES,
            BConfiguredWorldGen.keys[BConfiguredWorldGen.singleScaffolding]
        )
        UndeadConstructionSiteFeature.register()
    }
}


