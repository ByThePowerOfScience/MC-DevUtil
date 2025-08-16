@file:OptIn(ExperimentalContracts::class)

package btpos.mcmods.devutil.common.ext.kotlin

import org.jetbrains.annotations.Contract
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Perform action if the value is null. Does not modify the value.
 */
@Contract("_->this", pure=true)
inline fun <T> T?.ifNull(action: () -> Unit): T? {
	contract {
		callsInPlace(action, InvocationKind.AT_MOST_ONCE)
	}
	
	if (this == null)
		action()
	return this
}

inline fun <T> T.runIf(condition: Boolean, action: T.() -> T): T {
	contract {
		callsInPlace(action, InvocationKind.AT_MOST_ONCE)
	}
	
	if (condition)
		return action(this)
	else
		return this
}

/**
 * I forgot that [apply] exists lol
 */
inline fun <T> T.alsoRun(action: T.() -> Unit): T {
	contract {
		callsInPlace(action, InvocationKind.EXACTLY_ONCE)
	}
	
	this.action()
	return this
}