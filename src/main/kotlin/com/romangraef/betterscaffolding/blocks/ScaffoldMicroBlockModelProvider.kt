package com.romangraef.betterscaffolding.blocks

import com.romangraef.betterscaffolding.BetterScaffolding
import net.fabricmc.fabric.api.client.model.ModelProviderContext
import net.fabricmc.fabric.api.client.model.ModelResourceProvider
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.util.Identifier

object ScaffoldMicroBlockModelProvider : ModelResourceProvider {
    val modelResource = BetterScaffolding.id("block/micro_block")
    override fun loadModelResource(resourceId: Identifier?, context: ModelProviderContext?): UnbakedModel? {
        if (resourceId == modelResource) {
            BetterScaffolding.logger.info("Providing model resource ScaffoldMicroBlockModel")
            return ScaffoldMicroBlockModel()
        }
        return null
    }
}