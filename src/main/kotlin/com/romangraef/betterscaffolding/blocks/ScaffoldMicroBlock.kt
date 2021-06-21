package com.romangraef.betterscaffolding.blocks

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
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
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape =
        VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75)


    override fun getCollisionShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape = VoxelShapes.empty()
}