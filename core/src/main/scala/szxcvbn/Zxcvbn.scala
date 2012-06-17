package szxcvbn

import szxcvbn.Predef._

trait Zxcvbn {
  def password: String
  def entropy: Double
  def score: Int
  def matches: List[Match]
  def crackTime: Double
  def crackTimeDisplay: String
}

object Zxcvbn {
  type MatcherList = List[Matcher[Match]]

  def apply(password: String): Zxcvbn = {
    val (entropy, matches) = minEntropyMatchSequence(password, allMatches(password))
    new ZxcvbnImpl(password, entropy, matches)
  }

  def allMatches(password: String, userData: Option[Seq[String]] = None) =
    (userData match {
      case Some(ud) => doMatch(password, DictMatcher("user_data", ud) :: defaultMatchers)
      case None => doMatch(password, defaultMatchers)
    }).sortWith((m1,m2) => m1 < m2)

  private def doMatch(word: String, matchers: List[Matcher[Match]]): List[Match] = matchers match {
    case List(m) => m.matches(word)
    case m :: ms => m.matches(word) ::: doMatch(word, ms)
    case Nil     => Nil
  }

  def minEntropyMatchSequence(password: String, matches: Seq[Match]) = {
    val bfc = bruteForceCardinality(password)
    val lgBfc = log2(bfc)

    val minEntropyUpTo = new Array[Double](password.length)
    val backpointers = new Array[Option[Match]](password.length)

    for (k <- 0 until password.length) {
      // starting scenario to try and beat: adding a brute-force character to the minimum entropy sequence at k-1.
      minEntropyUpTo(k) = (if (k == 0) 0 else minEntropyUpTo(k-1)) + lgBfc
      backpointers(k) = None

      matches.foreach((m) => {
        if (m.end == k) {
          // see if best entropy up to start of match + entropy of the match is less than the current minimum at k.
          val candidateEntropy = (m.start match {
            case 0 => 0
            case s => minEntropyUpTo(s - 1)
          }) + m.entropy

          if (candidateEntropy < minEntropyUpTo(k)) {
            minEntropyUpTo(k) = candidateEntropy
            backpointers(k) = Some(m)
          }
        }
      })
    }

    var matchSequence: List[Match] = Nil
    var k = password.length - 1

    while (k >= 0) {
      backpointers(k) match {
        case Some(m) =>
          matchSequence ::= m
          k = m.start - 1
        case None => k -= 1
      }
    }

    val makeBruteForceMatch: (Int,Int) => Match = (i,j) => BruteForceMatch(i, j, password.substring(i, j+1), bfc)
    var matchSequenceCopy: List[Match] = Nil

    k = 0
    matchSequence.foreach((m) => {
      if (m.start - k > 0)
        matchSequenceCopy ::= makeBruteForceMatch(k, m.start - 1)

      matchSequenceCopy ::= m
      k = m.end + 1
    })

    if (k < password.length - 1)
      matchSequenceCopy ::= makeBruteForceMatch(k, password.length - 1)

    val minEntropy = if (password.isEmpty) 0 else minEntropyUpTo(password.length - 1)

    (minEntropy, matchSequenceCopy.reverse)
  }


  val StandardSequences = List(
    Tuple2("lower", "abcdefghijklmnopqrstuvwxyz"),
    Tuple2("upper", "ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    Tuple2("digits", "01234567890")
  )

  val SingleGuessTimeMs = 10
  val NumAttackers = 100

  import FrequencyLists._

  val dictMatchers = List(
    DictMatcher("passwords", passwords),
    DictMatcher("english", english),
    DictMatcher("male_names", maleNames),
    DictMatcher("female_names", femaleNames),
    DictMatcher("surnames", surnames)
  )

  val defaultMatchers: MatcherList = dictMatchers ::: List(
    L33tMatcher(dictMatchers),
    RepeatMatcher,
    SequenceMatcher(StandardSequences)
  )

  def entropyToCrackTime(entropy: Double): Double = (math.pow(2, entropy) / (2 * NumAttackers * 1000)) * SingleGuessTimeMs
}


private class ZxcvbnImpl(val password: String, val entropy: Double, val matches: List[Match]) extends Zxcvbn {

  import Zxcvbn._

  // .5 * Math.pow(2, entropy) * SECONDS_PER_GUESS
  val crackTime: Double = entropyToCrackTime(entropy)

  val score =
    if (crackTime < 100)
      0
    else if (crackTime < 10000)
      1
    else if (crackTime < 1000000)
      2
    else if (crackTime < 100000000)
      3
    else
      4

  val crackTimeDisplay = displayTime(crackTime)

}