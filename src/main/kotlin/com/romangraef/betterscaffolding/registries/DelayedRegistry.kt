package com.romangraef.betterscaffolding.registries

import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import kotlin.properties.ReadOnlyProperty

abstract class DelayedRegistry<T>(val modid: String) {
    val registeredObjects = mutableMapOf<Identifier, () -> T>()
    val instantiatedObjects = mutableMapOf<Identifier, T>()
    operator fun <R : T> String.invoke(block: () -> R): ReadOnlyProperty<Any?, R> {
        val id = Identifier(modid, this)
        registeredObjects[id] = block
        return ReadOnlyProperty { _, _ -> instantiatedObjects[id] as R }
    }

    abstract fun register(identifier: Identifier, obj: T)
    fun registerAll() {
        registeredObjects.forEach { (id, obj) ->
            obj().also {
                instantiatedObjects[id] = it
                register(id, it)
            }
        }
    }
}

abstract class DefaultDelayedRegistry<T>(val registry: Registry<T>, modid: String) : DelayedRegistry<T>(modid) {
    override fun register(identifier: Identifier, obj: T) {
        Registry.register(registry, identifier, obj)
    }
}