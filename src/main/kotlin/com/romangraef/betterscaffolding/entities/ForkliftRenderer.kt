package com.romangraef.betterscaffolding.entities

import com.romangraef.betterscaffolding.BetterScaffolding
import net.minecraft.block.BlockRenderType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3f

class ForkliftRenderer<T : ForkliftEntity>(ctx: EntityRendererFactory.Context) : EntityRenderer<T>(ctx) {
    val model = ForkliftModel(ForkliftModel.modelData)
    override fun getTexture(entity: T): Identifier = BetterScaffolding.id("textures/entity/forklift.png")
    override fun render(
        entity: T,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
        matrices.push()
        matrices.translate(0.0, 1.5, 0.0)
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180F))
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(yaw))
        model.render(
            matrices,
            vertexConsumers.getBuffer(model.getLayer(getTexture(entity))),
            light,
            OverlayTexture.DEFAULT_UV,
            1F,
            1F,
            1F,
            1F
        )
        matrices.translate(0.0, -ForkliftEntity.MAX_FORK_HEIGHT * entity.forkHeight, 0.0)
        model.fork.render(
            matrices,
            vertexConsumers.getBuffer(model.getLayer(getTexture(entity))),
            light,
            OverlayTexture.DEFAULT_UV,
            1f,
            1f,
            1f,
            1f
        )
        val bs = entity.pickedUpBlock
        if (bs != null && bs.renderType != BlockRenderType.INVISIBLE) {
            matrices.push()
            matrices.scale(0.8f, 0.8f, 0.8f)
            matrices.translate(-0.5, 1.4, -1.4)
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180F))
            MinecraftClient.getInstance().blockRenderManager.renderBlockAsEntity(
                bs,
                matrices,
                vertexConsumers,
                light,
                OverlayTexture.DEFAULT_UV
            )
            matrices.pop()
        }
        matrices.pop()
    }
}