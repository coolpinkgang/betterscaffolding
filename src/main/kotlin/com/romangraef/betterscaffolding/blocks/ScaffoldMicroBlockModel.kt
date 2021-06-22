package com.romangraef.betterscaffolding.blocks

import com.mojang.datafixers.util.Pair
import com.romangraef.betterscaffolding.BetterScaffolding
import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3f
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Function
import java.util.function.Supplier


class ScaffoldMicroBlockModel : UnbakedModel {
    companion object {
        private val GRAY_SPRITE =
            SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, BetterScaffolding.id("block/gray.png"))
    }

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableSetOf()

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
        val renderer: Renderer = RendererAccess.INSTANCE.renderer!!
        return Baked(
            sprite,
            generateLeg(renderer, sprite, 0f, 0f),
            generateLeg(renderer, sprite, 0f, 0f),
            generateLeg(renderer, sprite, 0f, 0f),
            generateLeg(renderer, sprite, 0f, 0f),
        )
    }

    fun generateLeg(renderer: Renderer, graySprite: Sprite, baseX: Float, baseZ: Float): Mesh {
        val builder: MeshBuilder = renderer.meshBuilder()
        val emitter = builder.emitter
        val pos = arrayOf(
            // DOWN                      UP
            Vec3f(1f, 0f, 0f), Vec3f(1f, 1f, 0f), // NORTH EAST
            Vec3f(0f, 0f, 0f), Vec3f(0f, 1f, 0f), // NORTH WEST
            Vec3f(1f, 0f, 1f), Vec3f(1f, 1f, 1f), // SOUTH EAST
            Vec3f(0f, 0f, 1f), Vec3f(0f, 1f, 1f), // SOUTH WEST
        )
        val quads = arrayOf(
            0, 2, 6, 4, // DOWN
            1, 3, 7, 5, // UP
            0, 1, 5, 4, // EAST
            2, 3, 7, 6, // WEST
            0, 1, 3, 2, // NORTH
            4, 5, 6, 5, // SOUTH
        )
        for (i in 0 until 6) {
            for (j in 0 until 4)
                emitter.pos(j, pos[quads[i * 4 + j]])
            emitter.spriteBake(0, graySprite, MutableQuadView.BAKE_LOCK_UV)
            emitter.spriteColor(0, -1, -1, -1, -1)
            emitter.emit()
        }
        return builder.build()
    }

    class Baked(
        val graySprite: Sprite,
        val northEastLeg: Mesh,
        val southEastLeg: Mesh,
        val northWestLeg: Mesh,
        val southWestLeg: Mesh,
    ) : BakedModel, FabricBakedModel {
        override fun isVanillaAdapter(): Boolean = false

        override fun emitBlockQuads(
            blockView: BlockRenderView,
            state: BlockState,
            pos: BlockPos,
            randomSupplier: Supplier<Random>,
            context: RenderContext
        ) {

            val mc = context.meshConsumer()
            if (ScaffoldMicroBlock.hasPoleNorthEast(state)) mc.accept(northEastLeg)
            if (ScaffoldMicroBlock.hasPoleNorthWest(state)) mc.accept(northWestLeg)
            if (ScaffoldMicroBlock.hasPoleSouthEast(state)) mc.accept(southEastLeg)
            if (ScaffoldMicroBlock.hasPoleSouthWest(state)) mc.accept(southWestLeg)
        }

        override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, context: RenderContext?) {
        }

        override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> =
            mutableListOf()

        override fun useAmbientOcclusion(): Boolean = false

        override fun hasDepth(): Boolean = false

        override fun isSideLit(): Boolean = true

        override fun isBuiltin(): Boolean = false

        override fun getSprite(): Sprite? = graySprite

        override fun getTransformation(): ModelTransformation = ModelTransformation.NONE

        override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    }
}