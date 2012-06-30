import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import szxcvbn._

class ZxcvbnSuite extends FunSuite with ShouldMatchers {
  val E = 0.005

  import Zxcvbn._

  test("Entropy to crack time conversion is accurate") {
    entropyToCrackTime(0) should be (0.0 plusOrMinus(E))
    entropyToCrackTime(10) should be (0.05 plusOrMinus(E))
    entropyToCrackTime(14) should be (0.82 plusOrMinus(E))
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
    m.entropy should be (10.5745935 plusOrMinus E)
    m = z.matches(1).asInstanceOf[DictMatch]
    m.rank should be (494)
    m.entropy should be (8.94836723 plusOrMinus E)
    z.matches(2).entropy should be (11.9087678 plusOrMinus E)
    z.matches(3).entropy should be (13.7799245 plusOrMinus E)

    z.entropy should be (45.212 plusOrMinus E)

    z = Zxcvbn("Amateur")
    m = z.matches(0).asInstanceOf[DictMatch]
    m.rank should be (481)
    z.entropy should be (9.91 plusOrMinus(E))

    Zxcvbn("AmAteUr").entropy should be (14.91 plusOrMinus(E))

    Zxcvbn("").entropy should be (0)

    Zxcvbn("d").entropy should be (4.7 plusOrMinus(0.05))
  }

  test("sequence entropies should be correct") {
    var z = Zxcvbn("bcdef")
    z.entropy should be (7.022 plusOrMinus(E))

    z = Zxcvbn("abcdef1234hgfed")
    z.entropy should be (13.607 plusOrMinus(E))

  }

  test("repeat entropies should be correct") {
    var z = Zxcvbn("aaaaaa")
    z.entropy should be (6.476 plusOrMinus(E))

    z = Zxcvbn("&&&&&&&&&&&&&&&")
    z.entropy should be (8.951 plusOrMinus(E))
  }

  test("L33t subsititution entropies should be correct") {
    var z = Zxcvbn("4b4cu$")
//    z.matches(0).asInstanceOf[L33tMatch].baseEntropy should be(12.655 plusOrMinus(E))
    z.entropy should be (13.655 plusOrMinus(E))

    z = Zxcvbn("48$0|u+10n")
    z.entropy should be (16.47 plusOrMinus(E))
  }

  test("Spatial entropies should be correct") {
    var z = Zxcvbn("jhgfds")
    z.matches.length should be (1)
    z.matches(0).asInstanceOf[SpatialMatch].turns should be (1)
    z.entropy should be (11.077 plusOrMinus(E))

    z = Zxcvbn("qwEdfR43@!")
    z.matches(0).asInstanceOf[SpatialMatch].turns should be (5)
    z.matches(0).asInstanceOf[SpatialMatch].shiftedCount should be (4)
    z.entropy should be (34.39 plusOrMinus(E))
  }

  test("'coRrecth0rseba++ery9.23.2007staple$' has entropy 66.018") {
    pending
    var z = Zxcvbn("coRrecth0rseba++ery9.23.2007staple$")
    z.entropy should be (66.018 plusOrMinus E)
  }

  import org.scalatest.prop.TableDrivenPropertyChecks._

  // Various test data made up or taken from zxcvbn
  val entropies =
    Table(
      ("password", "entropy"),
      ("bhjkdcxs", 28.69),
      ("AAAAAAA", 7.507),
      ("G0r!ll4F4c3", 23.766),
      ("[_{P;toads", 35.978),
      ("uiyijijfdjkl;", 48.636),
      // Chinese entropy is underestimated by brute force match
      ("对马电池钉书针", 35.311),
      ("Rosebud", 8.937),
      ("ROSEBUD", 8.937),
      ("rosebuD", 8.937),
      ("ros3bud", 8.937),
      ("(*.>ddWR%gHssT^6$h", 118.257),
      ("(*.>ddWR%gHssT^6$hvGhLz0_jw0w76£2", 201.387),
      ("(*.>ddWR%gHssT^6$hvGhLz0_jw0w76£2)(***feraQQ m\"^&ape", 294.861),
      ("(*.>ddWR%gHssT^6$hvGhLz0_jw0w76£2)(***feraQQ m\"^&apeF3t5%zuui+=Gn@7733ßü", 392.872)
    )

  test("Entropy should be correct for a selection of passwords") {
    forAll (entropies) { (password: String, entropy: Double) =>
      Zxcvbn(password).entropy should be (entropy plusOrMinus(0.05))
    }
  }
}
