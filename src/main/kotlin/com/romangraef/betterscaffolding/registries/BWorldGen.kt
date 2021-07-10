package com.romangraef.betterscaffolding.registries

import com.romangraef.betterscaffolding.BetterScaffolding
import com.romangraef.betterscaffolding.worldgen.SingleScaffoldingFeature
import com.romangraef.betterscaffolding.worldgen.UndeadConstructionSiteFeature
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder
import net.minecraft.util.registry.Registry
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.chunk.StructureConfig
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.FeatureConfig

object BWorldGen : DefaultDelayedRegistry<Feature<*>>(Registry.FEATURE, BetterScaffolding.modid) {
    val singleScaffolding by "single_scaffolding" {
        SingleScaffoldingFeature()
    }

}