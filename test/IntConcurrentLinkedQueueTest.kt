/**
 * This test didn't find any bugs in ConcurrentLinkedQueue.
 */

import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import java.util.concurrent.ConcurrentLinkedQueue

@Param.Params(
    Param(name = "elem", gen = IntGen::class, conf = "1:5")
)
class IntConcurrentLinkedQueueTest: AbstractLincheckTest() {
    private val queue: ConcurrentLinkedQueue<Int> = ConcurrentLinkedQueue()

    @Operation
    fun add(@Param(name = "elem") e: Int): Boolean = queue.add(e)

    @Operation
    fun contains(@Param(name = "elem") o: Int): Boolean = queue.contains(o)

    @Operation
    fun isEmpty(): Boolean = queue.isEmpty()

    @Operation
    fun offer(@Param(name = "elem") e: Int): Boolean = queue.offer(e)

    @Operation(handleExceptionsAsResult = [NullPointerException::class])
    fun peek(): Int = queue.peek()

    @Operation(handleExceptionsAsResult = [NullPointerException::class])
    fun poll(): Int = queue.poll()

    @Operation
    fun remove(@Param(name = "elem") o: Int): Boolean = queue.remove(o)

    override fun ModelCheckingOptions.customizeModelCheckingOptions() {
        verboseTrace()
    }
}