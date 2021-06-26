package com.romangraef.betterscaffolding.items

import com.romangraef.betterscaffolding.blocks.ScaffoldMicroBlock
import com.romangraef.betterscaffolding.registries.BBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.Style
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
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
        val Direction.plankDirection: ScaffoldMicroBlock.PlankDirection
            get() = when {
                this == Direction.NORTH || this == Direction.SOUTH -> ScaffoldMicroBlock.PlankDirection.NORTH_SOUTH
                this == Direction.WEST || this == Direction.EAST -> ScaffoldMicroBlock.PlankDirection.WEST_EAST
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
        val toUpdate = generateSequence(firstPos) {
            it.offset(lookingDirection)
        }
            .map { it to world.getBlockState(it) }
            .takeWhile { (_, state) ->
                state.isAir ||
                        (state.block == BBlock.scaffoldMicroBlock &&
                                state[ScaffoldMicroBlock.PLANKS] == ScaffoldMicroBlock.PlankDirection.NONE)
            }
            .take(4)
            .toList()
            .dropLastWhile { (_, state) -> state.block != BBlock.scaffoldMicroBlock }

        if (toUpdate.isEmpty()) return ActionResult.FAIL
        if (toUpdate.any { (pos, _) ->
                !world.canPlayerModifyAt(
                    player,
                    pos
                ) || !world.canSetBlock(pos)
            }) return ActionResult.FAIL
        if (toUpdate.size > stack.count && !player.isCreative) {
            player.sendMessage(
                TranslatableText("betterscaffolding.error.notenoughplanks").setStyle(
                    Style.EMPTY.withColor(
                        Formatting.DARK_RED
                    )
                ), true
            )
            return ActionResult.FAIL
        }
        if (!world.isClient) {
            toUpdate.forEach { (pos, _) ->
                BBlock.scaffoldMicroBlock.setMicroblock(world, pos) {
                    it.with(ScaffoldMicroBlock.PLANKS, plankDirection)
                }
            }
            if (!player.isCreative) {
                stack.count -= toUpdate.size
            }
        }
        return ActionResult.success(true)
    }

}