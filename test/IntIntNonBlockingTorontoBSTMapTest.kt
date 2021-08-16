import NonBlockingTorontoBSTMap.NonBlockingTorontoBSTMap
import org.jetbrains.kotlinx.lincheck.LoggingLevel
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.Test

private const val MIN_KEY = 1
private const val MAX_KEY = 8

private const val MIN_VALUE = 1
private const val MAX_VALUE = 10

@Param.Params(
    Param(name = "key", gen = IntGen::class, conf = "$MIN_KEY:$MAX_KEY"),
    Param(name = "value", gen = IntGen::class, conf = "$MIN_VALUE:$MAX_VALUE")
)
class IntIntNonBlockingTorontoBSTMapTest: VerifierState() {
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

    @Test
    fun runStressTest() = StressOptions()
        .iterations(500)
        .invocationsPerIteration(10_000)
        .actorsBefore(10)
        .actorsAfter(10)
        .threads(3)
        .actorsPerThread(5)
        .logLevel(LoggingLevel.INFO)
        .check(this::class.java)

    @Test
    fun runModelCheckingTest() = ModelCheckingOptions()
        .iterations(500)
        .invocationsPerIteration(10_000)
        .actorsBefore(10)
        .actorsAfter(10)
        .threads(3)
        .actorsPerThread(5)
        .verboseTrace()
        .logLevel(LoggingLevel.INFO)
        .check(this::class.java)

    override fun extractState() = (MIN_KEY..MAX_KEY).map { key -> tree.get(key) }
}