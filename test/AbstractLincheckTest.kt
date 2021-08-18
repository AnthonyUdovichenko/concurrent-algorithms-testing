import org.jetbrains.kotlinx.lincheck.LinChecker
import org.jetbrains.kotlinx.lincheck.LoggingLevel
import org.jetbrains.kotlinx.lincheck.Options
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.Test

abstract class AbstractLincheckTest : VerifierState() {
    open fun <O: Options<O, *>> O.customize() {}
    open fun StressOptions.customizeStressOptions() {}
    open fun ModelCheckingOptions.customizeModelCheckingOptions() {}
    override fun extractState(): Any = System.identityHashCode(this)

    @Test
    fun runStressTest(): Unit = StressOptions().run {
        invocationsPerIteration(10_000)
        commonConfiguration()
        customizeStressOptions()
        LinChecker.check(this@AbstractLincheckTest::class.java, this)
    }

    @Test
    fun runModelCheckingTest(): Unit = ModelCheckingOptions().run {
        invocationsPerIteration(10_000)
        commonConfiguration()
        customizeModelCheckingOptions()
        LinChecker.check(this@AbstractLincheckTest::class.java, this)
    }

    private fun <O: Options<O, *>> O.commonConfiguration(): Unit = run {
        iterations(100)
        actorsBefore(10)
        threads(3)
        actorsPerThread(4)
        actorsAfter(10)
        logLevel(LoggingLevel.INFO)
        customize()
    }
}