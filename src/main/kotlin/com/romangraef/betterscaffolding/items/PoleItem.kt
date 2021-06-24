package com.romangraef.betterscaffolding.items

import com.romangraef.betterscaffolding.blocks.ScaffoldMicroBlock
import com.romangraef.betterscaffolding.registries.BBlock
import net.minecraft.block.BlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
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
            Direction.NORTH -> ScaffoldMicroBlock.POLE_N
            Direction.SOUTH -> ScaffoldMicroBlock.POLE_S
            Direction.WEST -> ScaffoldMicroBlock.POLE_W
            Direction.EAST -> ScaffoldMicroBlock.POLE_E
            else -> throw IllegalStateException()
        }
        internal fun Direction.toBlockStateConnectionField() = when (this) {
            Direction.NORTH -> ScaffoldMicroBlock.CONNECTION_N
            Direction.SOUTH -> ScaffoldMicroBlock.CONNECTION_S
            Direction.WEST -> ScaffoldMicroBlock.CONNECTION_W
            Direction.EAST -> ScaffoldMicroBlock.CONNECTION_E
            else -> throw IllegalStateException()
        }
    }
    
    /*context ?: return super.useOnBlock(context)
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
        var baseState = if (tmp.block == BBlock.scaffoldMicroBlock) tmp else BBlock.scaffoldMicroBlock.defaultState
        if (context.world.getBlockState(placePos))
        context.world.setBlockState(
            placePos,
            baseState.with(context.placeDirection.toBlockStateField(), true)
        )
    }
    return ActionResult.SUCCESS*/

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val placePos = context.blockPos.offset(context.side)
        if (!context.world.canSetBlock(placePos)) return ActionResult.FAIL
        if (!context.world.canPlayerModifyAt(context.player, placePos)) return ActionResult.FAIL
        val oldBlock = context.world.getBlockState(placePos)
        if (!oldBlock.isAir && oldBlock.block != BBlock.scaffoldMicroBlock) return ActionResult.FAIL
        val blockUnderneathPos: BlockPos =
            if (context.side == Direction.UP) context.blockPos else placePos.offset(Direction.DOWN)
        val blockUnderneath: BlockState = context.world.getBlockState(blockUnderneathPos)
        if (blockUnderneath.isAir) return ActionResult.FAIL
        if (blockUnderneath.block == BBlock.scaffoldMicroBlock) {
            if (!(blockUnderneath[context.placeDirection.toBlockStateField()]
                    || blockUnderneath[ScaffoldMicroBlock.PLANKS] != ScaffoldMicroBlock.PlankDirection.NONE)
            ) return ActionResult.FAIL else context.world.setBlockState(
                blockUnderneathPos,
                blockUnderneath.with(context.placeDirection.toBlockStateConnectionField(), false)
            )
        }
        if (!context.world.isClient) {
            val blockAbovePos = placePos.offset(Direction.UP)
            val blockAbove = context.world.getBlockState(blockAbovePos)
            val baseState =
                if (oldBlock.block == BBlock.scaffoldMicroBlock) oldBlock else BBlock.scaffoldMicroBlock.defaultState
            context.world.setBlockState(
                placePos,
                baseState.with(context.placeDirection.toBlockStateField(), true)
                    .with(context.placeDirection.toBlockStateField(), blockAbove.block == BBlock.scaffoldMicroBlock)
            )
            if (context.player?.isCreative != true)
                context.stack.count--
        }
        return ActionResult.SUCCESS
    }

}
