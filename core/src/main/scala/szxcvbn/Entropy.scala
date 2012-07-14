package szxcvbn

import szxcvbn.Predef._

class Rx(p: String) extends scala.util.matching.Regex(p) {
  def matches(s: String) = pattern.matcher(s).matches()
}

/**
 * Stuff for entropy calculations
 */
object Entropy {

  val AllLower = new Rx("^[^A-Z]+$")
  val SimpleCapitalization = new Rx("^([A-Z][^A-Z]+)|([^a-z]+)|([^A-Z]+[A-Z])$")

  def extraUpperCaseEntropy(word: String): Double = {
    if (AllLower.matches(word))
      return 0
    // a capitalized word is the most common capitalization scheme,
    // so it only doubles the search space (uncapitalized + capitalized): 1 extra bit of entropy.
    // allcaps and end-capitalized are common enough too, underestimate as 1 extra bit to be safe.
    if (SimpleCapitalization.matches(word))
      return 1
    // otherwise calculate the number of ways to capitalize U+L uppercase+lowercase letters with U uppercase letters
    // or less. Or, if there's more uppercase than lower (for e.g. PASSwORD), the number of ways to lowercase U+L
    // letters with L lowercase letters
    // TODO: Use a single pass to calculate these and get rid of regex (use nU == 1 to check for simple caps)
    val nU = countUpperCase(word)
    val nL = countLowerCase(word)

    val possibilities: Int = Range.inclusive(0,  math.min(nL,nU)).foldLeft(0)(_ + nCk(nU + nL, _))

    log2(possibilities)
  }

  private def countLowerCase(word: String) = countBack(word, word.length-1, 0, Character.isLowerCase)

  private def countUpperCase(word: String) = countBack(word, word.length-1, 0, Character.isUpperCase)

  private def countBack(word: String, pos: Int, acc: Int, test: (Char => Boolean)): Int =
    if (pos == 0) {
      acc + boolean2Int(test(word(pos)))
    } else {
       countBack(word, pos - 1, acc + boolean2Int(test(word(pos))), test)
    }

  private def boolean2Int(b: Boolean) = if (b) 1 else 0

  def extraL33tEntropy(word: String, subs: List[(Char,Char)]): Double = {
    val possibilities = subs.foldLeft(0)((total,sub) => {
        val (nS,nU) = word.foldLeft((0,0))((acc, c) => c match {
          case sub._1 => (acc._1 + 1, acc._2)
          case sub._2 => (acc._1 + 1, acc._2)
          case _      => acc
        })
        total + Range.inclusive(0,  math.min(nS,nU)).foldLeft(0)(_ + nCk(nU + nS, _))
      })

    assert(possibilities > 0)

    possibilities match {
      case 1 => 1.0
      case p => log2(p)
    }
  }
}
