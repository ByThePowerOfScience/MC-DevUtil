package btpos.mcmods.devutil.common.ext.kotlin

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

/**
 * Gets the delegate object of this delegated property, casts it to T or null, and also sets it accessible first because FOR SOME REASON I'm not allowed to JUST GET THE OBJECT THAT ALREADY EXISTS IN MY OWN GOD FUCKING DAMN CLASS
 * todo: make a compiler plugin that circumvents the entire KProperty bull fucking shit and just inlines a direct access to the "delegate" object inside it
 */
inline fun <reified T> KProperty0<*>.safeGetDelegate(): T? {
    this.isAccessible = true // I hate this i hate this i hate this WHY IS THIS SO STUPID THE DELEGATE IS L I T E R A L L Y  I N  T H E  C L A S S ASDKAMDLKASMDLKMLK
    return this.getDelegate() as? T
}