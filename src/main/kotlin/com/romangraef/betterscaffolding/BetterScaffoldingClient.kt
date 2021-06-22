package com.romangraef.betterscaffolding

import com.romangraef.betterscaffolding.blocks.ScaffoldMicroBlockModelProvider
import com.romangraef.betterscaffolding.entities.ForkliftRenderer
import com.romangraef.betterscaffolding.registries.REntities
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry

object BetterScaffoldingClient : ClientModInitializer {
    override fun onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider { ScaffoldMicroBlockModelProvider }
        EntityRendererRegistry.INSTANCE.register(REntities.FORKLIFT) { ForkliftRenderer(it) }
    }
}