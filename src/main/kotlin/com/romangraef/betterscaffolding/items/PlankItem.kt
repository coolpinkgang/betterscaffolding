package com.romangraef.betterscaffolding.items

import com.romangraef.betterscaffolding.BetterScaffolding
import com.romangraef.betterscaffolding.blocks.Scaffolding
import com.romangraef.betterscaffolding.registries.BBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Direction

class PlankItem(settings: Settings) : Item(settings) {

    companion object {
        val PlayerEntity.roundedLookDirection: Direction
            get() {
                val yaw = Math.floorMod(yaw.toInt(), 360)
                if (yaw <= 45) return Direction.SOUTH
                if (yaw <= 135) return Direction.WEST
                if (yaw <= 225) return Direction.NORTH
                if (yaw <= 315) return Direction.EAST
                return Direction.SOUTH
            }
        val Direction.plankDirection: Scaffolding.PlankState
            get() = when {
                this == Direction.NORTH || this == Direction.SOUTH -> Scaffolding.PlankState.NORTH_SOUTH
                this == Direction.WEST || this == Direction.EAST -> Scaffolding.PlankState.WEST_EAST
                else -> throw IllegalStateException()
            }
        fun hasValidFirstPos(context: ItemUsageContext): Boolean {
            val firstState = context.world.getBlockState(context.blockPos)
            if (firstState.block != BBlock.scaffoldMicroBlock) return false
            if (Scaffolding.Block.hasPole(context.player!!.roundedLookDirection.opposite.toPolePosition(), firstState)) return true
            if (Scaffolding.Block.hasPole(context.player!!.roundedLookDirection.toPolePosition(), firstState))
                return Scaffolding.Block.hasPlank(firstState, context.player!!.roundedLookDirection.plankDirection)
            return false
        }
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val player = context.player ?: return ActionResult.FAIL
        val lookingDirection = player.roundedLookDirection
        val plankDirection = lookingDirection.plankDirection
        val firstPos = context.blockPos ?: return ActionResult.FAIL
        val world = context.world ?: return ActionResult.FAIL
        val stack = context.stack ?: return ActionResult.FAIL
        if (!hasValidFirstPos(context)) return ActionResult.FAIL
        var first = true
        val toUpdate = generateSequence(firstPos) { it.offset(lookingDirection) }
            .map { it to world.getBlockState(it) }
            .takeWhile { (_, state) ->
                if (first) { first = false; return@takeWhile true }
                if (state.isAir) return@takeWhile true
                if (state.block != BBlock.scaffoldMicroBlock) return@takeWhile false
                if (Scaffolding.Block.hasPole(lookingDirection.toPolePosition(), state)) return@takeWhile true
                if (Scaffolding.Block.hasPole(lookingDirection.opposite.toPolePosition(), state))
                    return@takeWhile Scaffolding.Block.hasPlank(state, plankDirection)
                return@takeWhile false
            }
            .take(BetterScaffolding.config.groupScaffolding.maxLength)
            .toList()
            .dropLastWhile { (_, state) ->
                if (state.block != Scaffolding.Block) return@dropLastWhile true
                if (Scaffolding.Block.hasPole(lookingDirection.toPolePosition(), state)) return@dropLastWhile false
                if (Scaffolding.Block.hasPlank(state, lookingDirection.plankDirection)) return@dropLastWhile false
                return@dropLastWhile true
            }
        if (toUpdate.isEmpty() || toUpdate.size < BetterScaffolding.config.groupScaffolding.minLength) return ActionResult.FAIL
        if (toUpdate.any { (pos, _) ->
                !world.canPlayerModifyAt(
                    player,
                    pos
                ) || !world.canSetBlock(pos)
            }) return ActionResult.FAIL
        if (toUpdate.size > stack.count && !player.isCreative) {
            player.sendMessage(
                BetterScaffolding.error("notenoughplanks"), true
            )
            return ActionResult.FAIL
        }
        if (!world.isClient) {
            toUpdate.forEach { (pos, _) ->
                BBlock.scaffoldMicroBlock.setMicroblock(world, pos) {
                    it.with(Scaffolding.States.PLANK, plankDirection)
                }
            }
            if (!player.isCreative) {
                stack.count -= toUpdate.size
            }
        }
        return ActionResult.success(true)
    }

}