package com.romangraef.betterscaffolding

import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

object BVoxelShapes {
    fun cuboidA(
        minX: Number,
        minY: Number,
        minZ: Number,
        maxX: Number,
        maxY: Number,
        maxZ: Number,
        switchXZ: Boolean = false,
    ) =
        VoxelShapes.cuboid(
            (if (switchXZ) minZ else minX).toDouble() / 16.0,
            minY.toDouble() / 16.0,
            (if (switchXZ) minX else minZ).toDouble() / 16.0,
            (if (switchXZ) maxZ else maxX).toDouble() / 16.0,
            maxY.toDouble() / 16.0,
            (if (switchXZ) maxX else maxZ).toDouble() / 16.0
        )

    fun cuboidB(
        minX: Number,
        minY: Number,
        minZ: Number,
        sizeX: Number,
        height: Number,
        sizeZ: Number,
        negateX: Boolean = false,
        negateZ: Boolean = false,
        switchXZ: Boolean = false,
    ): VoxelShape {
        var minX = minX
        if (negateX) minX = 16.0 - minX.toDouble()
        var minZ = minZ
        if (negateZ) minZ = 16.0 - minZ.toDouble()
        return cuboidA(
            minX,
            minY,
            minZ,
            minX.toDouble() + sizeX.toDouble(),
            minY.toDouble() + height.toDouble(),
            minZ.toDouble() + sizeZ.toDouble(),
            switchXZ
        )
    }

    fun combine(vararg shapes: VoxelShape, function: BooleanBiFunction): VoxelShape {
        var shape = shapes.first()
        for (s in shapes.iterator().also { it.next() }) {
            shape = VoxelShapes.combine(shape, s, function)
        }
        return shape
    }

}

operator fun VoxelShape.plus(other: VoxelShape) = VoxelShapes.combine(this, other, BooleanBiFunction.OR)!!
operator fun VoxelShape.times(other: VoxelShape) = VoxelShapes.combine(this, other, BooleanBiFunction.AND)!!
