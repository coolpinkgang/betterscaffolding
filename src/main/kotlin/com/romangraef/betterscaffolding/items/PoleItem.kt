package com.romangraef.betterscaffolding.items

import com.romangraef.betterscaffolding.blocks.ScaffoldMicroBlock
import com.romangraef.betterscaffolding.items.PoleItem.Companion.placeDirection
import com.romangraef.betterscaffolding.items.PoleItem.Companion.toBlockStateField
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
        val placePos =
            if (context.world.getBlockState(context.blockPos).block == BBlock.scaffoldMicroBlock)
                context.blockPos.offset(Direction.UP) else context.blockPos.offset(context.side)
        if (!(context.world.getBlockState(placePos).isAir
                    || context.world.getBlockState(placePos).block == BBlock.scaffoldMicroBlock))
            return ActionResult.FAIL
        if (!context.world.canSetBlock(placePos)) return ActionResult.FAIL
        if (!context.world.canPlayerModifyAt(context.player, placePos)) return ActionResult.FAIL
        if (!context.world.isClient) {
            if (context.player?.isCreative != true)
                context.stack.count--
            // TODO: player?.incrementStat(Stat)
            val tmp = context.world.getBlockState(placePos)
            val baseState = if (tmp.block == BBlock.scaffoldMicroBlock) tmp else BBlock.scaffoldMicroBlock.defaultState
            context.world.setBlockState(
                placePos,
                baseState.with(context.placeDirection.toBlockStateField(), true)
            )
        }
        return ActionResult.SUCCESS
    }

}
