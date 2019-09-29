package earleyscala.parser_testcases

import earleyscala._
import junit.framework.TestCase
import org.junit.Assert

class testSimpleAmbiguousGrammar extends TestCase {
  def testGrammar(): Unit = {
    val grammar = Grammar("A",
      List(
        Rule("A", List(NonTerminalSymbol("A"), TerminalSymbol("\\+".r), NonTerminalSymbol("A"))),
        Rule("A", List(NonTerminalSymbol("A"), TerminalSymbol("-".r), NonTerminalSymbol("A"))),
        Rule("A", List(TerminalSymbol("a".r)))
      )
    ) //A → A + A | A − A | a

    val earley = Earley(grammar)
    val input = "a+a+a"
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head
    println(TreeUtils.printCompleteAndLeaves(end, input))
    /*

    First derivation
                a (0, 1) [4]
          A -> 'a'  (0, 1) [3]
        + (0, 2) [2]
              a (2, 3) [5]
            A -> 'a'  (2, 3) [4]
          + (2, 4) [3]
          a (4, 5) [3]
        A -> 'a'  (4, 5) [2]
      A -> A '\+' A  (2, 5) [1]

    Second Derivation
                  a (0, 1) [7]
                A -> 'a'  (0, 1) [6]
              + (0, 2) [5]
              a (2, 3) [5]
            A -> 'a'  (2, 3) [4]
          A -> A '\+' A  (0, 3) [3]
        + (0, 4) [2]
        a (4, 5) [2]
      A -> 'a'  (4, 5) [1]

    (Shared root derivation)
    A -> A '\+' A  (0, 5) [0]
    */
  }
}
