package com.romangraef.betterscaffolding.entities

import com.romangraef.betterscaffolding.BetterScaffolding
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
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180F - yaw))
        matrices.translate(0.0, 1.5, 0.2)
        matrices.scale(1F, -1F, 1F)
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
        matrices.pop()
    }
}