package earleyscala.parser

import earleyscala._
import junit.framework.TestCase

class testEpsilonGrammar extends TestCase {
  val TreeUtils = new DisambiguatingTreeUtils
  def testEpsilonGrammar: Unit = {
    val grammar = Grammar("A",
      List(
        Rule("A", List()),
        Rule("A", List(NonTerminalSymbol("B"))),
        Rule("B", List(NonTerminalSymbol("A")))
      )
    ) //Matches the empty string with infinite derivations

    val input = ""
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)

    println(chart.repr())

    val end = chart.getLastStates.head
    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)
  }

  def testAycockHorspoolExample: Unit = {
    val grammar = Grammar("Start",
      List(
        Rule("Start", List(NonTerminalSymbol("S"))),
        Rule("S", List(NonTerminalSymbol("A"), NonTerminalSymbol("A"), NonTerminalSymbol("A"), NonTerminalSymbol("A"))),
        Rule("A", List(TerminalSymbol("a"))),
        Rule("A", List(NonTerminalSymbol("E"))),
        Rule("E", List())
      )
    )

    val input = "a"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)

    println(chart.completeRepr())

    val end = chart.getLastStates.head
    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)
  }
}
