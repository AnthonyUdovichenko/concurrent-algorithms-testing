/**
 * This test helped to find bug in ScalableFIFOChannel.
 * See details in bugsfound/ScalableFIFOChannel/index.html
 * In this regard, no changes will be made to this file anymore.
 */

import ScalableFIFOChannel.Node
import ScalableFIFOChannel.ScalableFIFOChannel
import org.jetbrains.kotlinx.lincheck.LoggingLevel
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.annotations.Validate
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.scenario
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.EpsilonVerifier
import org.junit.Test


@Param.Params(
    Param(name = "index", gen = IntGen::class, conf = "1:4")
)
class ScalableFIFOChannelTest {
    private val nodes: Array<Node>

    init {
        var i = 1
        nodes = Array(4) { Node(i++) }
    }

    private val queue = ScalableFIFOChannel()

    init {
        nodes.forEach { queue.addSequential(it) }
    }

    @Operation
    fun remove(@Param(name = "index") ind: Int) = queue.remove(nodes[ind - 1])

    @Operation
    fun dequeue() = queue.dequeue()

    @Validate
    fun checkNoRemovedNodesInTheQueue() = check(!queue.hasRemovedNodes()) {
        "The queue contains logically removed nodes while all the operations are completed: $queue"
    }

    @Test
    fun runStressTest() = StressOptions()
        .iterations(0)
        .addCustomScenario(scenario)
        .verifier(EpsilonVerifier::class.java)
        .logLevel(LoggingLevel.INFO)
        .check(this::class.java)

    @Test
    fun runModelCheckingTest() = ModelCheckingOptions()
        .iterations(0)
        .addCustomScenario(scenario)
        .verifier(EpsilonVerifier::class.java)
        .logLevel(LoggingLevel.INFO)
        .check(this::class.java)
}

private val scenario = scenario {
    parallel {
        thread {
            actor(ScalableFIFOChannelTest::remove, 1)
        }
        thread {
            actor(ScalableFIFOChannelTest::dequeue)
        }
    }
}