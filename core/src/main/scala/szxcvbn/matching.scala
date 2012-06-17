package szxcvbn

import scala.math._
import szxcvbn.Predef._
import Entropy._

sealed trait Match {
  /**
   * The character index where the match starts in a password
   */
  def start: Int

  /**
   * The character index where the match ends
   */
  def end: Int

  /**
   * The matched token
   */
  def token: String

  /**
   * Entropy of the match
   */
  def entropy: Double

  /**
   * The type of the match
   */
  def pattern: String

  def <(m: Match) = (start - m.start) match {
    case 0 => end - m.end < 0
    case x => x < 0
  }
}

final case class DictMatch(start: Int, end: Int, token: String, dictName: String, matchedWord: String, rank: Int) extends Match {
  val pattern = "dictionary"
  val entropy = log2(rank) + extraUpperCaseEntropy(token)
}

final case class L33tMatch(start: Int, end: Int, token: String, dictName: String, unl33ted: String, rank: Int, subs: List[(Char,Char)]) extends Match {
  val pattern = "dictionary"

  val baseEntropy = log2(rank) + extraUpperCaseEntropy(token)
  val entropy = baseEntropy + extraL33tEntropy(token, subs)
}

final case class SequenceMatch(start: Int, end: Int, token: String, name: String, space: Int, ascending: Boolean) extends Match {
  val pattern = "sequence"
  val entropy = baseEntropy(token(0)) + log2(token.length) + (if (ascending) 0 else 1)

  private def baseEntropy(startChar: Char) = {
    if (startChar == '1' || startChar == 'a') 1.0
    else if (Character.isDigit(startChar)) Log2of10
    else if (Character.isLetter(startChar)) Log2of26
    else Log2of26 + 1
  }
}

final case class RepeatMatch(start: Int, end: Int, token: String, repeatedChar: Char) extends Match {
  val pattern = "repeat"
  val entropy = log2(bruteForceCardinality(token) * token.length)
}

final case class BruteForceMatch(start: Int, end: Int, token: String, cardinality: Int) extends Match {
  val pattern = "bruteforce"
  val entropy = log2(pow(cardinality, end - start + 1))
}

final case class SpatialMatch(start: Int, end: Int, token: String, name: String, turns: Int, shiftedCount: Int) extends Match {
  val pattern = "spatial"
  val entropy = 0.0
}

trait Matcher[+A <: Match] {
  def matches(password: String): List[A]
}

final case class DictMatcher(name: String, wordList: Seq[String]) extends Matcher[DictMatch] {
  private val dict = wordList.zipWithIndex.map(t => (t._1, t._2 + 1)).toMap

  def matches(password: String) = {
    val lcPass = password.toLowerCase
    var matches: List[DictMatch] = Nil

    for (i <- 0 until lcPass.length)
      for (j <- i until lcPass.length) {
        val word = lcPass.substring(i, j + 1)
        dict.get(word) match {
          case Some(rank) =>
            matches = DictMatch(i, j, password.substring(i, j + 1), name, word, rank) :: matches
          case None =>
        }
      }

    matches
  }
}

object RepeatMatcher extends Matcher[RepeatMatch] {
  def matches(password: String) = findMatches(password, password.length-1, Nil)

  private def findMatches(passwd: String, to: Int, matches: List[RepeatMatch]): List[RepeatMatch] =
    if (to > 0) {
      val from = extendRepeat(passwd(to), passwd, to)

      findMatches(passwd,
                  from - 1,
                  if (okLength(from, to))
                    RepeatMatch(from, to, passwd.substring(from, to+1), passwd(to)) :: matches
                  else
                    matches)
    } else matches


  private def extendRepeat(c: Char, word: String, pos: Int): Int =
    if (pos > 0 && word(pos-1) == c) extendRepeat(c, word, pos-1) else pos
}

final case class SequenceMatcher(namedSequences: List[(String,String)]) extends Matcher[SequenceMatch] {
  def matches(passwd: String) = findMatches(passwd, passwd.length - 1, Nil)

  private def findMatches(passwd: String, to: Int, matches: List[SequenceMatch]): List[SequenceMatch] =
    if (to > 0)
      findCandidateSequence(passwd(to-1), passwd(to), namedSequences) match {
        case Some((name, sequence, direction)) =>
          // We have a valid sequence, now see how far it goes
          val from = extendSequence(to - 1, passwd, sequence, direction)

          findMatches(passwd,
                      from - 1,
                      if (okLength(from, to))
                        SequenceMatch(from, to, passwd.substring(from,to+1), name, sequence.length, direction == 1) :: matches
                      else
                        matches)
        case None =>
          findMatches(passwd, to - 1, matches)
      }
    else matches

  /**
   * Find the maximum extent of the sequence substring in the word, working back from the given position.
   */
  private def extendSequence(pos: Int, passwd: String, seq: String, direction: Int): Int = {
    if (pos > 0)
      sequenceMatch(passwd(pos-1), passwd(pos), seq) match {
        // Is this a continuation of the sequence
        case Some(d) if (d == direction) =>
          // pos-1 is in the sequence, so extend from there
          extendSequence(pos - 1, passwd, seq, direction)
        case _ =>
          pos
      }
    else 0
  }

  /**
   * Given a pair of adjoining characters, find a sequence which contains them in either order.
   */
  private def findCandidateSequence(c1: Char, c2: Char, sequences: List[(String, String)]): Option[(String,String,Int)] =
    sequences match {
      case s :: ss =>
        sequenceMatch(c1, c2, s._2) match {
          case Some(direction) =>
            Some(s._1, s._2, direction)
          case _ =>
            findCandidateSequence(c1, c2, ss)
        }
      case Nil => None
    }

  /**
   * Determines whether the two characters are adjacent in a given sequence, returning an optional direction
   * value if they are. Direction is 1 if c1,c2 are in the sequence order, -1 if they are in reverse order.
   */
  private def sequenceMatch(c1: Char, c2: Char, seq: String): Option[Int] = {
    val (i,j) = (seq.indexOf(c1), seq.indexOf(c2))

    if (i >= 0 && j >= 0 && abs(j - i) == 1) Some(j - i) else None
  }
}

final case class SpatialMatcher(name: String, adjacencyGraph: Map[Char, Seq[String]]) extends Matcher[SpatialMatch] {
  private val MinSeqLength = 3

  def matches(passwd: String) = findMatches(passwd, passwd.length - 1, Nil)

  private def findMatches(passwd: String, to: Int, matches: List[SpatialMatch]): List[SpatialMatch] =
    if (to > 0) {
      val (from, turns, shiftedCount) = walkAdjacents(to, 0, 1, 0, passwd)

      findMatches(passwd,
                  from - 1,
                  if (okLength(from, to))
                    SpatialMatch(from, to, passwd.slice(from, to+1), name, turns, shiftedCount) :: matches
                  else matches)

    }
    else matches

  /**
   * Walk back from the current position and continue while an adjacency match is found for the current character.
   */
  private def walkAdjacents(pos: Int, currentDirection: Int, turns: Int, shiftedCount: Int, password: String): (Int, Int, Int) =
    adjacentMatch(pos, password) match {
      case None =>
        (pos, turns, shiftedCount)
      case Some((direction, shifted)) =>
        walkAdjacents(pos - 1, direction, turns + (if (direction != currentDirection) 1 else 0), shiftedCount + (if (shifted) 1 else 0), password)
    }

  private def adjacentMatch(pos: Int, password: String): Option[(Int,Boolean)] = {
    if (pos > 0)
      adjacencyGraph.get(password(pos)) match {
        case None => None
        case Some(aseq) =>
          findAdjacent(password(pos-1), aseq, 0)
      }
    else None
  }

  private def findAdjacent(c: Char, adjacents: Seq[String], direction: Int): Option[(Int,Boolean)] =
    if (direction == 6)
      None
    else
      adjacents(direction) match {
        case null =>
          findAdjacent(c, adjacents, direction + 1)
        case a =>
          a.indexOf(c) match {
            case -1 => findAdjacent(c, adjacents, direction + 1)
            case i => Some(direction, i == 1)
          }
      }
}


final case class L33tMatcher(matchers: List[DictMatcher]) extends Matcher[L33tMatch]  {

  val L33tTable = Map(
    'a' -> Seq('4', '@'),
    'b' -> Seq('8'),
    'c' -> Seq('(', '{', '[', '<'),
    'e' -> Seq('3'),
    'g' -> Seq('6', '9'),
    'i' -> Seq('1', '!', '|'),
    'l' -> Seq('1', '|', '7'),
    'o' -> Seq('0'),
    's' -> Seq('$', '5'),
    't' -> Seq('+', '7'),
    'x' -> Seq('%'),
    'z' -> Seq('2'))

  val L33tSubs: Map[Char,Seq[Char]] = {
    val k = L33tTable.values.flatten.toList.distinct
    val v = k map { e => L33tTable.keys.toList.filter {L33tTable(_).contains(e)} }
    (k zip v).toMap
  }

  def matches(password: String): List[L33tMatch] = {
    val subbed: StringBuilder = new StringBuilder
    var multiSubs: List[(Char, Seq[Char])] = Nil
    // Actual substitutions used
    var subs = collection.mutable.Set[(Char,Char)]()

    password.foreach(c =>
      L33tSubs.get(c) match {
        case None =>
          subbed.append(c)
        case Some(seq) => seq match {
          // Single char sub. Do the substitution
          case Seq(sub) =>
            subbed.append(sub)
            subs += ((c, sub))
          case multiSub =>
            // Leave the unsubbed char. We need to create multiple strings
            subbed.append(c)
            multiSub.foreach(sub => subs += ((c, sub)))
            multiSubs ::= (c, multiSub)
        }
      }
    )

    if (subs.isEmpty) {
      return Nil
    }

    val words = multiSubs match {
      case Nil => List(subbed)
      case s   => doMultiSubs(List(subbed), s)
    }

    val dictMatches = words.flatMap(w => matchers.flatMap(_.matches(w.toString())))

    for {
      dm <- dictMatches
      token = password.substring(dm.start, dm.end+1)
      // Does the match actually contain any substitutions?
      if (token.toLowerCase != dm.matchedWord)
    } yield {
      L33tMatch(dm.start, dm.end, token, dm.dictName, dm.matchedWord, dm.rank, subs.filter(isUsedSub(token, dm.matchedWord, _)).toList)
    }
  }

  private def isUsedSub(token: String, matchedWord: String, sub: (Char,Char)) = {
    val i = token.indexOf(sub._1)
    i >= 0 && matchedWord(i) == sub._2
  }

  private def doMultiSubs(targets: List[StringBuilder], multiSubs: List[(Char, Seq[Char])]): List[StringBuilder] = {
    multiSubs match {
      case Seq((c,s)) =>
        targets.flatMap(t => doMultiSub(t, c, s))
      case seq =>
        doMultiSubs(targets.flatMap(doMultiSub(_, seq.head._1, seq.head._2)), multiSubs.tail)
    }
  }

  /**
   * Performs each one of multiple substitutions for a single character on a StringBuilder and
   * returns a copy for each substitution.
   */
  private def doMultiSub(target: StringBuilder, c: Char, substitutions: Seq[Char]): Seq[StringBuilder] =
    substitutions.map(s => singleSub(target.clone(), c, s))

  private def singleSub(sb: StringBuilder, c: Char, sub: Char): StringBuilder = {
    for (i <- 1 until sb.length) {
      if (sb(i) == c)
        sb.setCharAt(i, sub)
    }
    sb
  }

}