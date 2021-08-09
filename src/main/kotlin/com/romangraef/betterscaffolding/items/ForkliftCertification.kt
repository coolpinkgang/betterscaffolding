package com.romangraef.betterscaffolding.items

import com.romangraef.betterscaffolding.BetterScaffolding
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import java.util.*

private val PlayerInventory.all: Sequence<ItemStack?>
    get() = (0..this.size()).asSequence().map { this.getStack(it) }

class ForkliftCertification(settings: Settings) : Item(settings.maxCount(1)) {


    override fun appendStacks(group: ItemGroup, stacks: DefaultedList<ItemStack>) {
        super.appendStacks(group, stacks)
        if (isIn(group)) {
            stacks.add(createEmptyLicense())
        }
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        val tag = stack.orCreateTag
        val blanco = tag.contains(EMPTY) && tag.getBoolean(EMPTY)
        if (blanco) {
            tooltip.add(TranslatableText("betterscaffolding.info.license.later_attribution"))
            return
        }
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

    fun createLicense(playerEntity: PlayerEntity): ItemStack =
        ItemStack(this).also {
            it.orCreateTag?.also {
                it.putUuid(PLAYER_UUID, playerEntity.uuid)
                it.putString(PLAYER_NAME, playerEntity.name.asString())
                it.putLong(OBTAINED_DATE, Date().time)
            }
        }

    override fun inventoryTick(stack: ItemStack, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        val tag = stack.orCreateTag ?: return
        if (tag.contains(EMPTY) && tag.getBoolean(EMPTY)) {
            tag.putBoolean(EMPTY, false)
            tag.putUuid(PLAYER_UUID, entity.uuid)
            tag.putString(PLAYER_NAME, entity.entityName)
            tag.putLong(OBTAINED_DATE, Date().time)
        }
    }

    fun isPlayerCertified(player: PlayerEntity): Boolean =
        player.inventory.all.filterNotNull()
            .filter { it.item == this }
            .filter { this.isPlayerCertified(it, player) }.any()

    private fun isPlayerCertified(it: ItemStack, player: PlayerEntity): Boolean {
        val tag = it.orCreateTag ?: return false
        if (!tag.containsUuid(PLAYER_UUID)) return false
        return tag.getUuid(PLAYER_UUID) == player.uuid
    }

    fun createEmptyLicense(): ItemStack = ItemStack(this).also {
        it.orCreateTag.also {
            it.putBoolean(EMPTY, true)
        }
    }

    companion object {
        const val PLAYER_UUID = "playerUUID"
        const val EMPTY = "blanco"
        const val PLAYER_NAME = "playerName"
        const val OBTAINED_DATE = "obtainedAt"
    }
}