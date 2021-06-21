package com.romangraef.betterscaffolding

import net.fabricmc.api.ModInitializer
import java.util.logging.Logger

object BetterScaffolding : ModInitializer {
    val logger = Logger.getLogger("betterscaffolding")
    override fun onInitialize() {
        logger.info("Loaded better scaffolding")
    }
}


