package com.romangraef.betterscaffolding.registries

import com.romangraef.betterscaffolding.BetterScaffolding
import com.romangraef.betterscaffolding.entities.ForkliftEntity
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.util.registry.Registry

object REntities: DefaultDelayedRegistry<EntityType<*>>(Registry.ENTITY_TYPE, BetterScaffolding.modid) {
    val FORKLIFT by "forklift" {
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::ForkliftEntity).build() as EntityType<ForkliftEntity>
    }
}