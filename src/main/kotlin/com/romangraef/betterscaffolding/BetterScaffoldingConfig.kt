package com.romangraef.betterscaffolding

import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.annotation.ConfigEntry

@Config(name = BetterScaffolding.modid)
class BetterScaffoldingConfig : ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    var groupScaffolding = ScaffoldingBlockConfig()

    @ConfigEntry.Gui.CollapsibleObject
    var groupForklift = ForkliftConfig()
    
    class ForkliftConfig {
        var blockWhitelist = listOf("minecraft:chest")
    }
    
    class ScaffoldingBlockConfig {
        var maxLength = 5
        var minLength = 1
    }

    override fun validatePostLoad() {
        super.validatePostLoad()
        if (groupScaffolding.minLength >= groupScaffolding.maxLength) throw ConfigData.ValidationException("groupScaffolding.minLength must be smaller than groupScaffolding.maxLength")
        if (groupScaffolding.minLength < 1) throw ConfigData.ValidationException("groupScaffolding.minLength must be greater than 0")
    }
}