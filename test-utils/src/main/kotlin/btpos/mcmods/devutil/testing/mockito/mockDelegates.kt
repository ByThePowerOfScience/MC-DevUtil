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
 * Creates a mock for [T] with the name of the property this is assigned to.
 *
 * Naming mocks can be helpful for debugging - the name is used in all verification errors.
 *
 * @param extraInterfaces Specifies extra interfaces the mock should implement.
 * @param spiedInstance Specifies the instance to spy on. Makes sense only for spies/partial mocks.
 * @param defaultAnswer Specifies default answers to interactions.
 * @param serializable Configures the mock to be serializable.
 * @param serializableMode Configures the mock to be serializable with a specific serializable mode.
 * @param verboseLogging Enables real-time logging of method invocations on this mock.
 * @param invocationListeners Registers a listener for method invocations on this mock. The listener is notified every time a method on this mock is called.
 * @param stubOnly A stub-only mock does not record method invocations, thus saving memory but disallowing verification of invocations.
 * @param useConstructor Mockito attempts to use constructor when creating instance of the mock.
 * @param outerInstance Makes it possible to mock non-static inner classes in conjunction with [useConstructor].
 * @param lenient Lenient mocks bypass "strict stubbing" validation.
 */
inline fun <reified T : Any> mocking(
    extraInterfaces: Array<out KClass<out Any>>? = null,
    spiedInstance: Any? = null,
    defaultAnswer: Answer<Any>? = null,
    serializable: Boolean = false,
    serializableMode: SerializableMode? = null,
    verboseLogging: Boolean = false,
    invocationListeners: Array<InvocationListener>? = null,
    stubOnly: Boolean = false,
    @Incubating useConstructor: UseConstructor? = null,
    @Incubating outerInstance: Any? = null,
    @Incubating lenient: Boolean = false,
    crossinline stubbing: KStubbing<T>.(T) -> Unit = {}
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, T>> {
    return PropertyDelegateProvider { _, property ->
        val mock = mock<T>(
            extraInterfaces = extraInterfaces,
            name = property.name,
            spiedInstance = spiedInstance,
            defaultAnswer = defaultAnswer,
            serializable = serializable,
            serializableMode = serializableMode,
            verboseLogging = verboseLogging,
            invocationListeners = invocationListeners,
            stubOnly = stubOnly,
            useConstructor = useConstructor,
            outerInstance = outerInstance,
            lenient = lenient,
            stubbing = stubbing
        )
        ReadOnlyProperty { _, _ ->
            mock
        }
    }
}

/**
 * Creates a delegate holding a spy of a new instance of [T], optionally allowing for immediate stubbing.
 *
 * The name of the spy is set to the name of the parameter delegating to this.
 *
 * The spy calls **real** methods unless they are stubbed.
 */
inline fun <reified T : Any> spying(ctorArgs: UseConstructor = UseConstructor.parameterless(), crossinline stubbing: KStubbing<T>.(T) -> Unit = {}): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, T>> {
    return mocking<T>(
        useConstructor = ctorArgs,
        defaultAnswer = Mockito.CALLS_REAL_METHODS,
        stubbing = stubbing
    )
}

/**
 * Creates a delegate holding a spy of the given instance of [T], optionally allowing for immediate stubbing.
 *
 * The name of the spy is set to the name of the parameter delegating to this.
 *
 * The spy calls **real** methods unless they are stubbed.
 */
inline fun <reified T : Any> spying(instance: T, crossinline stubbing: KStubbing<T>.(T) -> Unit = {}): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, T>> {
    return mocking<T>(
        spiedInstance = instance,
        defaultAnswer = Mockito.CALLS_REAL_METHODS,
        stubbing = stubbing
    )
}