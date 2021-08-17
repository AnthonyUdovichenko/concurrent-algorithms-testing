/**
 * This test found a single-threaded problem in SnapTreeMapFixed, which is not part of our area of interest.
 */

import SnapTree.SnapTreeMapFixed
import org.jetbrains.kotlinx.lincheck.LoggingLevel
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.scenario
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.junit.Test

@Param.Params(
    Param(name = "key", gen = IntGen::class, conf = "1:8"),
    Param(name = "value", gen = IntGen::class, conf = "1:10")
)
class IntIntSnapTreeMapFixedTest {
    private val stm: SnapTreeMapFixed<Int, Int> = SnapTreeMapFixed()

    @Operation
    fun size(): Int = stm.size

    @Operation
    fun isEmpty(): Boolean = stm.isEmpty()

    @Operation
    fun containsValue(@Param(name = "value") value: Int): Boolean = stm.containsValue(value)

    @Operation
    fun containsKey(@Param(name = "key") key: Int): Boolean = stm.containsKey(key)

    @Operation
    fun get(@Param(name = "key") key: Int): Int? = stm.get(key)

    @Operation(handleExceptionsAsResult = [NoSuchElementException::class])
    fun firstKey(): Int = stm.firstKey()

    @Operation(handleExceptionsAsResult = [NoSuchElementException::class])
    fun lastKey(): Int = stm.lastKey()

    @Operation
    fun lowerKey(@Param(name = "key") key: Int): Int? = stm.lowerKey(key)

    @Operation
    fun floorKey(@Param(name = "key") key: Int): Int? = stm.floorKey(key)

    @Operation
    fun ceilingKey(@Param(name = "key") key: Int): Int? = stm.ceilingKey(key)

    @Operation
    fun higherKey(@Param(name = "key") key: Int): Int? = stm.higherKey(key)

    @Operation
    fun put(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? = stm.put(key, value)

    @Operation
    fun putIfAbsent(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? =
        stm.putIfAbsent(key, value)

    @Operation
    fun replace(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? = stm.replace(key, value)

    @Operation
    fun replace(
        @Param(name = "key") key: Int,
        @Param(name = "value") oldValue: Int,
        @Param(name = "value") newValue: Int
    ): Boolean = stm.replace(key, oldValue, newValue)

    @Operation
    fun remove(@Param(name = "key") key: Int): Int? = stm.remove(key)

    @Test
    fun runStressTest() = StressOptions()
        .iterations(100)
        .invocationsPerIteration(10_000)
        .actorsBefore(10)
        .actorsAfter(10)
        .threads(3)
        .actorsPerThread(4)
        .addCustomScenario(scenario)
        .logLevel(LoggingLevel.INFO)
        .check(this::class.java)

    @Test
    fun runModelCheckingTest() = ModelCheckingOptions()
        .iterations(100)
        .invocationsPerIteration(10_000)
        .actorsBefore(10)
        .actorsAfter(10)
        .threads(3)
        .actorsPerThread(4)
        .addCustomScenario(scenario)
        .logLevel(LoggingLevel.INFO)
        .check(this::class.java)
}

private val scenario = scenario {
    initial {
        actor(IntIntSnapTreeMapTest::putIfAbsent, 2, 4)
        actor(IntIntSnapTreeMapTest::putIfAbsent, 4, 2)
    }
    parallel {
        thread {
            actor(IntIntSnapTreeMapTest::putIfAbsent, 6, 4)
            actor(IntIntSnapTreeMapTest::remove, 4)
        }
        thread {
            actor(IntIntSnapTreeMapTest::lastKey)
        }
    }
}