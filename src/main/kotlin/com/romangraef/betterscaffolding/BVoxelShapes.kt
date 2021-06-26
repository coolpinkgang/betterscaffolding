package com.romangraef.betterscaffolding

import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

object BVoxelShapes {
    fun cuboidA(minX: Number, minY: Number, minZ: Number, maxX: Number, maxY: Number, maxZ: Number) =
        VoxelShapes.cuboid(
            minX.toDouble()/16.0,
            minY.toDouble()/16.0,
            minZ.toDouble()/16.0,
            maxX.toDouble()/16.0,
            maxY.toDouble()/16.0,
            maxZ.toDouble()/16.0
        )
    fun cuboidB(minX: Number, minY: Number, minZ: Number, sizeX: Number, height: Number, sizeZ: Number) =
        cuboidA(
            minX, minY, minZ,
            minX.toDouble() + sizeX.toDouble(),
            minY.toDouble() + height.toDouble(),
            minZ.toDouble() + sizeZ.toDouble()
        )
    fun combine(vararg shapes: VoxelShape, function: BooleanBiFunction): VoxelShape {
        var shape = shapes.first()
        for (s in shapes.iterator().also { it.next() }) {
            shape = VoxelShapes.combine(shape, s, function)
        }
        return shape
    }
    
}