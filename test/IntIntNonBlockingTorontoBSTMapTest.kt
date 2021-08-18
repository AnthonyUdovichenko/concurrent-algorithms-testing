/**
 * This test didn't find any bugs in NonBlockingTorontoBSTMap.
 */

import NonBlockingTorontoBSTMap.NonBlockingTorontoBSTMap
import org.jetbrains.kotlinx.lincheck.Options
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions

private const val MIN_KEY = 1
private const val MAX_KEY = 8

private const val MIN_VALUE = 1
private const val MAX_VALUE = 10

@Param.Params(
    Param(name = "key", gen = IntGen::class, conf = "$MIN_KEY:$MAX_KEY"),
    Param(name = "value", gen = IntGen::class, conf = "$MIN_VALUE:$MAX_VALUE")
)
class IntIntNonBlockingTorontoBSTMapTest: AbstractLincheckTest() {
    private val tree: NonBlockingTorontoBSTMap<Int, Int> = NonBlockingTorontoBSTMap()

    @Operation
    fun containsKey(@Param(name = "key") key: Int): Boolean = tree.containsKey(key)

    @Operation
    fun get(@Param(name = "key") key: Int): Int? = tree.get(key)

    @Operation
    fun putIfAbsent(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? =
        tree.putIfAbsent(key, value)

    @Operation
    fun put(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? = tree.put(key, value)

    @Operation
    fun remove(@Param(name = "key") key: Int): Int? = tree.remove(key)

    override fun <O : Options<O, *>> O.customize() {
        iterations(500)
        actorsPerThread(5)
    }

    override fun ModelCheckingOptions.customizeModelCheckingOptions() {
        verboseTrace()
    }

    override fun extractState() = (MIN_KEY..MAX_KEY).map { key -> tree.get(key) }
}