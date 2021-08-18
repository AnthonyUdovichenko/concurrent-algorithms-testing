/**
 * This test helped to find bug in LogicalOrderingAVL.
 * See details in bugsfound/README.md
 */

import LogicalOrderingAVL.LogicalOrderingAVL
import org.jetbrains.kotlinx.lincheck.Options
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.paramgen.BooleanGen
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.scenario

private const val MIN_KEY = 1
private const val MAX_KEY = 8

private const val MIN_VALUE = 1
private const val MAX_VALUE = 10

@Param.Params(
    Param(name = "key", gen = IntGen::class, conf = "$MIN_KEY:$MAX_KEY"),
    Param(name = "value", gen = IntGen::class, conf = "$MIN_VALUE:$MAX_VALUE"),
    Param(name = "bool", gen = BooleanGen::class)
)
class IntIntLogicalOrderingAVLTest: AbstractLincheckTest() {
    private val tree = LogicalOrderingAVL<Int, Int>()

    @Operation
    fun get(@Param(name = "key") key: Int): Int? = tree.get(key)

    @Operation
    fun containsKey(@Param(name = "key") key: Int): Boolean = tree.containsKey(key)

    @Operation
    fun put(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? = tree.put(key, value)

    @Operation
    fun putIfAbsent(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? =
        tree.putIfAbsent(key, value)

    @Operation
    fun replace(@Param(name = "key") key: Int, @Param(name = "value") value: Int): Int? = tree.replace(key, value)

    @Operation(handleExceptionsAsResult = [NullPointerException::class])
    fun replace(
        @Param(name = "key") key: Int,
        @Param(name = "value") oldValue: Int,
        @Param(name = "value") newValue: Int
    ): Boolean = tree.replace(key, oldValue, newValue)

    @Operation
    fun remove(@Param(name = "key") key: Int): Int? = tree.remove(key)

    /*@Operation
    fun remove(@Param(name = "key") key: Int, @Param(name = "value") item: Int): Boolean = tree.remove(key, item)*/

    /*@Operation(handleExceptionsAsResult = [NullPointerException::class])
    fun remove(
        @Param(name = "key") key: Int,
        @Param(name = "bool") compareItem: Boolean,
        @Param(name = "value") item: Int
    ): Int = tree.remove(key, compareItem, item)*/

    @Operation
    fun clear() = tree.clear()

    @Operation
    fun height(): Int = tree.height()

    @Operation
    fun size(): Int = tree.size

    @Operation
    fun isEmpty(): Boolean = tree.isEmpty()

    override fun <O : Options<O, *>> O.customize() {
        addCustomScenario(scenario)
    }

    override fun extractState() = (MIN_KEY..MAX_KEY).map { key -> tree.get(key) }
}

private val scenario = scenario {
    initial {
        actor(IntIntLogicalOrderingAVLTest::put, 5, 3)
    }
    parallel {
        thread {
            actor(IntIntLogicalOrderingAVLTest::putIfAbsent, 3, 8)
        }
        thread {
            actor(IntIntLogicalOrderingAVLTest::put, 1, 7)
        }
        thread {
            actor(IntIntLogicalOrderingAVLTest::remove, 3)
        }
    }
}