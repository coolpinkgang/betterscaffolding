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
        switchXZ: Boolean = false
    ): VoxelShape {
        return VoxelShapes.cuboid(
            (if (switchXZ) minZ else minX).toDouble() / 16.0,
            minY.toDouble() / 16.0,
            (if (switchXZ) minX else minZ).toDouble() / 16.0,
            (if (switchXZ) maxZ else maxX).toDouble() / 16.0,
            maxY.toDouble() / 16.0,
            (if (switchXZ) maxX else maxZ).toDouble() / 16.0
        )
    }
    
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
        var minX = if (negateX) 16.0 - sizeX.toDouble() - minX.toDouble() else minX.toDouble()
        val maxX = minX + sizeX.toDouble()
        var minZ = if (negateZ) 16.0 - sizeZ.toDouble() - minZ.toDouble() else minZ.toDouble()
        val maxZ = minZ + sizeZ.toDouble()
        return cuboidA(
            minX,
            minY,
            minZ,
            maxX,
            minY.toDouble() + height.toDouble(),
            maxZ,
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

class VoxelShapeBuilder(internal var shape: VoxelShape) {
    
    operator fun plusAssign(other: VoxelShape) {
        shape += other
    }
    
    operator fun timesAssign(other: VoxelShape) {
        shape *= other
    }
}

fun buildShape(start: VoxelShape = VoxelShapes.empty(), block: (VoxelShapeBuilder) -> Unit): VoxelShape =
    VoxelShapeBuilder(start).apply(block).shape
