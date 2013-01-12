import org.scalameter.api._
import szxcvbn._
import Zxcvbn._

/**
 *
 */
class ZxcvbnBenchmark extends PerformanceTest {

  lazy val executor = LocalExecutor(
    Executor.Warmer.Default(),
    Aggregator.statistic(Aggregator.average),
    new Measurer.IgnoringGC)

  lazy val reporter = new LoggingReporter //Composite(new LoggingReporter, new ChartReporter(new ChartFactory.Histogram()))
  lazy val persistor = Persistor.None

  val gen = Gen.single("password")("coRrecth0rseba++ery9.23.2007staple")

  def usePassword = using(gen).config(exec.benchRuns -> 1000, exec.minWarmupRuns -> 50, exec.maxWarmupRuns -> 100)

  performance of "Zxcvbn" in {

    measure method ("full_match") in {
      usePassword in { p => Zxcvbn(p) }
    }

    measure method ("dictionary_matches") in {
      usePassword in { p => Zxcvbn(p, dictMatchers) }
    }

    measure method ("l33t_matches") in {
      usePassword in { p => Zxcvbn(p, List(L33tMatcher(dictMatchers))) }
    }

    measure method ("sequence_matches") in {
      usePassword in { p => Zxcvbn(p, List(SequenceMatcher(StandardSequences)))}
    }

    measure method ("repeat_matches") in {
      usePassword in { p => Zxcvbn(p, List(RepeatMatcher))}
    }

    measure method ("date_matches") in {
      usePassword in { p => Zxcvbn(p, List(DateMatcher))}
    }

    measure method ("digit_matches") in {
      usePassword in { p => Zxcvbn(p, List(DigitsMatcher))}
    }

  }
}

