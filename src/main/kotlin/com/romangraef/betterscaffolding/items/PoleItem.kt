package com.romangraef.betterscaffolding.items

import com.romangraef.betterscaffolding.blocks.Scaffolding
import com.romangraef.betterscaffolding.registries.BBlock
import net.minecraft.block.BlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import kotlin.math.abs

fun Direction.toPolePosition() = when (this) {
    Direction.NORTH -> Scaffolding.PolePosition.NORTH
    Direction.SOUTH -> Scaffolding.PolePosition.SOUTH
    Direction.WEST -> Scaffolding.PolePosition.WEST
    Direction.EAST -> Scaffolding.PolePosition.EAST
    else -> throw IllegalStateException()
}

class PoleItem(settings: Settings) : Item(settings) {

    companion object {
        internal val ItemUsageContext.placeDirection
            get() = when (side) {
                Direction.UP, Direction.DOWN -> {
                    val x = hitPos.x - blockPos.x - 0.5
                    val z = hitPos.z - blockPos.z - 0.5
                    if (abs(z) < abs(x)) {
                        if (x < 0) Direction.WEST else Direction.EAST
                    } else {
                        if (z < 0) Direction.NORTH else Direction.SOUTH
                    }
                }
                else -> side.opposite
            }
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val placePos = context.blockPos.offset(context.side)
        if (!context.world.canSetBlock(placePos)) return ActionResult.FAIL
        if (!context.world.canPlayerModifyAt(context.player, placePos)) return ActionResult.FAIL
        val oldBlock = context.world.getBlockState(placePos)
        if (!oldBlock.isAir && oldBlock.block != BBlock.scaffoldMicroBlock) return ActionResult.FAIL
        val blockUnderneathPos: BlockPos = placePos.offset(Direction.DOWN)
        val blockUnderneath: BlockState = context.world.getBlockState(blockUnderneathPos)
        if (blockUnderneath.isAir) return ActionResult.FAIL
        if (blockUnderneath.block == BBlock.scaffoldMicroBlock)
            if (!(BBlock.scaffoldMicroBlock.hasPole(context.placeDirection.toPolePosition(), blockUnderneath) ||
                        BBlock.scaffoldMicroBlock.hasPlanks(blockUnderneath))
            )
                return ActionResult.FAIL
        if (!context.world.isClient) {
            BBlock.scaffoldMicroBlock.setMicroblock(context.world, placePos) {
                it.with(context.placeDirection.toPolePosition().toProperty(), Scaffolding.PoleState.BASE)
            }
            if (context.player?.isCreative != true)
                context.stack.count--
        }
        return ActionResult.SUCCESS
    }

}
