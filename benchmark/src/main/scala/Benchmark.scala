import szxcvbn._
import szxcvbn.L33tMatcher
import szxcvbn.SequenceMatcher

/**
 *
 */
class Benchmark extends SimpleScalaBenchmark {

  import Zxcvbn._

  override def setUp() {
  }

  def timeCommonDictionaryPassword(reps: Int) = repeat(reps) {
    Zxcvbn("zxcvbn")
  }

  def timeComplexPassword(reps: Int) = repeat(reps) {
    Zxcvbn("coRrecth0rseba++ery9.23.2007staple")
  }

  def timeComplexPasswordDictionaryMatches(reps: Int) = repeat(reps) {
    Zxcvbn("coRrecth0rseba++ery9.23.2007staple", dictMatchers)
  }

  def timeComplexPasswordL33tMatches(reps: Int) = repeat(reps) {
    Zxcvbn("coRrecth0rseba++ery9.23.2007staple", List(L33tMatcher(dictMatchers)))
  }

  def timeComplexPasswordSequenceMatches(reps: Int) = repeat(reps) {
    Zxcvbn("coRrecth0rseba++ery9.23.2007staple", List(SequenceMatcher(StandardSequences)))
  }

  def timeComplexPasswordRepeatMatches(reps: Int) = repeat(reps) {
    Zxcvbn("coRrecth0rseba++ery9.23.2007staple", List(RepeatMatcher))
  }

  def timeComplexPasswordDateMatches(reps: Int) = repeat(reps) {
    Zxcvbn("coRrecth0rseba++ery9.23.2007staple", List(DateMatcher))
  }

  def timeComplexPasswordDigitMatches(reps: Int) = repeat(reps) {
    Zxcvbn("coRrecth0rseba++ery9.23.2007staple", List(DigitsMatcher))
  }


//  def timeLongSingleCharRepeat(reps: Int) = repeat(reps) {
//    Zxcvbn("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
//  }
//
//  def time5xComplexPassword(reps: Int) = repeat(reps) {
//    Zxcvbn("coRrecth0rseba++ery9.23.2007staplecoRrecth0rseba++ery9.23.2007staplecoRrecth0rseba++ery9.23.2007staplecoRrecth0rseba++ery9.23.2007staplecoRrecth0rseba++ery9.23.2007staple")
//  }
}
