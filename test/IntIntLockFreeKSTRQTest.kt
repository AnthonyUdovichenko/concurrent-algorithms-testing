import LockFreeKSTRQ.LockFreeKSTRQ
import org.jetbrains.kotlinx.lincheck.LoggingLevel
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.junit.Test

@Param.Params(
    Param(name = "key", gen = IntGen::class, conf = "1:8"),
    Param(name = "value", gen = IntGen::class, conf = "1:10")
)
abstract class IntIntLockFreeKSTRQTest(k: Int) {
    private val tree: LockFreeKSTRQ<Int, Int> = LockFreeKSTRQ(k)

    @Operation(handleExceptionsAsResult = [NullPointerException::class])
    fun containsKey(@Param(name = "key") key: Int): Boolean = tree.containsKey(key)

    @Operation(handleExceptionsAsResult = [NullPointerException::class])
    fun get(@Param(name = "key") key: Int): Int? = tree.get(key)

    @Operation(handleExceptionsAsResult = [NullPointerException::class])
    fun putIfAbsent(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? =
        tree.putIfAbsent(key, value)

    @Operation(handleExceptionsAsResult = [NullPointerException::class])
    fun put(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? = tree.put(key, value)

    @Operation(handleExceptionsAsResult = [NullPointerException::class])
    fun remove(@Param(name = "key") key: Int): Int? = tree.remove(key)

    @Test
    fun runStressTest() = StressOptions()
        .iterations(100)
        .invocationsPerIteration(10_000)
        .actorsBefore(2)
        .actorsAfter(2)
        .threads(3)
        .actorsPerThread(4)
        .logLevel(LoggingLevel.INFO)
        .check(this::class.java)

    @Test
    fun runModelCheckingTest() = ModelCheckingOptions()
        .iterations(100)
        .invocationsPerIteration(10_000)
        .actorsBefore(2)
        .actorsAfter(2)
        .threads(3)
        .actorsPerThread(4)
        .logLevel(LoggingLevel.INFO)
        .check(this::class.java)
}

class IntIntLockFreeKSTRQTest2: IntIntLockFreeKSTRQTest(2)
class IntIntLockFreeKSTRQTest3: IntIntLockFreeKSTRQTest(3)
class IntIntLockFreeKSTRQTest4: IntIntLockFreeKSTRQTest(4)
class IntIntLockFreeKSTRQTest5: IntIntLockFreeKSTRQTest(5)