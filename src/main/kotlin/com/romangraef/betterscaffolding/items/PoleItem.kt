package com.romangraef.betterscaffolding.items

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
    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        context ?: return super.useOnBlock(context)
        if (context.hitsInsideBlock()) return super.useOnBlock(context)
        val world = context.world
        return when (context.side) {
            Direction.UP, Direction.DOWN -> {
                val x = context.hitPos.x - context.blockPos.x - 0.5
                val z = context.hitPos.z - context.blockPos.z - 0.5
                val direction = if (abs(z) < abs(x)) {
                    if (x < 0) Direction.WEST else Direction.EAST
                } else {
                    if (z < 0) Direction.NORTH else Direction.SOUTH
                }

                placeBlock(
                    context.player,
                    context.stack,
                    context.world, context.blockPos.offset(context.side), direction
                )
            }
            else -> placeBlock(
                context.player,
                context.stack,
                context.world,
                context.blockPos.offset(context.side),
                context.side.opposite
            )
        }
    }

    fun placeBlock(
        player: PlayerEntity?,
        itemStack: ItemStack,
        world: World,
        position: BlockPos,
        side: Direction
    ): ActionResult {
        if (!world.getBlockState(position).isAir) {
            return ActionResult.FAIL
        }
        if (!world.canSetBlock(position)) {
            return ActionResult.FAIL
        }
        if (!world.canPlayerModifyAt(player, position))
            return ActionResult.FAIL
        if (!world.isClient) {
            if (player?.isCreative != true)
                itemStack.count--
            // TODO: player?.incrementStat(Stat)
            world.setBlockState(position, BBlock.pole.defaultState)
        }
        return ActionResult.CONSUME_PARTIAL
    }

}
