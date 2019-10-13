package earleyscala.parser

import earleyscala._
import junit.framework.TestCase

class testBasicGrammars extends TestCase {
  val TreeUtil = new DisambiguatingTreeUtils

  def testSimple: Unit = {
    val grammar = Grammar("S",
      List(
        Rule("S", List(TerminalSymbol("a")))
      )
    )

    val input = "a"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    val endState = chart.getLastStates.head
    TreeUtil.createLeaves(endState, input)
    println()
    TreeUtil.createTree(endState, input)
    /*
      a
    S -> 'a'
    */
  }

  def testAllTerminals() = {
    val grammar = Grammar("S",
      List(
        Rule("S", List(TerminalSymbol("a"), TerminalSymbol("b"), TerminalSymbol("c"))),
      )
    )

    val input = "abc"
    val earley = Earley(grammar)
    val end = earley.buildChart(input).getLastStates.head
    TreeUtil.createLeaves(end, input)
    println()
    TreeUtil.createTree(end, input)
    /*
      a
      b
      c
    S -> 'a' 'b' 'c'
    */
  }

  def testPalindrome: Unit = {
    val grammar = Grammar("S",
      List(
        Rule("S", List(TerminalSymbol("(a|b)"), NonTerminalSymbol("S"), TerminalSymbol("(a|b)"))),
        Rule("S", List(TerminalSymbol("(a|b)"))),
      )
    )
    //This grammar will accept palindromes with an a or b in the middle (so 'aa' is not accepted).

    val input = "aab"
    val earley = Earley(grammar)
    val end = earley.buildChart(input).getLastStates.head
    TreeUtil.createLeaves(end, input)
    println()
    TreeUtil.createTree(end, input)
    /*
      a
        a
      S -> '(a|b)'
      b
    S -> '(a|b)' S '(a|b)'
    */
  }
}
