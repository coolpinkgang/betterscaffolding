package com.romangraef.betterscaffolding.worldgen

import com.romangraef.betterscaffolding.BetterScaffolding
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.biome.v1.ModificationPhase
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder
import net.minecraft.structure.PoolStructurePiece
import net.minecraft.structure.StructureManager
import net.minecraft.structure.StructureStart
import net.minecraft.structure.pool.StructurePoolBasedGenerator
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.DynamicRegistryManager
import net.minecraft.util.registry.Registry
import net.minecraft.world.HeightLimitView
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.source.BiomeSource
import net.minecraft.world.gen.ChunkRandom
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.chunk.StructureConfig
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.FeatureConfig
import net.minecraft.world.gen.feature.StructureFeature
import net.minecraft.world.gen.feature.StructureFeature.StructureStartFactory
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig
import java.util.function.Supplier

class UndeadConstructionSiteFeature : StructureFeature<DefaultFeatureConfig>(DefaultFeatureConfig.CODEC) {
    override fun shouldStartAt(
        chunkGenerator: ChunkGenerator?,
        biomeSource: BiomeSource?,
        worldSeed: Long,
        random: ChunkRandom?,
        pos: ChunkPos?,
        biome: Biome?,
        chunkPos: ChunkPos?,
        config: DefaultFeatureConfig?,
        world: HeightLimitView?
    ): Boolean {
        return true
    }

    companion object {
        val INSTANCE = UndeadConstructionSiteFeature()
        val CONFIGURED = INSTANCE.configure(FeatureConfig.DEFAULT)
        fun register() {
            FabricStructureBuilder.create(BetterScaffolding.id("undead_construction_site"), INSTANCE)
                .step(GenerationStep.Feature.SURFACE_STRUCTURES)
                .defaultConfig(StructureConfig(3, 2, 588044896))
                .superflatFeature(CONFIGURED)
                .adjustsSurface()
                .register()
            Registry.register(
                BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE,
                BetterScaffolding.id("configured_undead_construction_site"),
                CONFIGURED
            )
            BiomeModifications.create(BetterScaffolding.id("undead_construction_site_addition"))
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.all()) { ctx ->
                    ctx.generationSettings.addBuiltInStructure(CONFIGURED)
                }
        }
    }

    override fun getStructureStartFactory(): StructureStartFactory<DefaultFeatureConfig> =
        StructureStartFactory { feature, pos, references, seed -> Start(feature, pos, references, seed) }

    class Start(
        feature: StructureFeature<DefaultFeatureConfig>?,
        pos: ChunkPos?, references: Int, seed: Long
    ) : StructureStart<DefaultFeatureConfig>(feature, pos, references, seed) {
        override fun init(
            registryManager: DynamicRegistryManager,
            chunkGenerator: ChunkGenerator,
            manager: StructureManager,
            pos: ChunkPos,
            biome: Biome,
            config: DefaultFeatureConfig,
            world: HeightLimitView
        ) {
            val p = pos.getCenterAtY(0)
            StructurePoolBasedGenerator.method_30419(
                registryManager,
                StructurePoolFeatureConfig(Supplier {
                    val x = registryManager.get(Registry.STRUCTURE_POOL_KEY).get(BetterScaffolding.id("undead/base"))
                    println("XXXX: $x")
                    x
                }, 10),
                ::PoolStructurePiece,
                chunkGenerator,
                manager,
                p,
                this,
                this.random,
                false,
                true,
                world
            )
        }
    }

}