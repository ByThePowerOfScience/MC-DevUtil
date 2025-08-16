package btpos.mcmods.devutil.common.structure.composition

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Standardized interface for objects that invoke a callback whenever they change.
 *
 * Implementers should make sure that [onChange]'s setter also sets onChange for any members that also implement this class.
 */
interface IOnChange {
    /**
     * Callback to be invoked whenever this object's state is changed. (Usually to updated the NBT serialization).
     *
     * Implementers need to make sure the setter also sets onChange for any members that also implement this class.
     */
    var onChange: () -> Unit
    
    
    /**
     * Property delegate that calls [::onChange] when the value has been set.
     */
    fun <T> notify(initialValue: T): ReadWriteProperty<Any?, T> {
        return object : ReadWriteProperty<Any?, T> {
            var value: T = initialValue
            
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                if (this.value == value)
                    return;
                
                this.value = value
                onChange()
            }
        }
    }
}