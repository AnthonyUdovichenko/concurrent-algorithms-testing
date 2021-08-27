import NonBlockingFriendlySkipListMap.NonBlockingFriendlySkipListMap
import org.jetbrains.kotlinx.lincheck.Options
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
class IntIntNonBlockingFriendlySkipListMapTest: AbstractLincheckTest() {
    private val skipList = NonBlockingFriendlySkipListMap<Int, Int>()

    @Operation
    fun get(@Param(name = "key") kkey: Int): Int? = skipList.get(kkey)

    @Operation
    fun containsKey(@Param(name = "key") key: Int): Boolean = skipList.containsKey(key)

    @Operation
    fun putIfAbsent(@Param(name = "key") kkey: Int, @Param(name = "value") value: Int): Int? =
        skipList.putIfAbsent(kkey, value)

    @Operation
    fun put(@Param(name = "key") kkey: Int, @Param(name = "value") value: Int): Int? = skipList.put(kkey, value)

    @Operation
    fun remove(@Param(name = "key") kkey: Int): Int? = skipList.remove(kkey)

    @Operation
    fun size(): Int = skipList.size

    override fun <O : Options<O, *>> O.customize() {
        actorsBefore(0)
        actorsPerThread(3)
        actorsAfter(0)
    }

    override fun ModelCheckingOptions.customizeModelCheckingOptions() {
        invocationsPerIteration(1_000)
    }

    override fun StressOptions.customizeStressOptions() {
        invocationsPerIteration(1_000)
    }

    override fun extractState() = (MIN_KEY..MAX_KEY).map { key -> skipList.get(key) }
}