package earleyscala

import junit.framework.TestCase
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import org.junit.Test

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class testReproduceNullableAndAmbiguousGrammarBug extends TestCase {
  val TreeUtils = new DisambiguatingTreeUtils

  @Test
  // This bug was found by ndw (github.com/ndw). 
  def test_1_ndw_case: Unit = {
    val grammar = Grammar("_0",
      List(
        Rule("_0", List(NonTerminalSymbol("block"))),
        Rule("block", List(TerminalSymbol("\\{"), NonTerminalSymbol("_1"), TerminalSymbol("\\}"))),
        Rule("statement", List(TerminalSymbol("a"))),
        Rule("statement", List()),
        Rule("_1", List(NonTerminalSymbol("_2"))),
        Rule("_2", List(NonTerminalSymbol("statement"), NonTerminalSymbol("_3"))),
        Rule("_2", List()),
        Rule("_3", List(NonTerminalSymbol("_4"))),
        Rule("_4", List(TerminalSymbol(";"), NonTerminalSymbol("statement"), NonTerminalSymbol("_3"))),
        Rule("_4", List())
      )
    )

    val input = "{a;}"
    val earley = Earley(grammar)

    val chart = earley.buildChart(input)
    println(chart.repr())
    val endState = chart.getLastStates.head
    TreeUtils.createLeaves(endState, input)
    println()
    TreeUtils.createTree(endState, input)
  }
}