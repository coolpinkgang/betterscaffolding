package com.romangraef.betterscaffolding.registries

import com.romangraef.betterscaffolding.BetterScaffolding
import net.minecraft.sound.SoundEvent
import net.minecraft.util.registry.Registry

object BSounds : DefaultDelayedRegistry<SoundEvent>(Registry.SOUND_EVENT, BetterScaffolding.modid) {
    val forkliftMove by sound("forklift_move")

    fun sound(name: String) = name {
        SoundEvent(BetterScaffolding.id(name))
    }

}