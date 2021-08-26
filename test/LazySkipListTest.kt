import LazySkipList.LazySkipList
import org.jetbrains.kotlinx.lincheck.Options
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen

@Param(name = "value", gen = IntGen::class, conf = "1:5")
class LazySkipListTest: AbstractLincheckTest() {
    private val skipList = LazySkipList()

    @Operation
    fun containsInt(@Param(name = "value") value: Int): Boolean = skipList.containsInt(value)

    @Operation
    fun addInt(@Param(name = "value") value: Int): Boolean = skipList.addInt(value)

    @Operation
    fun removeInt(@Param(name = "value") value: Int): Boolean = skipList.removeInt(value)

    override fun <O : Options<O, *>> O.customize() {
        actorsBefore(0)
        actorsAfter(0)
    }
}