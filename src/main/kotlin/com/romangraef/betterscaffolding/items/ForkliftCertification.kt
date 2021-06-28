package com.romangraef.betterscaffolding.items

import com.romangraef.betterscaffolding.BetterScaffolding
import com.romangraef.betterscaffolding.registries.BItems
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.world.World
import java.util.*

class ForkliftCertification(settings: Settings) : Item(settings.maxCount(1)) {
    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        val tag = stack.orCreateTag
        val playerName =
            if (tag.contains(PLAYER_NAME, NbtCompound.STRING_TYPE.toInt())) tag.getString(PLAYER_NAME) else null
        val obtainedDate = if (tag.contains(
                OBTAINED_DATE,
                NbtCompound.LONG_TYPE.toInt()
            )
        ) Date(tag.getLong(OBTAINED_DATE)) else null
        if (playerName == null || obtainedDate == null) {
            tooltip.add(BetterScaffolding.error("license.obtain_via"))
            return
        }
        tooltip.add(TranslatableText("betterscaffolding.info.license.player_name", playerName))
        tooltip.add(TranslatableText("betterscaffolding.info.license.obtained_date", obtainedDate))
    }

    companion object {
        fun createLicense(playerEntity: PlayerEntity): ItemStack =
            ItemStack(BItems.license).also {
                it.orCreateTag?.also {
                    it.putUuid(PLAYER_UUID, playerEntity.uuid)
                    it.putString(PLAYER_NAME, playerEntity.name.asString())
                    it.putLong(OBTAINED_DATE, Date().time)
                }
            }


        const val PLAYER_UUID = "playerUUID"
        const val PLAYER_NAME = "playerName"
        const val OBTAINED_DATE = "obtainedAt"
    }
}