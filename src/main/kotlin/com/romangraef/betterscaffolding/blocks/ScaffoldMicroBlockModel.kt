package com.romangraef.betterscaffolding.blocks

import com.mojang.datafixers.util.Pair
import com.romangraef.betterscaffolding.BetterScaffolding
import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.*
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.Identifier
import org.apache.commons.lang3.tuple.ImmutablePair
import java.util.function.Function
import java.util.function.Predicate


class ScaffoldMicroBlockModel : UnbakedModel {
    companion object {
        private val GRAY_SPRITE =
            SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, BetterScaffolding.id("block/gray"))
        private val SUPPORT_BASE_MODEL =
            ModelIdentifier(BetterScaffolding.id("block/scaffolding_support_base"), "")
    }

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableSetOf(SUPPORT_BASE_MODEL)

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> = mutableSetOf(GRAY_SPRITE)

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings,
        modelId: Identifier
    ): BakedModel {
        val sprite = textureGetter.apply(GRAY_SPRITE)
        val supportBase = loader.bake(SUPPORT_BASE_MODEL, rotationContainer)!!
        val renderer: Renderer = RendererAccess.INSTANCE.renderer!!
        return MultipartBakedModel(
            listOf(
                ImmutablePair.of(Predicate { it[ScaffoldMicroBlock.POLE_EAST] }, supportBase)
            )
        )
    }

}
