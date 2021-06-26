package com.romangraef.betterscaffolding.blocks

import com.mojang.datafixers.util.Pair
import com.romangraef.betterscaffolding.BetterScaffolding
import net.minecraft.client.render.model.*
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.Identifier
import org.apache.commons.lang3.tuple.ImmutablePair
import java.util.function.Function
import java.util.function.Predicate


class ScaffoldMicroBlockModel : UnbakedModel {
    companion object {
        private val SUPPORT_LEG_NE_MODEL =
            ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_leg_ne"), "")
        private val SUPPORT_LEG_NW_MODEL =
            ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_leg_nw"), "")
        private val SUPPORT_LEG_SE_MODEL =
            ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_leg_se"), "")
        private val SUPPORT_LEG_SW_MODEL =
            ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_leg_sw"), "")
        private val SUPPORT_CONNECTION_N_MODEL =
            ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_connection_n"), "")
        private val SUPPORT_CONNECTION_S_MODEL =
            ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_connection_s"), "")
        private val SUPPORT_CONNECTION_W_MODEL =
            ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_connection_w"), "")
        private val SUPPORT_CONNECTION_E_MODEL =
            ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_connection_e"), "")
        private val PLANKS_NS_MODEL =
            ModelIdentifier(BetterScaffolding.id("block/scaffolding_planks_ns"), "")
        private val PLANKS_WE_MODEL =
            ModelIdentifier(BetterScaffolding.id("block/scaffolding_planks_we"), "")
    }

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableSetOf(
        SUPPORT_LEG_NE_MODEL,
        SUPPORT_LEG_NW_MODEL,
        SUPPORT_LEG_SE_MODEL,
        SUPPORT_LEG_SW_MODEL,
        SUPPORT_CONNECTION_N_MODEL,
        SUPPORT_CONNECTION_S_MODEL,
        SUPPORT_CONNECTION_W_MODEL,
        SUPPORT_CONNECTION_E_MODEL,
        PLANKS_NS_MODEL,
        PLANKS_WE_MODEL,
    )

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> = mutableListOf()

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings,
        modelId: Identifier
    ): BakedModel {
        val supportLegNE = loader.bake(SUPPORT_LEG_NE_MODEL, rotationContainer)!!
        val supportLegNW = loader.bake(SUPPORT_LEG_NW_MODEL, rotationContainer)!!
        val supportLegSE = loader.bake(SUPPORT_LEG_SE_MODEL, rotationContainer)!!
        val supportLegSW = loader.bake(SUPPORT_LEG_SW_MODEL, rotationContainer)!!
        val supportConnectionN = loader.bake(SUPPORT_CONNECTION_N_MODEL, rotationContainer)!!
        val supportConnectionS = loader.bake(SUPPORT_CONNECTION_S_MODEL, rotationContainer)!!
        val supportConnectionW = loader.bake(SUPPORT_CONNECTION_W_MODEL, rotationContainer)!!
        val supportConnectionE = loader.bake(SUPPORT_CONNECTION_E_MODEL, rotationContainer)!!
        val planksNS = loader.bake(PLANKS_NS_MODEL, rotationContainer)!!
        val planksWE = loader.bake(PLANKS_WE_MODEL, rotationContainer)!!
        return MultipartBakedModel(
            listOf(
                ImmutablePair.of(Predicate { ScaffoldMicroBlock.hasPoleNorthEast(it) }, supportLegNE),
                ImmutablePair.of(Predicate { ScaffoldMicroBlock.hasPoleNorthWest(it) }, supportLegNW),
                ImmutablePair.of(Predicate { ScaffoldMicroBlock.hasPoleSouthEast(it) }, supportLegSE),
                ImmutablePair.of(Predicate { ScaffoldMicroBlock.hasPoleSouthWest(it) }, supportLegSW),
                ImmutablePair.of(Predicate { it[ScaffoldMicroBlock.CONNECTION_N] }, supportConnectionN),
                ImmutablePair.of(Predicate { it[ScaffoldMicroBlock.CONNECTION_S] }, supportConnectionS),
                ImmutablePair.of(Predicate { it[ScaffoldMicroBlock.CONNECTION_W] }, supportConnectionW),
                ImmutablePair.of(Predicate { it[ScaffoldMicroBlock.CONNECTION_E] }, supportConnectionE),
                ImmutablePair.of(Predicate { it[ScaffoldMicroBlock.PLANKS] == ScaffoldMicroBlock.PlankDirection.NORTH_SOUTH }, planksNS),
                ImmutablePair.of(Predicate { it[ScaffoldMicroBlock.PLANKS] == ScaffoldMicroBlock.PlankDirection.WEST_EAST }, planksWE),
            )
        )
    }

}
