package btpos.mcmods.devutil.common.ext.vanilla.destructuring

import com.mojang.datafixers.util.Pair

operator fun <T, U> Pair<T, U>.component1(): T = this.first
operator fun <T, U> Pair<T, U>.component2(): U = this.second