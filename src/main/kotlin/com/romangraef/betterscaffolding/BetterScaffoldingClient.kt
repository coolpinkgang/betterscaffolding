package com.romangraef.betterscaffolding

import com.romangraef.betterscaffolding.blocks.ScaffoldMicroBlockModelProvider
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry

class BetterScaffoldingClient : ClientModInitializer {
    override fun onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider { ScaffoldMicroBlockModelProvider }
    }
}