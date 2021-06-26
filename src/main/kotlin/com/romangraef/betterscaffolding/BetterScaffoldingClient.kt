package com.romangraef.betterscaffolding

import com.romangraef.betterscaffolding.blocks.ScaffoldMicroBlockModelProvider
import com.romangraef.betterscaffolding.entities.ForkliftRenderer
import com.romangraef.betterscaffolding.registries.REntities
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object BetterScaffoldingClient : ClientModInitializer {
    object Keybindings {
        lateinit var forkliftUp: KeyBinding
        lateinit var forkliftDown: KeyBinding
    }

    override fun onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider { ScaffoldMicroBlockModelProvider }
        EntityRendererRegistry.INSTANCE.register(REntities.FORKLIFT) { ForkliftRenderer(it) }
        Keybindings.forkliftDown = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.betterscaffolding.forkliftdown",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "key.betterscaffolding.category"
            )
        )
        Keybindings.forkliftUp = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.betterscaffolding.forkliftup",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "key.betterscaffolding.category"
            )
        )
    }
}