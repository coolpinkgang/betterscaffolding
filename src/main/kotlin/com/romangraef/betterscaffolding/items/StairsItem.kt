package com.romangraef.betterscaffolding.items

import com.romangraef.betterscaffolding.BetterScaffolding
import com.romangraef.betterscaffolding.blocks.Scaffolding
import com.romangraef.betterscaffolding.items.PlankItem.Companion.plankDirection
import com.romangraef.betterscaffolding.items.PlankItem.Companion.roundedLookDirection
import com.romangraef.betterscaffolding.registries.BBlock
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class StairsItem(settings: Settings) : Item(settings) {

    companion object {
        val Direction.stairDirection: Scaffolding.PlankState
            get() = when (this) {
                Direction.NORTH -> Scaffolding.PlankState.STAIR_SOUTH_NORTH
                Direction.SOUTH -> Scaffolding.PlankState.STAIR_NORTH_SOUTH
                Direction.WEST -> Scaffolding.PlankState.STAIR_EAST_WEST
                Direction.EAST -> Scaffolding.PlankState.STAIR_WEST_EAST
                else -> throw IllegalStateException()
            }
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val player = context.player ?: return ActionResult.FAIL
        val lookingDirection = player.roundedLookDirection
        val plankDirection = lookingDirection.plankDirection
        val world = context.world ?: return ActionResult.FAIL
        val stack = context.stack ?: return ActionResult.FAIL
        val firstPosition = context.blockPos.offset(context.side) // TODO: allow placing within a microblock
        if (!isValidFirstPosition(world, firstPosition, lookingDirection))
            return ActionResult.FAIL
        val potentialSpots = generateSequence(firstPosition) { it.offset(lookingDirection).offset(Direction.UP) }
            .takeWhile { pos ->
                val state = world.getBlockState(pos)
                if (state.isAir) return@takeWhile true
                if (state.block != BBlock.scaffoldMicroBlock) return@takeWhile false
                if (BBlock.scaffoldMicroBlock.hasPlanks(state)) return@takeWhile false
                true
            }
            .take(3) // TODO: CONFIG OPTION
            .toList()
        val toUpdate = getCutoffList(world, lookingDirection, potentialSpots)
        if (toUpdate.isEmpty() || toUpdate.size < 1) // TODO: CONFIG OPTION
            return ActionResult.FAIL
        if (toUpdate.any { pos -> !world.canPlayerModifyAt(player, pos) || !world.canSetBlock(pos) })
            return ActionResult.FAIL

        if (toUpdate.size > stack.count && !player.isCreative) {
            player.sendMessage(BetterScaffolding.error("notenoughstairs"), true)
            return ActionResult.FAIL
        }
        if(!world.isClient){
            toUpdate.forEach { pos->
                BBlock.scaffoldMicroBlock.setMicroblock(world, pos) {
                    it.with(Scaffolding.States.PLANK, lookingDirection.stairDirection)
                }
            }
            if(!player.isCreative)
                stack.count-=toUpdate.size
        }
        return ActionResult.SUCCESS
    }

    private fun getCutoffList(world: World, lookingDirection: Direction, list: List<BlockPos>): List<BlockPos> {
        for (i in list.indices) {
            if (isValidLastPosition(world, list[i], lookingDirection)) {
                return list.subList(0, i + 1)
            }
        }
        return emptyList()
    }

    private fun isValidLastPosition(world: World, blockPos: BlockPos, lookingDirection: Direction): Boolean {
        val currentBlock = world.getBlockState(blockPos)
        if (currentBlock.block == BBlock.scaffoldMicroBlock && BBlock.scaffoldMicroBlock.hasPole(
                lookingDirection.toPolePosition(),
                currentBlock
            )
        )
            return true
        val nextPosition = world.getBlockState(blockPos.offset(lookingDirection))
        if (nextPosition.isAir) return false
        if (nextPosition.block == BBlock.scaffoldMicroBlock && !BBlock.scaffoldMicroBlock.hasPlank(
                nextPosition,
                lookingDirection.plankDirection
            )
        )
            return false
        return true
    }

    private fun isValidFirstPosition(
        world: World,
        firstPosition: BlockPos,
        lookingDirection: Direction
    ): Boolean {
        val belowFirstPosition = world.getBlockState(firstPosition.offset(Direction.DOWN))
        val belowOneOver = world.getBlockState(firstPosition.offset(Direction.DOWN).offset(lookingDirection.opposite))
        if (belowOneOver.block == BBlock.scaffoldMicroBlock) {
            if (BBlock.scaffoldMicroBlock.hasPlank(belowOneOver, lookingDirection.plankDirection))
                return true
        }
        if (belowFirstPosition.isAir) return false
        if (belowFirstPosition.block == BBlock.scaffoldMicroBlock) {
            if (BBlock.scaffoldMicroBlock.hasPole(lookingDirection.opposite.toPolePosition(), belowFirstPosition))
                return true
            if (BBlock.scaffoldMicroBlock.hasPlank(belowFirstPosition, lookingDirection.plankDirection))
                return true
            return false
        }
        return true
    }

}