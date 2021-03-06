import org.scalatest.FunSuite

import szxcvbn.Data._


class FrequencyListsSuite extends FunSuite {

  test("Lists contain expected data") {
    assert(passwords(0) === "password")
    assert(maleNames(0) === "james")
    assert(femaleNames(0) === "mary")
    assert(surnames(0) === "smith")
    assert(english(0) === "you")
  }

  test("Lists do not overlap") {
    val engSet = english.toSet
    assert(passwords.toSet.intersect(engSet) === Set())
    assert(maleNames.toSet.intersect(engSet) === Set())
    assert(femaleNames.toSet.intersect(engSet) === Set())
    assert(surnames.toSet.intersect(engSet) === Set())
  }
}
