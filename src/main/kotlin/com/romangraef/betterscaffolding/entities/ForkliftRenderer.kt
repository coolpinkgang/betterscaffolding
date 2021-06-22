package com.romangraef.betterscaffolding.entities

import com.romangraef.betterscaffolding.BetterScaffolding
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

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
        matrices.scale(1F, -1F, 1F)
        model.render(matrices, vertexConsumers.getBuffer(model.getLayer(getTexture(entity))), light, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F)
        matrices.pop()
    }
}