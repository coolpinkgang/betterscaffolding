package com.romangraef.betterscaffolding

import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import java.util.Random
import java.util.function.Function
import java.util.function.Supplier

class UVMapping(
    val vertex: Int,
    val u: Float,
    val v: Float
)

class Face(
    val direction: Direction,
    val textureId: SpriteIdentifier,
    val uvMappings: List<UVMapping>
)

class Cube(
    val posX: Float,
    val posY: Float,
    val posZ: Float,
    val sizeX: Float,
    val sizeY: Float,
    val sizeZ: Float,
    val faces: List<Face>,
)

class Model(
    val cubes: List<Cube>,
    val breakTexture: SpriteIdentifier
) : UnbakedModel {
    
    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()
    
    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<com.mojang.datafixers.util.Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> = mutableListOf(breakTexture).also { list ->
        cubes.forEach { cube -> cube.faces.forEach { list += it.textureId } }
    }
    
    override fun bake(
        loader: ModelLoader?,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel {
        val builder = RendererAccess.INSTANCE.renderer!!.meshBuilder()
        val quadEmitter = builder.emitter
        for (cube in cubes) {
            for (face in cube.faces) {
                quadEmitter.nominalFace(face.direction)
                when (face.direction) {
                    Direction.UP -> {
                        val y = cube.posY + cube.sizeY
                        quadEmitter.pos(0, cube.posX, y, cube.posZ)
                        quadEmitter.pos(1, cube.posX, y, cube.posZ + cube.sizeZ)
                        quadEmitter.pos(2, cube.posX + cube.sizeX, y, cube.posZ + cube.sizeZ)
                        quadEmitter.pos(3, cube.posX + cube.sizeX, y, cube.posZ)
                    }
                    Direction.DOWN -> {
                        quadEmitter.pos(0, cube.posX, cube.posY, cube.posZ)
                        quadEmitter.pos(1, cube.posX + cube.sizeX, cube.posY, cube.posZ)
                        quadEmitter.pos(2, cube.posX + cube.sizeX, cube.posY, cube.posZ + cube.sizeZ)
                        quadEmitter.pos(3, cube.posX, cube.posY, cube.posZ + cube.sizeZ)
                    }
                    Direction.SOUTH -> {
                        val z = cube.posZ + cube.sizeZ
                        quadEmitter.pos(0, cube.posX, cube.posY, z)
                        quadEmitter.pos(1, cube.posX + cube.sizeX, cube.posY, z)
                        quadEmitter.pos(2, cube.posX + cube.sizeX, cube.posY + cube.sizeY, z)
                        quadEmitter.pos(3, cube.posX, cube.posY + cube.sizeY, z)
                    }
                    Direction.NORTH -> {
                        quadEmitter.pos(0, cube.posX, cube.posY, cube.posZ)
                        quadEmitter.pos(1, cube.posX, cube.posY + cube.sizeY, cube.posZ)
                        quadEmitter.pos(2, cube.posX + cube.sizeX, cube.posY + cube.sizeY, cube.posZ)
                        quadEmitter.pos(3, cube.posX + cube.sizeX, cube.posY, cube.posZ)
                    }
                    Direction.EAST -> {
                        val x = cube.posX + cube.sizeX
                        quadEmitter.pos(0, x, cube.posY, cube.posZ)
                        quadEmitter.pos(1, x, cube.posY + cube.sizeY, cube.posZ)
                        quadEmitter.pos(2, x, cube.posY + cube.sizeY, cube.posZ + cube.sizeZ)
                        quadEmitter.pos(3, x, cube.posY, cube.posZ + cube.sizeZ)
                    }
                    Direction.WEST -> {
                        quadEmitter.pos(0, cube.posX, cube.posY, cube.posZ)
                        quadEmitter.pos(1, cube.posX, cube.posY, cube.posZ + cube.sizeZ)
                        quadEmitter.pos(2, cube.posX, cube.posY + cube.sizeY, cube.posZ + cube.sizeZ)
                        quadEmitter.pos(3, cube.posX, cube.posY + cube.sizeY, cube.posZ)
                    }
                }
                for (mapping in face.uvMappings) {
                    quadEmitter.sprite(mapping.vertex, 0, mapping.u, mapping.v)
                    quadEmitter.spriteColor(mapping.vertex, 0, -1, -1, -1)
                }
                quadEmitter.spriteBake(0, textureGetter.apply(face.textureId), MutableQuadView.BAKE_NORMALIZED)
                quadEmitter.emit()
            }
        }
        val mesh = builder.build()
        return object : FabricBakedModel, BakedModel {
            
            override fun isVanillaAdapter(): Boolean = false
    
            override fun emitBlockQuads(
                blockView: BlockRenderView?,
                state: BlockState?,
                pos: BlockPos?,
                randomSupplier: Supplier<Random>?,
                context: RenderContext
            ) = context.meshConsumer().accept(mesh)
    
            override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, context: RenderContext?) {}
    
            override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> =
                mutableListOf()
    
            override fun useAmbientOcclusion(): Boolean = true
    
            override fun hasDepth(): Boolean = true
    
            override fun isSideLit(): Boolean = false
    
            override fun isBuiltin(): Boolean = false
    
            override fun getSprite(): Sprite = textureGetter.apply(breakTexture)
    
            override fun getTransformation(): ModelTransformation? = null
    
            override fun getOverrides(): ModelOverrideList? = null
    
        }
    }
    
}

class CubeBuilder(
    val posX: Float,
    val posY: Float,
    val posZ: Float,
    val sizeX: Float,
    val sizeY: Float,
    val sizeZ: Float,
) {
    
    val faces: MutableList<Face> = mutableListOf()
    
    fun faceB(
        direction: Direction,
        textureId: SpriteIdentifier,
        uvMapping0: Pair<Float, Float>,
        uvMapping1: Pair<Float, Float>,
        uvMapping2: Pair<Float, Float>,
        uvMapping3: Pair<Float, Float>,
    ) {
        face(
            direction,
            textureId,
            UVMapping(0, uvMapping0.first, uvMapping0.second),
            UVMapping(1, uvMapping1.first, uvMapping1.second),
            UVMapping(2, uvMapping2.first, uvMapping2.second),
            UVMapping(3, uvMapping3.first, uvMapping3.second),
        )
    }
    
    fun face(
        direction: Direction,
        textureId: SpriteIdentifier,
        uvMapping0: UVMapping,
        uvMapping1: UVMapping,
        uvMapping2: UVMapping,
        uvMapping3: UVMapping,
    ) { faces += Face(direction, textureId, listOf(uvMapping0, uvMapping1, uvMapping2, uvMapping3)) }
    
    fun build(): Cube = Cube(posX, posY, posZ, sizeX, sizeY, sizeZ, faces)
    
}

class ModelBuilder(val breakTexture: SpriteIdentifier) {
    
    val cubes: MutableList<Cube> = mutableListOf()
    
    fun cube(
        posX: Number,
        posY: Number,
        posZ: Number,
        sizeX: Number,
        sizeY: Number,
        sizeZ: Number,
        builder: CubeBuilder.() -> Unit
    ) {
        cubes += CubeBuilder(
            posX.toFloat()/16f,
            posY.toFloat()/16f,
            posZ.toFloat()/16f,
            sizeX.toFloat()/16f,
            sizeY.toFloat()/16f,
            sizeZ.toFloat()/16f
        ).apply(builder).build()
    }
    
    fun build(): Model = Model(cubes, breakTexture)
    
}

fun model(breakTexture: SpriteIdentifier, builder: ModelBuilder.() -> Unit): Model =
    ModelBuilder(breakTexture).apply(builder).build()
