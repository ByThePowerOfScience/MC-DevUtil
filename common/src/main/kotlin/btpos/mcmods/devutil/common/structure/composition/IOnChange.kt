package btpos.mcmods.devutil.common.structure.composition

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Standardized interface for objects that invoke a callback whenever they change.
 */
interface IOnChange {
    /**
     * Callback to be invoked whenever this object's state is changed. (Usually to update the NBT serialization)
     *
     * Implementers need to make sure the setter for this property _also_ sets the [onChange] callback for any of the class's members that need it.
     */
    var onChange: Runnable
    
    /**
     * Property delegate that calls [onChange] when the value has been set.
     */
    fun <T> notify(initialValue: T): ReadWriteProperty<Any?, T> {
        return object : ReadWriteProperty<Any?, T> {
            private var value: T = initialValue
            
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                if (this.value == value)
                    return;
                
                this.value = value
                onChange.run()
            }
        }
    }
}