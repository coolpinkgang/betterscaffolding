package com.romangraef.betterscaffolding.interop

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi

class ModmenuInterop : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return super.getModConfigScreenFactory()
    }

    override fun getProvidedConfigScreenFactories(): MutableMap<String, ConfigScreenFactory<*>> {
        return super.getProvidedConfigScreenFactories()
    }
}