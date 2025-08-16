@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.ext.kotlin

inline fun Boolean?.isNullOrTrue() = this == null || this
inline fun Boolean?.isNullOrFalse() = this == null || !this


inline fun <R> Boolean.ifTrue(action: () -> R): R? {
	if (this)
		return action()
	else
		return null
}