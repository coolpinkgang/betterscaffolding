package com.romangraef.betterscaffolding.registries

import com.romangraef.betterscaffolding.BetterScaffolding
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.world.gen.decorator.ChanceDecoratorConfig
import net.minecraft.world.gen.decorator.Decorator
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.FeatureConfig

object BConfiguredWorldGen :
    DefaultDelayedRegistry<ConfiguredFeature<*, *>>(BuiltinRegistries.CONFIGURED_FEATURE, BetterScaffolding.modid) {
    val singleScaffolding by "single_scaffolding"{
        BWorldGen.singleScaffolding.configure(FeatureConfig.DEFAULT)
            .decorate(Decorator.CHANCE.configure(ChanceDecoratorConfig(5)))
    }
}