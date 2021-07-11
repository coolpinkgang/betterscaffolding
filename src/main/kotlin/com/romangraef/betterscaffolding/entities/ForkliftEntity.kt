package com.romangraef.betterscaffolding.entities

import com.romangraef.betterscaffolding.BetterScaffolding
import com.romangraef.betterscaffolding.registries.BItems
import com.romangraef.betterscaffolding.registries.BEntities
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MovementType
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.vehicle.BoatEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.world.GameRules
import net.minecraft.world.World
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class ForkliftEntity(entityType: EntityType<*>, world: World) : Entity(entityType, world) {
    constructor(world: World) : this(BEntities.FORKLIFT, world)

    private var clientX: Double = 0.0
    private var clientY: Double = 0.0
    private var clientZ: Double = 0.0
    private var clientYaw: Float = 0.0F
    private var clientInterpolationSteps: Int = -1
    private var pressingRight = false
    private var pressingBack = false
    private var pressingLeft = false
    private var pressingForward = false

    companion object {
        const val MAX_FORK_HEIGHT = 1.4
        val FORK_HEIGHT = DataTracker.registerData(ForkliftEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
        val PICKED_UP_BLOCK = DataTracker.registerData(ForkliftEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
    }

    override fun collidesWith(other: Entity): Boolean {
        return BoatEntity.canCollide(this, other)
    }

    override fun getMovementDirection(): Direction = horizontalFacing.rotateYClockwise()

    override fun isPushable(): Boolean = false
    override fun isCollidable(): Boolean = true
    override fun collides(): Boolean = !isRemoved

    override fun interact(player: PlayerEntity, hand: Hand): ActionResult {
        if (player.shouldCancelInteraction())
            return ActionResult.PASS
        if (hasPassengers())
            return ActionResult.PASS
        if (world.isClient)
            return ActionResult.SUCCESS
        if (!BItems.license.isPlayerCertified(player)) {
            player.sendMessage(BetterScaffolding.error("license.missing"), true)
            return ActionResult.FAIL
        }
        return if (player.startRiding(this)) ActionResult.CONSUME else ActionResult.PASS
    }


    @Environment(EnvType.CLIENT)
    fun setInputs(pressingLeft: Boolean, pressingRight: Boolean, pressingForward: Boolean, pressingBack: Boolean) {
        this.pressingBack = pressingBack
        this.pressingRight = pressingRight
        this.pressingLeft = pressingLeft
        this.pressingForward = pressingForward
    }

    override fun damage(source: DamageSource, amount: Float): Boolean {
        if (!world.isClient && !isRemoved) {
            if (isInvulnerableTo(source)) return false
            removeAllPassengers()
            remove(RemovalReason.KILLED)
            if (world.gameRules.getBoolean(GameRules.DO_ENTITY_DROPS) && !source.isSourceCreativePlayer)
                dropItems()
        }
        return false
    }

    private fun dropItems() {
        dropStack(ItemStack(BItems.forklift))
    }

    var pickupDelay = 0
    var forkHeight by dataTracker.wrap(FORK_HEIGHT)

    private var pickedUpBlockId by dataTracker.wrap(PICKED_UP_BLOCK)
    var pickedUpBlock: BlockState?
        get() = if (pickedUpBlockId < 0) null else Block.getStateFromRawId(pickedUpBlockId)
        set(value) {
            pickedUpBlockId =
                if (value != null)
                    Block.getRawIdFromState(value)
                else
                    -1
        }

    var pickedUpNbtData: NbtCompound? = null

    override fun getMountedHeightOffset(): Double = 0.0
    override fun initDataTracker() {
        dataTracker.startTracking(FORK_HEIGHT, 0f)
        dataTracker.startTracking(PICKED_UP_BLOCK, -1)
    }


    override fun readCustomDataFromNbt(nbt: NbtCompound?) {
        nbt ?: return
        if (nbt.contains("forkHeight"))
            forkHeight = MathHelper.clamp(nbt.getFloat("forkHeight"), 0f, 1f)
        pickedUpBlock =
            if (nbt.contains("pickedUpBlock"))
                NbtHelper.toBlockState(nbt.getCompound("pickedUpBlock"))
            else
                null
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        nbt.putFloat("forkHeight", forkHeight)
        if (pickedUpBlock != null)
            nbt.put("pickedUpBlock", NbtHelper.fromBlockState(pickedUpBlock))
    }

    override fun createSpawnPacket(): Packet<*> = EntitySpawnS2CPacket(this)
    override fun tick() {
        super.tick()
        if (pickupDelay > 0)
            pickupDelay--
        val pp = primaryPassenger as? ServerPlayerEntity
        if (!world.isClient && pp != null && !BItems.license.isPlayerCertified(pp)) {
            pp.sendMessage(BetterScaffolding.error("license.missing"), true)
            removeAllPassengers()
        }
        if (isLogicalSideForUpdatingMovement) {
            if (world.isClient)
                updateClientMovement()
            if (!hasNoGravity()) {
                val gravity = if (isTouchingWater) -0.005 else -0.04
                velocity = velocity.add(0.0, gravity, 0.0)
            }
            checkBlockCollision()
            updateWaterState()
            move(MovementType.SELF, velocity)
            velocity = Vec3d(0.0, velocity.y * 0.95, 0.0)
        }
        moveSmooth();
    }

    private fun updateClientMovement() {
        if (hasPassengers()) {
            val p = primaryPassenger ?: return
            when {
                this.pressingLeft > this.pressingRight -> {
                    yaw -= 2
                }
                this.pressingRight > this.pressingLeft -> {
                    yaw += 2
                }
                this.pressingForward -> {
                    move(MovementType.PLAYER, Vec3d.fromPolar(0f, yaw).normalize().multiply(0.1))
                }
            }
        }
    }

    val blockEntityWhitelist: List<String> = BetterScaffolding.config.groupForklift.blockEntityWhitelist
    
    fun pickOrDropBlock() {
        if (pickupDelay > 0)
            return
        pickupDelay = 10
        val b = pickedUpBlock
        val p = primaryPassenger as ServerPlayerEntity
        val interactPosD =
            pos.add(Vec3d.fromPolar(0f, yaw).multiply(1.5)).add(0.0, 0.3 + forkHeight * MAX_FORK_HEIGHT, 0.0)
        val interactPos = BlockPos(interactPosD)
        if (b != null) {
            if (world.getBlockState(interactPos).isAir) {
                world.setBlockState(interactPos, b)
                if (pickedUpNbtData != null)
                    world.getBlockEntity(interactPos)!!.readNbt(pickedUpNbtData)
                world.blockTickScheduler.schedule(interactPos, b.block, 1)
                pickedUpBlock = null
                pickedUpNbtData = null
            } else
                p.sendMessage(BetterScaffolding.error("blockalreadypresent"), true)
        } else {
            val ns = world.getBlockState(interactPos)
            when {
                ns.isAir -> {
                    p.sendMessage(BetterScaffolding.error("noblockfound"), true)
                }
                world.getBlockEntity(interactPos) != null -> {
                    if (blockEntityWhitelist.contains(Registry.BLOCK.getKey(ns.block).get().value.toString())) {
                        pickedUpBlock = ns
                        pickedUpNbtData = world.getBlockEntity(interactPos)!!.writeNbt(NbtCompound())
                    } else p.sendMessage(BetterScaffolding.error("invalidblockfound"), true)
                }
                else -> {
                    pickedUpBlock = ns
                    world.setBlockState(interactPos, Blocks.AIR.defaultState)
                }
            }
        }
    }

    override fun getPrimaryPassenger(): Entity? = firstPassenger

    private fun moveSmooth() {
        if (isLogicalSideForUpdatingMovement) {
            clientInterpolationSteps = -1
            updateTrackedPosition(x, y, z)
        }
        if (clientInterpolationSteps > 0) {
            val nx = x + (clientX - x) / clientInterpolationSteps
            val ny = y + (clientY - y) / clientInterpolationSteps
            val nz = z + (clientZ - z) / clientInterpolationSteps
            val nyaw = yaw + (clientYaw - yaw) / clientInterpolationSteps
            setPosition(nx, ny, nz)
            setRotation(nyaw.toFloat(), pitch)
        }
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
        clientYaw = yaw
        clientInterpolationSteps = interpolationSteps + 2
    }

    override fun getPickBlockStack(): ItemStack = ItemStack(BItems.forklift)
}

private fun <T> DataTracker.wrap(trackedData: TrackedData<T>): ReadWriteProperty<Any?, T> =
    object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            this@wrap[trackedData]


        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this@wrap[trackedData] = value
        }

    }

