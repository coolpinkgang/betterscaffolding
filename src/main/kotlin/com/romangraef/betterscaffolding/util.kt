package com.romangraef.betterscaffolding

import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Property
import net.minecraft.util.StringIdentifiable
import kotlin.properties.ReadOnlyProperty
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

val Number.pixelAsDouble: Double get() = this.toDouble()/16.0

fun <U, R, T> ((U) -> R).then(next: (R) -> T): (U) -> T =
    {
        next(this(it))
    }
