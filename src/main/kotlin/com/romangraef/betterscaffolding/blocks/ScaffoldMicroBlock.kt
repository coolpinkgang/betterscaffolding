package com.romangraef.betterscaffolding.blocks

import com.romangraef.betterscaffolding.registries.BItems
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Property
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ScaffoldMicroBlock(settings: Settings) : Block(settings) {
    
    enum class PlankDirection : StringIdentifiable {
        NONE,
        NORTH_SOUTH,
        WEST_EAST;
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
        val POLE_N by property(BooleanProperty::of)
        val POLE_S by property(BooleanProperty::of)
        val POLE_W by property(BooleanProperty::of)
        val POLE_E by property(BooleanProperty::of)
        val PLANKS = EnumProperty.of("planks", PlankDirection::class.java)
        val CONNECTION_N by property(BooleanProperty::of)
        val CONNECTION_S by property(BooleanProperty::of)
        val CONNECTION_W by property(BooleanProperty::of)
        val CONNECTION_E by property(BooleanProperty::of)
        fun hasPoleSouthEast(state: BlockState) = state[POLE_S] || state[POLE_E]
        fun hasPoleNorthEast(state: BlockState) = state[POLE_N] || state[POLE_E]
        fun hasPoleSouthWest(state: BlockState) = state[POLE_S] || state[POLE_W]
        fun hasPoleNorthWest(state: BlockState) = state[POLE_N] || state[POLE_W]
        const val POLE_SIZE = 0.0625
        const val POLE_X = .0 + POLE_SIZE
        const val POLE_Y = .0 + POLE_SIZE
    }

    init {
        defaultState = getStateManager().defaultState
            .with(POLE_N, false)
            .with(POLE_S, false)
            .with(POLE_W, false)
            .with(POLE_E, false)
            .with(PLANKS, PlankDirection.NONE)
            .with(CONNECTION_N, false)
            .with(CONNECTION_S, false)
            .with(CONNECTION_W, false)
            .with(CONNECTION_E, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(POLE_N, POLE_S, POLE_W, POLE_E, PLANKS, CONNECTION_N, CONNECTION_S, CONNECTION_W, CONNECTION_E)
    }

    fun addPoleShape(shape: VoxelShape, state: BlockState): VoxelShape {
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
            shape = VoxelShapes.combine(shape, poleAt(0.0 + POLE_X, 0.0 + POLE_Y), BooleanBiFunction.OR)
        if (hasPoleSouthWest(state))
            shape = VoxelShapes.combine(shape, poleAt(0.0 + POLE_X, 1.0 - POLE_Y), BooleanBiFunction.OR)
        if (hasPoleNorthEast(state))
            shape = VoxelShapes.combine(shape, poleAt(1.0 - POLE_X, 0.0 + POLE_Y), BooleanBiFunction.OR)
        if (hasPoleSouthEast(state))
            shape = VoxelShapes.combine(shape, poleAt(1.0 - POLE_X, 1.0 - POLE_Y), BooleanBiFunction.OR)
        return shape
    }
    
    fun addConnectionShape(shape: VoxelShape, state: BlockState): VoxelShape {
        var shape = shape
        if (state[CONNECTION_N])
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(1.0/16.0, 13.0/16.0, 0.5/16, 15.0/16.0, 15.0/16.0, 1.5/16.0), BooleanBiFunction.OR)
        return shape
    }

    fun addPlankShape(shape: VoxelShape, state: BlockState): VoxelShape = when (state[PLANKS]!!) {
        PlankDirection.NONE -> shape
        PlankDirection.NORTH_SOUTH -> VoxelShapes.combine(
            shape,
            VoxelShapes.cuboid(1.0/8.0, 15.0 / 16.0, 0.0, 7.0/8.0, 1.0, 1.0),
            BooleanBiFunction.OR
        )
        PlankDirection.WEST_EAST -> VoxelShapes.combine(
            shape,
            VoxelShapes.cuboid(0.0, 15.0 / 16.0, 1.0/8.0, 1.0, 1.0, 7.0/8.0),
            BooleanBiFunction.OR
        )
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

    override fun getCollisionShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape = addPlankShape(VoxelShapes.empty(), state)
}