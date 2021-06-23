package com.romangraef.betterscaffolding.items

import com.romangraef.betterscaffolding.registries.REntities
import net.minecraft.entity.SpawnReason
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult

class ForkliftItem(settings: Settings) : Item(settings.maxCount(1)) {
    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        context ?: return super.useOnBlock(context)
        if (!context.player!!.isCreative) {
            context.stack.count--
        }
        if (!context.world.isClient) {
            val forklift = REntities.FORKLIFT.spawn(
                context.world as ServerWorld,
                null,
                null,
                context.player,
                context.blockPos,
                SpawnReason.SPAWN_EGG,
                true,
                false
            )!!
            forklift.setPosition(context.hitPos)
        }
        return ActionResult.success(true)
    }
}