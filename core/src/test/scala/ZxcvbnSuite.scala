import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import szxcvbn._

class ZxcvbnSuite extends FunSuite with ShouldMatchers {

  import Zxcvbn._

  test("Entropy to crack time conversion is accurate") {
    entropyToCrackTime(0) should be (0.0 plusOrMinus(0.005))
    entropyToCrackTime(10) should be (0.05 plusOrMinus(0.005))
    entropyToCrackTime(14) should be (0.82 plusOrMinus(0.005))
    entropyToCrackTime(65) should be (1844674407370955.0 plusOrMinus(0.5))
    entropyToCrackTime(70) should be (59029581035870570.0 plusOrMinus(0.5))
  }

  test("Passwords matching [a-zA-Z]* produce expected entropy") {
    var z = Zxcvbn("password")

    assert(z.password === "password")
    assert(z.matches(0).asInstanceOf[DictMatch].rank === 1)
    assert(z.entropy === 0)
    assert(z.crackTimeDisplay === "instant")
    assert(z.score === 0)

    z = Zxcvbn("zxcvbn")
    z.entropy should be (6.845 plusOrMinus 0.0005)

    z = Zxcvbn("correcthorsebatterystaple")
    z.matches.length should be (4)
    z.crackTime should be (2037200406.0 plusOrMinus(0.5))
    var m = z.matches(0).asInstanceOf[DictMatch]
    m.token should be ("correct")
    m.rank should be (1525)
    m.entropy should be (10.5745935 plusOrMinus 0.005)
    m = z.matches(1).asInstanceOf[DictMatch]
    m.rank should be (494)
    m.entropy should be (8.94836723 plusOrMinus 0.005)
    z.matches(2).entropy should be (11.9087678 plusOrMinus 0.005)
    z.matches(3).entropy should be (13.7799245 plusOrMinus 0.005)

    z.entropy should be (45.212 plusOrMinus 0.005)

    z = Zxcvbn("Amateur")
    m = z.matches(0).asInstanceOf[DictMatch]
    m.rank should be (481)
    z.entropy should be (9.91 plusOrMinus(0.005))

    Zxcvbn("AmAteUr").entropy should be (14.91 plusOrMinus(0.005))

    Zxcvbn("").entropy should be (0)

    Zxcvbn("d").entropy should be (4.7 plusOrMinus(0.05))
  }

  test("sequence entropies should be correct") {
    var z = Zxcvbn("bcdef")
    z.entropy should be (7.022 plusOrMinus(0.005))

    z = Zxcvbn("abcdef1234hgfed")
    z.entropy should be (13.607 plusOrMinus(0.005))

  }

  test("repeat entropies should be correct") {
    var z = Zxcvbn("aaaaaa")
    z.entropy should be (6.476 plusOrMinus(0.005))

    z = Zxcvbn("AAAAAAA")
    z.entropy should be (7.507 plusOrMinus(0.005))

    z = Zxcvbn("-------")
    z.entropy should be (7.852 plusOrMinus(0.005))

    z = Zxcvbn("&&&&&&&&&&&&&&&")
    z.entropy should be (8.951 plusOrMinus(0.005))
  }

  test("L33t subsititution entropies should be correct") {
    var z = Zxcvbn("4b4cu$")
//    z.matches(0).asInstanceOf[L33tMatch].baseEntropy should be(12.655 plusOrMinus(0.005))
    z.entropy should be (13.655 plusOrMinus(0.005))

    z = Zxcvbn("48$0|u+10n")
    z.entropy should be (16.47 plusOrMinus(0.005))
  }

  test("More complex examples are handled correctly") {
    pending
    var z = Zxcvbn("coRrecth0rseba++ery9.23.2007staple$")
    z.entropy should be (66.018 plusOrMinus 0.001)
  }


}
