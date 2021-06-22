// Made with Blockbench 3.8.4
// Exported for Minecraft version 1.17
// Paste this class into your mod and generate all required imports
package com.romangraef.betterscaffolding.entities

import net.minecraft.client.model.*
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity

class ForkliftModel(data: Pair<ModelPart, ModelPart>) : EntityModel<Entity?>() {
    val root = data.first
    val fork = data.second

    override fun setAngles(
        entity: Entity?,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
    }

    override fun render(
        matrixStack: MatrixStack,
        buffer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        root.render(matrixStack, buffer, packedLight, packedOverlay)
    }

    fun setRotationAngle(bone: ModelPart, x: Float, y: Float, z: Float) {
        bone.pitch = x
        bone.yaw = y
        bone.roll = z
    }

    companion object {
        val modelData: Pair<ModelPart, ModelPart>
            get() {
                val modelData = ModelData()
                val modelPartData = modelData.root
                val wheel = modelPartData.addChild(
                    "wheel",
                    ModelPartBuilder.create().uv(0, 26)
                        .cuboid(-11.5f, -6.0f, 2.0f, 1.0f, 6.0f, 6.0f, Dilation(0.0f)).mirrored(false)
                        .uv(0, 12)
                        .cuboid(-11.5f, -6.0f, -13.0f, 1.0f, 6.0f, 6.0f, Dilation(0.0f)).mirrored(false)
                        .uv(8, 6)
                        .cuboid(10.5f, -6.0f, -13.0f, 1.0f, 6.0f, 6.0f, Dilation(0.0f)).mirrored(false)
                        .uv(0, 0)
                        .cuboid(10.5f, -6.0f, 2.0f, 1.0f, 6.0f, 6.0f, Dilation(0.0f)).mirrored(false),
                    ModelTransform.of(0.0f, 24.0f, 0.0f, 0.0f, 0.0f, 0.0f)
                )
                val housing = modelPartData.addChild(
                    "housing",
                    ModelPartBuilder.create().uv(0, 0).cuboid(-10.0f, -4.0f, -15.0f, 20.0f, 1.0f, 25.0f, Dilation(0.0f))
                        .mirrored(false)
                        .uv(65, 26).cuboid(-10.0f, -29.0f, -15.0f, 20.0f, 25.0f, 0.0f, Dilation(0.0f)).mirrored(false)
                        .uv(65, 0).cuboid(-10.0f, -29.0f, 10.0f, 20.0f, 25.0f, 0.0f, Dilation(0.0f)).mirrored(false)
                        .uv(50, 26).cuboid(10.0f, -29.0f, -15.0f, 0.0f, 25.0f, 25.0f, Dilation(0.0f)).mirrored(false)
                        .uv(0, 26).cuboid(-10.0f, -29.0f, -15.0f, 0.0f, 25.0f, 25.0f, Dilation(0.0f)).mirrored(false)
                        .uv(0, 26).cuboid(-10.0f, -29.0f, -15.0f, 20.0f, 0.0f, 25.0f, Dilation(0.0f)).mirrored(false),
                    ModelTransform.of(0.0f, 24.0f, 0.0f, 0.0f, 0.0f, 0.0f)
                )
                val forkguide = modelPartData.addChild(
                    "forkguide",
                    ModelPartBuilder.create().uv(64, 76)
                        .cuboid(-10.0f, -5.0f, -17.0f, 20.0f, 1.0f, 1.0f, Dilation(0.0f)).mirrored(false)
                        .uv(8, 0).cuboid(-2.0f, -5.0f, -16.0f, 4.0f, 1.0f, 1.0f, Dilation(0.0f)).mirrored(false)
                        .uv(90, 78).cuboid(-8.0f, -30.0f, -17.0f, 4.0f, 25.0f, 1.0f, Dilation(0.0f)).mirrored(false)
                        .uv(80, 78).cuboid(4.0f, -30.0f, -17.0f, 4.0f, 25.0f, 1.0f, Dilation(0.0f)).mirrored(false),
                    ModelTransform.of(0.0f, 24.0f, 0.0f, 0.0f, 0.0f, 0.0f)
                )
                val forkData = ModelData()
                val fork = forkData.root.addChild(
                    "fork",
                    ModelPartBuilder.create().uv(40, 76).cuboid(4.0f, -1.0f, -15.0f, 4.0f, 1.0f, 16.0f, Dilation(0.0f))
                        .mirrored(false)
                        .uv(0, 76).cuboid(-8.0f, -1.0f, -15.0f, 4.0f, 1.0f, 16.0f, Dilation(0.0f)).mirrored(false),
                    ModelTransform.of(0.0f, 19.0f, -18.0f, 0.0f, 0.0f, 0.0f)
                )
                return modelPartData.createPart(128, 128) to forkData.root.createPart(128, 128)
            }
    }

}