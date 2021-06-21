package com.romangraef.betterscaffolding.items

import com.romangraef.betterscaffolding.blocks.ScaffoldMicroBlock
import com.romangraef.betterscaffolding.registries.BBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import kotlin.math.abs

class PoleItem(settings: Settings) : Item(settings) {

    companion object {
        internal val ItemUsageContext.placeDirection get() = when (side) {
            Direction.UP, Direction.DOWN -> {
                val x = hitPos.x - blockPos.x - 0.5
                val z = hitPos.z - blockPos.z - 0.5
                if (abs(z) < abs(x)) {
                    if (x < 0) Direction.WEST else Direction.EAST
                } else {
                    if (z < 0) Direction.NORTH else Direction.SOUTH
                }
            }
            else -> side
        }
        internal fun Direction.toBlockStateField() = when (this) {
            Direction.NORTH -> ScaffoldMicroBlock.POLE_NORTH
            Direction.SOUTH -> ScaffoldMicroBlock.POLE_SOUTH
            Direction.WEST -> ScaffoldMicroBlock.POLE_WEST
            Direction.EAST -> ScaffoldMicroBlock.POLE_EAST
            else -> throw IllegalStateException()
        }
    }

    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        context ?: return super.useOnBlock(context)
        if (context.hitsInsideBlock()) return super.useOnBlock(context)
        if (!context.world.getBlockState(context.blockPos).isAir) {
            return ActionResult.FAIL
        }
        if (!context.world.canSetBlock(context.blockPos)) {
            return ActionResult.FAIL
        }
        if (!context.world.canPlayerModifyAt(context.player, context.blockPos))
            return ActionResult.FAIL
        if (!context.world.isClient) {
            if (context.player?.isCreative != true)
                context.stack.count--
            // TODO: player?.incrementStat(Stat)
            context.world.setBlockState(
                context.blockPos,
                BBlock.scaffoldMicroBlock.defaultState.with(context.placeDirection.toBlockStateField(), true)
            )
        }
        return ActionResult.CONSUME_PARTIAL
    }

}
