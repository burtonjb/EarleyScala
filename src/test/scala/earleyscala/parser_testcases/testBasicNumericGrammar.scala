package earleyscala.parser_testcases

import earleyscala._
import junit.framework.TestCase
import org.junit.Assert

class testBasicNumericGrammar extends TestCase {
  def testBasicNumericGrammar_LeftRecursive: Unit = {
    val grammar = Grammar("number",
      List(
        Rule("number", List(NonTerminalSymbol("number"), TerminalSymbol("[0-9]".r))),
        Rule("number", List(TerminalSymbol("[0-9]".r)))
      )
    )
    val input = "123"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head
    println(TreeUtils.printCompleteAndLeaves(end, input))
    /*
                1 (0, 1) [5]
            number -> '[0-9]'  (0, 1) [4]
          2 (0, 2) [3]
        number -> number '[0-9]'  (0, 2) [2]
      3 (0, 3) [1]
    number -> number '[0-9]'  (0, 3) [0]
    */
  }

  def testBasicNumericGrammar_RightRecursive: Unit = {
    val grammar = Grammar("number",
      List(
        Rule("number", List(TerminalSymbol("[0-9]".r), NonTerminalSymbol("number"))),
        Rule("number", List(TerminalSymbol("[0-9]".r)))
      )
    )

    val input = "123"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head
    println(TreeUtils.printCompleteAndLeaves(end, input))
    /*
            1 (0, 1) [2]
          2 (1, 2) [3]
          3 (2, 3) [3]
        number -> '[0-9]'  (2, 3) [2]
      number -> '[0-9]' number  (1, 3) [1]
    number -> '[0-9]' number  (0, 3) [0]
    */
  }
}
