/**
 * This test didn't find any bugs in LockBasedFriendlyTreeMapTest.
 */

import LockBasedFriendlyTreeMap.LockBasedFriendlyTreeMap
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
class IntIntLockBasedFriendlyTreeMapTest: AbstractLincheckTest() {
    private val tree: LockBasedFriendlyTreeMap<Int, Int> = LockBasedFriendlyTreeMap()

    @Operation
    fun containsKey(@Param(name = "key") key: Int): Boolean = tree.containsKey(key)

    @Operation
    fun contains(@Param(name = "key") key: Int): Boolean = tree.contains(key)

    @Operation
    fun get(@Param(name = "key") key: Int): Int? = tree.get(key)

    @Operation
    fun remove(@Param(name = "key") key: Int): Int? = tree.remove(key)

    @Operation
    fun putIfAbsent(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? =
        tree.putIfAbsent(key, value)

    override fun ModelCheckingOptions.customizeModelCheckingOptions() {
        actorsPerThread(5)
        verboseTrace()
    }

    override fun extractState() = (MIN_KEY..MAX_KEY).map { key -> tree.get(key) }
}