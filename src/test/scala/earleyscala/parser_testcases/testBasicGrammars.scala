package earleyscala.parser_testcases

import earleyscala._
import junit.framework.TestCase
import org.junit.Assert

class testBasicGrammars extends TestCase{
  def testSimple: Unit = {
    val grammar = Grammar("S",
      List(
        Rule("S", List(TerminalSymbol("a".r)))
      )
    )

    val input = "a"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    val endState = chart.getLastStates.head
    println(TreeUtils.printCompleteAndLeaves(endState, input))
    /*
      a (0, 1) [1]
    S -> 'a'  (0, 1) [0]
    */
  }

  def testPalindrome: Unit = {
    val grammar = Grammar("S",
      List(
        Rule("S", List(TerminalSymbol("a".r), NonTerminalSymbol("S"), TerminalSymbol("a".r))),
        Rule("S", List(TerminalSymbol("a".r))),
      )
    )
    //This grammar will accept palindromes with an a or b in the middle (so 'aa' is not accepted).

    val input = "aaa"
    val earley = Earley(grammar)
    val end = earley.buildChart(input).getLastStates.head
    println(TreeUtils.printCompleteAndLeaves(end, input))
    /*
              a (0, 1) [3]
          a (1, 2) [3]
        S -> 'a'  (1, 2) [2]
      a (0, 3) [1]
    S -> 'a' S 'a'  (0, 3) [0]
    */
  }

  def testEScottExample2(): Unit = {
    //grammar from here: https://www.sciencedirect.com/science/article/pii/S1571066108001497 - Paper by Elizabeth Scott; the example 2 grammar
    val grammar = Grammar("S",
      List(
        Rule("S", List(NonTerminalSymbol("S"), NonTerminalSymbol("S"))),
        Rule("S", List(TerminalSymbol("b".r)))
      )
    ) // S -> SS|b

    val input = "bbb"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head
    println(TreeUtils.printCompleteAndLeaves(end, input))
    /*
    First derivation
                  b (0, 1) [5]
            S -> 'b'  (0, 1) [4]
            b (1, 2) [4]
          S -> 'b'  (1, 2) [3]
        S -> S S  (0, 2) [2]
        b (2, 3) [2]
      S -> 'b'  (2, 3) [1]
    S -> S S  (0, 3) [0]

    Second derivation (I haven't implemented a disambiguation algorithm)
            b (0, 1) [3]
      S -> 'b'  (0, 1) [2]
          b (1, 2) [4]
        S -> 'b'  (1, 2) [3]
        b (2, 3) [3]
      S -> 'b'  (2, 3) [2]
    S -> S S  (1, 3) [1]
    */
  }
}
