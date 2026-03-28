package btpos.mcmods.devutil.testing.mockito

import org.mockito.Incubating
import org.mockito.Mockito
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.UseConstructor
import org.mockito.kotlin.mock
import org.mockito.listeners.InvocationListener
import org.mockito.mock.SerializableMode
import org.mockito.stubbing.Answer
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass


/**
 * Creates a delegate holding a spy of a new instance of [T], optionally allowing for immediate stubbing.
 *
 * The name of the spy is set to the name of the parameter delegating to this.
 *
 * The spy calls **real** methods unless they are stubbed.
 */
inline fun <reified T : Any> spy(name: String? = null, ctorArgs: UseConstructor = UseConstructor.parameterless(), stubbing: KStubbing<T>.(T) -> Unit = {}): T {
    return mock<T>(
        useConstructor = ctorArgs,
        defaultAnswer = Mockito.CALLS_REAL_METHODS,
        stubbing = stubbing,
        name = name
    )
}

/**
 * Creates a delegate holding a spy of the given instance of [T], optionally allowing for immediate stubbing.
 *
 * The name of the spy is set to the name of the parameter delegating to this.
 *
 * The spy calls **real** methods unless they are stubbed.
 */
inline fun <reified T : Any> spy(instance: T, name: String? = null, stubbing: KStubbing<T>.(T) -> Unit = {}): T {
    return mock<T>(
        spiedInstance = instance,
        defaultAnswer = Mockito.CALLS_REAL_METHODS,
        stubbing = stubbing,
        name = name
    )
}