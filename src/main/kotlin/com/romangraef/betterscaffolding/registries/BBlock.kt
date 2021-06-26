package com.romangraef.betterscaffolding.registries

import com.romangraef.betterscaffolding.BetterScaffolding
import com.romangraef.betterscaffolding.blocks.ScaffoldMicroBlock
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.util.registry.Registry

object BBlock : DefaultDelayedRegistry<Block>(Registry.BLOCK, BetterScaffolding.modid) {
    val DEFAULT_SETTINGS = AbstractBlock.Settings.of(Material.STONE)
    val scaffoldMicroBlock by "micro_block" {
        ScaffoldMicroBlock()
    }
}