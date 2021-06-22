package com.romangraef.betterscaffolding.entities

import com.romangraef.betterscaffolding.registries.REntities
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MovementType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ForkliftEntity(entityType: EntityType<*>, world: World) : Entity(entityType, world) {
    constructor(world: World) : this(REntities.FORKLIFT, world)

    private var clientX: Double = 0.0
    private var clientY: Double = 0.0
    private var clientZ: Double = 0.0
    private var clientInterpolationSteps: Int = -1

    companion object {
        const val MAX_FORK_HEIGHT = 1.4
        val FORK_HEIGHT = DataTracker.registerData(ForkliftEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
    }

    var forkHeight by dataTracker.wrap(FORK_HEIGHT)

    override fun initDataTracker() {
        dataTracker.startTracking(FORK_HEIGHT, 0f)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound?) {
        nbt ?: return
        if (nbt.contains("forkHeight"))
            forkHeight = MathHelper.clamp(nbt.getFloat("forkHeight"), 0f, 1f)
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        nbt.putFloat("forkHeight", forkHeight)
    }

    override fun createSpawnPacket(): Packet<*> = EntitySpawnS2CPacket(this)
    override fun tick() {
        super.tick()
        if (!world.isClient) {
            if (!hasNoGravity()) {
                val gravity = if (isTouchingWater) -0.005 else -0.04
                velocity = velocity.add(0.0, gravity, 0.0)
            }
            checkBlockCollision()
            updateWaterState()
            move(MovementType.SELF, velocity)
            velocity = velocity.multiply(0.95)
        } else {
            moveSmooth();
        }
    }

    private fun moveSmooth() {
        if (clientInterpolationSteps > 0) {
            val nx = x + (clientX - x) / clientInterpolationSteps
            val ny = y + (clientY - y) / clientInterpolationSteps
            val nz = z + (clientZ - z) / clientInterpolationSteps
            setPosition(nx, ny, nz)
        } else {
            refreshPosition()
        }
        setRotation(yaw, pitch)
    }

    override fun updateTrackedPositionAndAngles(
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        interpolationSteps: Int,
        interpolate: Boolean
    ) {
        clientX = x
        clientY = y
        clientZ = z
        clientInterpolationSteps = interpolationSteps + 2
    }

    override fun getPickBlockStack(): ItemStack? {
        return super.getPickBlockStack() // TODO
    }
}

private fun <T> DataTracker.wrap(trackedData: TrackedData<T>): ReadWriteProperty<Any?, T> =
    object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            this@wrap[trackedData]


        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this@wrap[trackedData] = value
        }

    }

