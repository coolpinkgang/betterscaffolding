package com.romangraef.betterscaffolding.items

import com.romangraef.betterscaffolding.BetterScaffolding
import com.romangraef.betterscaffolding.blocks.Scaffolding
import com.romangraef.betterscaffolding.items.PlankItem.Companion.plankDirection
import com.romangraef.betterscaffolding.items.PlankItem.Companion.roundedLookDirection
import com.romangraef.betterscaffolding.registries.BBlock
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Direction

class StairsItem(settings: Settings) : Item(settings) {
    
    companion object {
        val Direction.stairDirection: Scaffolding.PlankState get() = when (this) {
            Direction.NORTH -> Scaffolding.PlankState.STAIR_NORTH_SOUTH
            Direction.SOUTH -> Scaffolding.PlankState.STAIR_SOUTH_NORTH
            Direction.WEST  -> Scaffolding.PlankState.STAIR_WEST_EAST
            Direction.EAST  -> Scaffolding.PlankState.STAIR_EAST_WEST
            else -> throw IllegalStateException()
        }
    }
    
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val player = context.player ?: return ActionResult.FAIL
        val lookingDirection = player.roundedLookDirection
        val plankDirection = lookingDirection.plankDirection
        val firstPos = context.blockPos ?: return ActionResult.FAIL
        val world = context.world ?: return ActionResult.FAIL
        val stack = context.stack ?: return ActionResult.FAIL
        if (!PlankItem.hasValidFirstPos(context)) return ActionResult.FAIL
        var first = false
        val toUpdate = generateSequence(firstPos) { it.offset(lookingDirection).offset(Direction.UP) }
            .map { Triple(it, world.getBlockState(it), world.getBlockState(it.offset(Direction.DOWN))) }
            .takeWhile { (_, state, bottom) ->
                if (first) { first = false; return@takeWhile true }
                if (state.block == Scaffolding.Block) {
                    if (Scaffolding.Block.hasPole(lookingDirection.toPolePosition(), state))
                        return@takeWhile false
                    if (state[Scaffolding.States.PLANK] == lookingDirection.plankDirection)
                        return@takeWhile false
                }
                if (bottom.block == Scaffolding.Block)
                    if (Scaffolding.Block.hasPole(lookingDirection.opposite.toPolePosition(), bottom))
                        if (Scaffolding.Block.hasPlank(bottom, plankDirection))
                            return@takeWhile false
                if (state.isAir) return@takeWhile true
                false
            }.take(BetterScaffolding.config.groupScaffolding.maxLength).toList()
        //TODO: check if last pos is valid
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
                    it.with(Scaffolding.States.PLANK, lookingDirection.stairDirection)
                }
            }
            if (!player.isCreative) {
                stack.count -= toUpdate.size
            }
        }
        return ActionResult.SUCCESS
    }
    
}