package btpos.mcmods.devutil.common.util.serialization

import com.mojang.datafixers.util.Pair
import com.mojang.datafixers.util.Unit
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.Lifecycle
import org.apache.commons.lang3.mutable.MutableObject
import java.util.Objects
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Stream

/**
 * An implementation of [com.mojang.serialization.codecs.PairCodec] that actually works when using two "primitive" values.
 *
 * (e.g. Pair<BlockPos, BlockPos> becoming "Do not know how to append primitive value [I; 1,2,3] to [I; 4,5,6]")
 */
class PrimitiveAblePairCodec<A, B>(val first: Codec<A>, val second: Codec<B>) : Codec<Pair<A, B>> {
    override fun <T : Any> encode(value: Pair<A, B>, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        val builder = ops.listBuilder()
        builder.add(first.encodeStart(ops, value.first))
        builder.add(second.encodeStart(ops, value.second))
        return builder.build(prefix)
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<Pair<A, B>, T>> {
        return ops.getList(input).flatMap { it ->
            val ourPair = arrayOfNulls<Any>(2)
            
            /**
             * Any that fail will be put here, AS WELL AS any extras that happen to be in our list
             */
            val failed = Stream.builder<T>()
            val result = MutableObject<DataResult<Unit>>(DataResult.success(Unit.INSTANCE, Lifecycle.stable()))
            
            val haveDecodedFirst = AtomicBoolean(false)
            val haveDecodedSecond = AtomicBoolean(false)
            
            it.accept { t ->
                when {
                    !haveDecodedFirst.getPlain() -> {
                        val gotFirst = first.decode(ops, t)
                        gotFirst.error().ifPresent { failed.add(t) }
                        result.setValue(result.value.apply2stable( { r: Unit, v: Pair<A,T> ->
                            ourPair[0] = v.first
                            r
                        }, gotFirst))
                        haveDecodedFirst.setPlain(true)
                    }
                    !haveDecodedSecond.getPlain() -> {
                        val gotSecond = second.decode(ops, t)
                        gotSecond.error().ifPresent { failed.add(t) }
                        result.setValue(result.value.apply2stable( { r: Unit, v: Pair<B,T> ->
                            ourPair[1] = v.first
                            r
                        }, gotSecond))
                        haveDecodedSecond.setPlain(true)
                    }
                    else -> {
                        failed.add(t)
                    }
                }
            }
            
            val errors: T = ops.createList(failed.build())
            val partialPair: Pair<Pair<A,B>, T> = Pair(Pair<A,B>(ourPair[0] as A?, ourPair[1] as B?), errors)
            
            return@flatMap result.value.map{ it -> partialPair }.setPartial(partialPair)
        }
    }
    
    
    override fun equals(other: Any?): Boolean {
        if (this == other)
            return true
        if (other == null || other !is PrimitiveAblePairCodec<*, *>) {
            return false
        }
        return this.first == other.first && this.second == other.second
    }
    
    override fun hashCode(): Int {
        return Objects.hash(first, second)
    }
    
    override fun toString(): String {
        return "PrimitiveAblePairCodec[$first, $second]"
    }
}