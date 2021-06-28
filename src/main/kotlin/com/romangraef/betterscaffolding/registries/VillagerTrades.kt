package com.romangraef.betterscaffolding.registries

import com.romangraef.betterscaffolding.BetterScaffolding
import net.fabricmc.fabric.api.`object`.builder.v1.trade.TradeOfferHelper
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.village.TradeOffer
import net.minecraft.village.TradeOffers
import net.minecraft.village.VillagerProfession
import kotlin.random.asKotlinRandom

object VillagerTrades {
    fun MutableList<TradeOffers.Factory>.sell(
        toBeSold: ItemStack,
        forWhichPrice: IntRange,
        maxUses: Int,
        merchantExp: Int,
        priceMultiplier: Float
    ) {
        add(TradeOffers.Factory { entity, random ->
            BetterScaffolding.logger.info("Generating toolsmith stuff")
            TradeOffer(
                ItemStack(Items.EMERALD, forWhichPrice.random(random.asKotlinRandom())),
                toBeSold,
                maxUses,
                merchantExp,
                priceMultiplier
            )
        })
    }

    fun registerTrades() {
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 1) {
            BetterScaffolding.logger.info("Registering toolsmith stuff")
            it.sell(BItems.license.createEmptyLicense(), 30..50, 1, 10, 0.5f)
        }
    }
}