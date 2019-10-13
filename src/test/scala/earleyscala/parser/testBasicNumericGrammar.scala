package earleyscala.parser

import earleyscala._
import junit.framework.TestCase

class testBasicNumericGrammar extends TestCase {
  val TreeUtils = new FullTreeUtils

  def testBasicNumericGrammar_LeftRecursive: Unit = {
    val grammar = Grammar("number",
      List(
        Rule("number", List(NonTerminalSymbol("number"), TerminalSymbol("[0-9]"))),
        Rule("number", List(TerminalSymbol("[0-9]")))
      )
    )
    val input = "123"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head

    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)
    /*
          1
        number -> '[0-9]'
        2
      number -> number '[0-9]'
      3
    number -> number '[0-9]'
    */
  }

  def testBasicNumericGrammar_RightRecursive: Unit = {
    val grammar = Grammar("number",
      List(
        Rule("number", List(TerminalSymbol("[0-9]"), NonTerminalSymbol("number"))),
        Rule("number", List(TerminalSymbol("[0-9]")))
      )
    )

    val input = "123"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head

    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)
    /*
      1
        2
          3
        number -> '[0-9]'
      number -> '[0-9]' number
    number -> '[0-9]' number
    */
  }
}
