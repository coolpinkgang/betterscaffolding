package com.romangraef.betterscaffolding.interop

import com.romangraef.betterscaffolding.BetterScaffoldingConfig
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.shedaniel.autoconfig.AutoConfig
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
class ModmenuInterop : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> =
        ConfigScreenFactory { AutoConfig.getConfigScreen(BetterScaffoldingConfig::class.java, it).get() }
}