package com.romangraef.betterscaffolding.blocks

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

class ScaffoldMicroBlock(settings: Settings) : Block(settings) {

    companion object {
        val POLE_NORTH = BooleanProperty.of("pole_north")
        val POLE_SOUTH = BooleanProperty.of("pole_south")
        val POLE_WEST = BooleanProperty.of("pole_west")
        val POLE_EAST = BooleanProperty.of("pole_east")
        fun hasPoleSouthEast(state: BlockState) = state[POLE_SOUTH] || state[POLE_EAST]
        fun hasPoleNorthEast(state: BlockState) = state[POLE_NORTH] || state[POLE_EAST]
        fun hasPoleSouthWest(state: BlockState) = state[POLE_SOUTH] || state[POLE_WEST]
        fun hasPoleNorthWest(state: BlockState) = state[POLE_NORTH] || state[POLE_WEST]
    }

    init {
        defaultState = getStateManager().defaultState
            .with(POLE_NORTH, false)
            .with(POLE_SOUTH, false)
            .with(POLE_WEST, false)
            .with(POLE_EAST, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(POLE_NORTH, POLE_SOUTH, POLE_WEST, POLE_EAST)
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        fun poleAt(baseX: Double, baseZ: Double) =
            VoxelShapes.cuboid(baseX - 0.1, 0.0, baseZ - 0.1, baseX + 0.1, 1.0, baseZ + 0.1)

        var s = VoxelShapes.empty()
        if (hasPoleNorthWest(state)) s = VoxelShapes.combine(s, poleAt(0.25, 0.25), BooleanBiFunction.OR)
        if (hasPoleSouthWest(state)) s = VoxelShapes.combine(s, poleAt(0.25, 0.75), BooleanBiFunction.OR)
        if (hasPoleNorthEast(state)) s = VoxelShapes.combine(s, poleAt(0.75, 0.25), BooleanBiFunction.OR)
        if (hasPoleSouthEast(state)) s = VoxelShapes.combine(s, poleAt(0.75, 0.75), BooleanBiFunction.OR)
        return s.simplify()
    }


    override fun getPickStack(world: BlockView?, pos: BlockPos?, state: BlockState?): ItemStack {
        return super.getPickStack(world, pos, state) // TODO
    }


    override fun getCollisionShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape = getOutlineShape(state, world, pos, context)
}