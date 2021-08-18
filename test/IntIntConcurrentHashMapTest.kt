/**
 * This test didn't find any bugs in ConcurrentHashMap.
 */

import org.jetbrains.kotlinx.lincheck.annotations.*
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.paramgen.*
import org.jetbrains.kotlinx.lincheck.strategy.stress.*
import org.jetbrains.kotlinx.lincheck.verifier.*
import org.jetbrains.kotlinx.lincheck.Options
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import java.util.concurrent.ConcurrentHashMap

@Param.Params(
    Param(name = "key", gen = IntGen::class, conf = "1:8"),
    Param(name = "value", gen = IntGen::class, conf = "1:10")
)
class IntIntConcurrentHashMapTest: AbstractLincheckTest() {
    private val hm = ConcurrentHashMap<Int, Int>()

    @Operation
    fun get(@Param(name = "key") key: Int): Int? = hm.get(key)

    @Operation
    fun put(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? = hm.put(key, value)

    @Operation
    fun remove(@Param(name = "key") key: Int): Int? = hm.remove(key)

    override fun StressOptions.customizeStressOptions() {
        invocationsPerIteration(50_000)
    }

    override fun ModelCheckingOptions.customizeModelCheckingOptions() {
        invocationsPerIteration(50_000)
    }

    override fun <O : Options<O, *>> O.customize() {
        actorsPerThread(5)
        sequentialSpecification(IntIntHashMapSequential::class.java)
    }
}

class IntIntHashMapSequential : VerifierState() {
    private val map = HashMap<Int, Int>()

    fun get(key: Int): Int? = map.get(key)
    fun put(key: Int, value: Int): Int? = map.put(key, value)
    fun remove(key: Int): Int? = map.remove(key)

    override fun extractState() = map
}
