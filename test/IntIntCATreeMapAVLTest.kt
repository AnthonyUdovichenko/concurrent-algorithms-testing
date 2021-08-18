/**
 * This test doesn't work yet.
 *
 * Stress test doesn't finish in a reasonable time.
 * Model checking test doesn't work due to non-determinism in CATreeMapAVL.
 */

import CATreeMapAVL.CATreeMapAVL
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions

private const val MIN_KEY = 1
private const val MAX_KEY = 8

private const val MIN_VALUE = 1
private const val MAX_VALUE = 10

@Param.Params(
    Param(name = "key", gen = IntGen::class, conf = "$MIN_KEY:$MAX_KEY"),
    Param(name = "value", gen = IntGen::class, conf = "$MIN_VALUE:$MAX_VALUE")
)
class IntIntCATreeMapAVLTest: AbstractLincheckTest() {
    private val tree = CATreeMapAVL<Int, Int>()

    @Operation
    fun size(): Int = tree.size

    @Operation
    fun isEmpty(): Boolean = tree.isEmpty()

    @Operation
    fun containsKey(@Param(name = "key") key: Int): Boolean = tree.containsKey(key)

    @Operation
    fun get(@Param(name = "key") key: Int): Int? = tree.get(key)

    @Operation
    fun put(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? = tree.put(key, value)

    @Operation
    fun putIfAbsent(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? =
        tree.putIfAbsent(key, value)

    @Operation
    fun remove(@Param(name = "key") key: Int): Int? = tree.remove(key)

    @Operation
    fun clear() = tree.clear()

    override fun StressOptions.customizeStressOptions() {
        actorsPerThread(3)
    }

    override fun ModelCheckingOptions.customizeModelCheckingOptions() {
        invocationsPerIteration(50_000)
        actorsPerThread(5)
    }

    override fun extractState() = (MIN_KEY..MAX_KEY).map { key -> tree.get(key) }
}