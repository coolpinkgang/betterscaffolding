package com.romangraef.betterscaffolding.blocks

import com.romangraef.betterscaffolding.items.PoleItem
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Property
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ScaffoldMicroBlock(settings: Settings) : Block(settings) {

    companion object {
        fun <T : Property<*>> property(getter: (String) -> T) = object : ReadOnlyProperty<Any, T> {
            private var _value: T? = null
            override fun getValue(thisRef: Any, property: KProperty<*>): T {
                return this._value ?: kotlin.run {
                    _value = getter(property.name)
                    _value!!
                }
            }
        }
        val POLE_NORTH by property(BooleanProperty::of)
        val POLE_SOUTH by property(BooleanProperty::of)
        val POLE_WEST by property(BooleanProperty::of)
        val POLE_EAST by property(BooleanProperty::of)
        val PLANKS by property(BooleanProperty::of)
        fun hasPoleSouthEast(state: BlockState) = state[POLE_SOUTH] || state[POLE_EAST]
        fun hasPoleNorthEast(state: BlockState) = state[POLE_NORTH] || state[POLE_EAST]
        fun hasPoleSouthWest(state: BlockState) = state[POLE_SOUTH] || state[POLE_WEST]
        fun hasPoleNorthWest(state: BlockState) = state[POLE_NORTH] || state[POLE_WEST]
        val poleX = 0.25
        val poleY = 0.25
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
    
    fun addPoleShape(shape: VoxelShape, state: BlockState): VoxelShape {
        fun poleAt(baseX: Double, baseZ: Double) =
            VoxelShapes.cuboid(baseX - 0.1, 0.0, baseZ - 0.1, baseX + 0.1, 1.0, baseZ + 0.1)
        
        var shape = shape
        if (hasPoleNorthWest(state))
            shape = VoxelShapes.combine(shape, poleAt(0.0 + poleX, 0.0 + poleY), BooleanBiFunction.OR)
        if (hasPoleSouthWest(state))
            shape = VoxelShapes.combine(shape, poleAt(0.0 + poleX, 1.0 - poleY), BooleanBiFunction.OR)
        if (hasPoleNorthEast(state))
            shape = VoxelShapes.combine(shape, poleAt(1.0 - poleX, 0.0 + poleY), BooleanBiFunction.OR)
        if (hasPoleSouthEast(state))
            shape = VoxelShapes.combine(shape, poleAt(1.0 - poleX, 1.0 - poleY), BooleanBiFunction.OR)
        return shape
        
    }
    
    fun addPlankShape(shape: VoxelShape, position: BlockPos, state: BlockState): VoxelShape =
        if (!state[PLANKS]) shape else VoxelShapes.combine(
            shape,
            VoxelShapes.cuboid(0.0, 15.0, 0.0, 16.0, 16.0, 16.0),
            BooleanBiFunction.OR
        )

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        var shape = VoxelShapes.empty()
        shape = addPoleShape(shape, state)
        shape = addPlankShape(shape, pos, state)
        return shape.simplify()
    }

    override fun getPickStack(world: BlockView?, pos: BlockPos?, state: BlockState?): ItemStack {
        return ItemStack(PoleItem(Item.Settings()), 1) //TODO: maybe improve to select sub blocks
    }

    override fun getCollisionShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape = addPlankShape(VoxelShapes.empty(), pos, state)
}