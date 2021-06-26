package com.romangraef.betterscaffolding.blocks

import com.romangraef.betterscaffolding.BVoxelShapes
import com.romangraef.betterscaffolding.plus
import com.romangraef.betterscaffolding.registries.BBlock
import com.romangraef.betterscaffolding.registries.BItems
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.ShapeContext
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Property
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.WorldAccess
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ScaffoldMicroBlock() : Block(Settings.copy(Blocks.SCAFFOLDING)) {

    enum class PlankDirection : StringIdentifiable {
        NONE,
        NORTH_SOUTH,
        WEST_EAST;

        override fun asString(): String = name.lowercase()
    }

    enum class PoleState : StringIdentifiable {
        NONE, POLE, POLE_AND_CONNECTION;

        val bool get() = this != NONE
        override fun asString(): String = name.lowercase()
    }

    companion object {
        fun <T : Property<*>> property(getter: (String) -> T) = object : ReadOnlyProperty<Any, T> {
            private var _value: T? = null
            override fun getValue(thisRef: Any, property: KProperty<*>): T {
                return this._value ?: kotlin.run {
                    _value = getter(property.name.lowercase())
                    _value!!
                }
            }
        }

        inline fun <reified T : Enum<T>> enumProperty() where T : StringIdentifiable =
            property { EnumProperty.of(it, T::class.java) }

        val POLE_N by enumProperty<PoleState>()
        val POLE_S by enumProperty<PoleState>()
        val POLE_W by enumProperty<PoleState>()
        val POLE_E by enumProperty<PoleState>()
        val PLANKS = EnumProperty.of("planks", PlankDirection::class.java)
        fun hasPoleSouthEast(state: BlockState) = state[POLE_S] != PoleState.NONE || state[POLE_E] != PoleState.NONE
        fun hasPoleNorthEast(state: BlockState) = state[POLE_N] != PoleState.NONE || state[POLE_E] != PoleState.NONE
        fun hasPoleSouthWest(state: BlockState) = state[POLE_S] != PoleState.NONE || state[POLE_W] != PoleState.NONE
        fun hasPoleNorthWest(state: BlockState) = state[POLE_N] != PoleState.NONE || state[POLE_W] != PoleState.NONE
        fun hasConnectionSouth(state: BlockState) = state[POLE_S] == PoleState.POLE_AND_CONNECTION
        fun hasConnectionNorth(state: BlockState) = state[POLE_N] == PoleState.POLE_AND_CONNECTION
        fun hasConnectionWest(state: BlockState) = state[POLE_W] == PoleState.POLE_AND_CONNECTION
        fun hasConnectionEast(state: BlockState) = state[POLE_E] == PoleState.POLE_AND_CONNECTION
        const val POLE_SIZE = 0.0625
        const val POLE_X = .0 + POLE_SIZE
        const val POLE_Y = .0 + POLE_SIZE
    }

    private fun updateSingularPole(prop: EnumProperty<PoleState>, bs: BlockState, isTopOrPlanks: Boolean): BlockState {
        return bs.with(
            prop,
            if (bs[prop] != PoleState.NONE)
                if (isTopOrPlanks)
                    PoleState.POLE_AND_CONNECTION
                else PoleState.POLE
            else PoleState.NONE
        )
    }

    fun setMicroblock(world: WorldAccess, pos: BlockPos, updater: (BlockState) -> BlockState) {
        var bs = world.getBlockState(pos)
        if (bs == null || bs.isAir) bs = this.defaultState
        if (bs.block != this) throw IllegalStateException("Invalid initial block state during microblock update: $bs")
        bs = updater(bs)
        if (bs.block != this) throw IllegalStateException("Invalid updated block state during microblock update: $bs")
        val isTop = world.getBlockState(pos.offset(Direction.UP))?.let { it.block != BBlock.scaffoldMicroBlock } ?: true
        val isTopOrPlanks = isTop || bs[PLANKS] != PlankDirection.NONE
        bs = updateSingularPole(POLE_N, bs, isTopOrPlanks)
        bs = updateSingularPole(POLE_W, bs, isTopOrPlanks)
        bs = updateSingularPole(POLE_S, bs, isTopOrPlanks)
        bs = updateSingularPole(POLE_E, bs, isTopOrPlanks)
        world.setBlockState(pos, bs, NOTIFY_ALL or SKIP_LIGHTING_UPDATES)
        world.blockTickScheduler.schedule(pos, this, 1)
    }

    init {
        defaultState = getStateManager().defaultState
            .with(POLE_N, PoleState.NONE)
            .with(POLE_S, PoleState.NONE)
            .with(POLE_W, PoleState.NONE)
            .with(POLE_E, PoleState.NONE)
            .with(PLANKS, PlankDirection.NONE)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(POLE_N, POLE_S, POLE_W, POLE_E, PLANKS)
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        super.scheduledTick(state, world, pos, random)
        if (!isValidStructure(world, state, pos)) // TODO check for stable footing
            world.breakBlock(pos, true)
    }


    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction?,
        neighborState: BlockState?,
        world: WorldAccess,
        pos: BlockPos,
        neighborPos: BlockPos?
    ): BlockState {
        if (!isValidStructure(world, state, pos)) {
            world.blockTickScheduler.schedule(pos, this, 1)
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
    }

    private fun isValidStructure(world: WorldAccess, state: BlockState, pos: BlockPos): Boolean {
        if (!(state[POLE_E] != PoleState.NONE || state[POLE_N] != PoleState.NONE || state[POLE_S] != PoleState.NONE
                    || state[POLE_W] != PoleState.NONE || state[PLANKS] != PlankDirection.NONE)
        ) {
            return false
        }
        return true
    }


    private fun addPoleShape(shape: VoxelShape, state: BlockState): VoxelShape {
        fun poleAt(baseX: Double, baseZ: Double) =
            VoxelShapes.cuboid(
                baseX - POLE_SIZE,
                0.0,
                baseZ - POLE_SIZE,
                baseX + POLE_SIZE,
                1.0,
                baseZ + POLE_SIZE
            )

        var shape = shape
        if (hasPoleNorthWest(state))
            shape += poleAt(0.0 + POLE_X, 0.0 + POLE_Y)
        if (hasPoleSouthWest(state))
            shape += poleAt(0.0 + POLE_X, 1.0 - POLE_Y)
        if (hasPoleNorthEast(state))
            shape += poleAt(1.0 - POLE_X, 0.0 + POLE_Y)
        if (hasPoleSouthEast(state))
            shape += poleAt(1.0 - POLE_X, 1.0 - POLE_Y)
        return shape
    }

    private fun addConnectionShape(shape: VoxelShape, state: BlockState): VoxelShape {
        var shape = shape
        if (hasConnectionNorth(state))
            shape += BVoxelShapes.cuboidB(2, 13, 0.5, 12, 2, 1)
        if (hasConnectionSouth(state))
            shape += BVoxelShapes.cuboidB(2, 13, 14.5, 12, 2, 1)
        if (hasConnectionWest(state))
            shape += BVoxelShapes.cuboidB(0.5, 13, 2, 1, 2, 12)
        if (hasConnectionEast(state))
            shape += BVoxelShapes.cuboidB(14.5, 13, 2, 1, 2, 12)
        return shape
    }

    private fun addPlankShape(shape: VoxelShape, state: BlockState): VoxelShape = when (state[PLANKS]!!) {
        PlankDirection.NONE -> shape
        PlankDirection.NORTH_SOUTH ->
            (shape + BVoxelShapes.cuboidB(2.5, 15, 0, 5, 1, 16)
                    + BVoxelShapes.cuboidB(8.5, 15, 0, 5, 1, 16))
        PlankDirection.WEST_EAST ->
            (shape + BVoxelShapes.cuboidB(0, 15, 2.5, 16, 1, 5)
                    + BVoxelShapes.cuboidB(0, 15, 8.5, 16, 1, 5))
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        var shape = VoxelShapes.empty()
        shape = addPoleShape(shape, state)
        shape = addConnectionShape(shape, state)
        shape = addPlankShape(shape, state)
        return shape.simplify()
    }

    override fun getPickStack(world: BlockView?, pos: BlockPos?, state: BlockState?): ItemStack {
        return ItemStack(BItems.pole, 1) //TODO: maybe improve to select sub blocks
    }

    override fun getDroppedStacks(state: BlockState?, builder: LootContext.Builder?): MutableList<ItemStack> {
        return super.getDroppedStacks(state, builder)
    }

    override fun getCollisionShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape = addPlankShape(VoxelShapes.empty(), state)
}