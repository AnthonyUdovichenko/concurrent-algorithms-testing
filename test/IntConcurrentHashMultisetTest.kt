/**
 * This test didn't find any bugs in ConcurrentHashMultiset.
 */

import com.google.common.collect.ConcurrentHashMultiset
import com.google.common.collect.HashMultiset
import org.jetbrains.kotlinx.lincheck.Options
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState

@Param.Params(
    Param(name = "elem", gen = IntGen::class, conf = "1:5"),
    Param(name = "num", gen = IntGen::class, conf = "1:3")
)
class IntConcurrentHashMultisetTest: AbstractLincheckTest() {
    private val hm = ConcurrentHashMultiset.create<Int>()

    @Operation
    fun count(@Param(name = "elem") element: Int): Int = hm.count(element)

    @Operation
    fun add(@Param(name = "elem") element: Int, @Param(name = "num") occurences: Int): Int = hm.add(element, occurences)

    @Operation
    fun remove(@Param(name = "elem") element: Int, @Param(name = "num") occurences: Int): Int =
        hm.remove(element, occurences)

    @Operation
    fun setCount(@Param(name = "elem") element: Int, @Param(name = "num") count: Int): Int = hm.setCount(element, count)

    @Operation
    fun contains(@Param(name = "elem") element: Int): Boolean = hm.contains(element)

    @Operation
    fun add(@Param(name = "elem") element: Int): Boolean = hm.add(element)

    @Operation
    fun remove(@Param(name = "elem") element: Int): Boolean = hm.remove(element)

    override fun <O : Options<O, *>> O.customize() {
        actorsPerThread(5)
        sequentialSpecification(IntHashMultisetSequential::class.java)
    }

    override fun StressOptions.customizeStressOptions() {
        invocationsPerIteration(50_000)
    }

    override fun ModelCheckingOptions.customizeModelCheckingOptions() {
        invocationsPerIteration(50_000)
    }
}

class IntHashMultisetSequential : VerifierState() {
    private val hm: HashMultiset<Int> = HashMultiset.create()

    fun count(element: Int): Int = hm.count(element)
    fun add(element: Int, occurences: Int): Int = hm.add(element, occurences)
    fun remove(element: Int, occurences: Int): Int = hm.remove(element, occurences)
    fun setCount(element: Int, count: Int): Int = hm.setCount(element, count)
    fun contains(element: Int): Boolean = hm.contains(element)
    fun add(element: Int): Boolean = hm.add(element)
    fun remove(element: Int): Boolean = hm.remove(element)

    override fun extractState() = hm
}