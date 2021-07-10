package com.romangraef.betterscaffolding.registries

import com.romangraef.betterscaffolding.BetterScaffolding
import com.romangraef.betterscaffolding.entities.ForkliftEntity
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.util.registry.Registry

object BEntities: DefaultDelayedRegistry<EntityType<*>>(Registry.ENTITY_TYPE, BetterScaffolding.modid) {
    val FORKLIFT by "forklift" {
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::ForkliftEntity).dimensions(EntityDimensions.fixed(1.4F, 1.8F)).build() as EntityType<ForkliftEntity>
    }
}