package com.romangraef.betterscaffolding.entities

import com.romangraef.betterscaffolding.registries.REntities
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.world.World

class ForkliftEntity(entityType: EntityType<*>, world: World) : Entity(entityType, world) {
    constructor(world: World) : this(REntities.FORKLIFT, world)

    companion object {

    }

    override fun initDataTracker() {
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound?) {
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound?) {
    }

    override fun createSpawnPacket(): Packet<*> = EntitySpawnS2CPacket(this)

}