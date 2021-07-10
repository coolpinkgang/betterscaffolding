package com.romangraef.betterscaffolding

import com.romangraef.betterscaffolding.blocks.Scaffolding
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Property
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.ChunkPos
import kotlin.properties.ReadOnlyProperty
import kotlin.random.Random
import kotlin.reflect.KProperty

fun <T : Property<*>> property(getter: (String) -> T) = object : ReadOnlyProperty<Any, T> {
    private var _value: T? = null
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return this._value ?: kotlin.run {
            _value = getter(property.name.lowercase())
            _value!!
        }
    }
}

inline fun <reified T> enumProperty() where T : Enum<T>, T : StringIdentifiable =
    property { EnumProperty.of(it, T::class.java) }

val Number.pixelAsDouble: Double get() = this.toDouble() / 16.0

fun <U, R, T> ((U) -> R).then(next: (R) -> T): (U) -> T =
    {
        next(this(it))
    }

fun <T> identity(): (T) -> T = { it }

fun <T> Random.choose(f: T, vararg xs: T): T =
    (listOf(f) + xs.toList()).random(this)

fun ChunkPos.randomXZ(random: Random): Pair<Int, Int> =
    random.nextInt(startX, endX + 1) to random.nextInt(startZ, endZ)


fun Scaffolding.PolePosition.toShorthand() = name.first().lowercase()
