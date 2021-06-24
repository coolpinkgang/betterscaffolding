package com.romangraef.betterscaffolding.items

import com.romangraef.betterscaffolding.blocks.ScaffoldMicroBlock
import com.romangraef.betterscaffolding.items.PoleItem.Companion.toBlockStateConnectionField
import com.romangraef.betterscaffolding.items.PoleItem.Companion.toBlockStateField
import com.romangraef.betterscaffolding.registries.BBlock
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class PlankItem(settings: Settings) : Item(settings) {
    
    companion object {
        val PlayerEntity.roundedLookDirection: Direction get() = when {
            -45 < yaw && yaw <= 45                                   -> Direction.SOUTH
            45 < yaw && yaw <= 135                                   -> Direction.WEST
            (135 < yaw && yaw <= 180) || (-180 < yaw && yaw <= -135) -> Direction.NORTH
            -135 < yaw && yaw <= -45                                 -> Direction.EAST
            else                                                     -> throw IllegalStateException()
        }
        val Direction.plankDirection: ScaffoldMicroBlock.PlankDirection get() = when {
            this == Direction.NORTH || this == Direction.SOUTH -> ScaffoldMicroBlock.PlankDirection.NORTH_SOUTH
            this == Direction.WEST  || this == Direction.EAST  -> ScaffoldMicroBlock.PlankDirection.WEST_EAST
            else -> throw IllegalStateException()
        }
    }
    
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if (!context.world.canSetBlock(context.blockPos)) return ActionResult.FAIL
        if (!context.world.canPlayerModifyAt(context.player, context.blockPos)) return ActionResult.FAIL
        val toUpdate = mutableListOf<Pair<BlockPos, BlockState>>(Pair(
            context.blockPos,
            context.world.getBlockState(context.blockPos)
        ))
        if (toUpdate.first().second.block != BBlock.scaffoldMicroBlock) return ActionResult.FAIL
        val direction: Direction = context.player!!.roundedLookDirection
        val connectionUpdate = mutableListOf<Triple<BlockPos, BlockState, Direction>>()
        if (toUpdate.first().second[direction.toBlockStateField()]) {
            connectionUpdate += Triple(toUpdate.first().first, toUpdate.first().second, direction)
            toUpdate.removeAt(0)
        } else if (toUpdate.first().second[direction.opposite.toBlockStateField()]
            && toUpdate.first().second[ScaffoldMicroBlock.PLANKS] != ScaffoldMicroBlock.PlankDirection.NONE) {
            connectionUpdate += Triple(toUpdate.first().first, toUpdate.first().second, direction.opposite)
        } else toUpdate.removeAt(0)
        for (i in 0..4) {
            val pos = toUpdate[toUpdate.lastIndex].first.offset(direction)
            val block = context.world.getBlockState(pos)
            if (!context.world.canSetBlock(pos)) return ActionResult.FAIL
            if (!context.world.canPlayerModifyAt(context.player, pos)) return ActionResult.FAIL
            if (!block.isAir) {
                if (block.block == BBlock.scaffoldMicroBlock) {
                    if (block[direction.opposite.toBlockStateField()])
                        connectionUpdate += Triple(pos, block, direction.opposite)
                    else if (block[direction.toBlockStateField()])
                        connectionUpdate += Triple(pos, block, direction)
                        toUpdate += Pair(pos, block)
                    break
                } else return ActionResult.FAIL
            }
            toUpdate += Pair(pos, block)
        }
        if (toUpdate.size > context.stack.count) return ActionResult.FAIL
        if (!context.world.isClient) {
            connectionUpdate.forEach {
                context.world.setBlockState(it.first, it.second.with(it.third.toBlockStateConnectionField(), true))
            }
            toUpdate.forEach {
                val base = if (it.second.block == BBlock.scaffoldMicroBlock)
                    it.second else BBlock.scaffoldMicroBlock.defaultState
                context.world.setBlockState(it.first, base.with(ScaffoldMicroBlock.PLANKS, direction.plankDirection))
                if (!context.player!!.isCreative)
                    context.stack.count--
            }
        }
        return ActionResult.SUCCESS
    }

}