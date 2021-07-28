package earleyscala.parser

import earleyscala._
import junit.framework.TestCase
import org.junit.Assert

class testBasicGrammars extends TestCase {
  val TreeUtil = new DisambiguatingTreeUtils

  def testSimplistGrammar: Unit = {
    /*
    This is a simple grammar which accepts 'a' and rejects anything else
     */
    val grammar = Grammar("S",
      List(
        Rule("S", List(TerminalSymbol("a")))
      )
    )

    val input = "a"
    val earley = Earley(grammar)

    val chart = earley.buildChart(input)
    println(chart.repr())
    /*
    ---- 0 ---
    S ->  •  'a' 		(0)

    ---- 1 ---
    S ->  'a'  • 		(0)
    */

    val endState = chart.getLastStates.head
    TreeUtil.createLeaves(endState, input)
    //a

    println()
    TreeUtil.createTree(endState, input)
    /*
      a
    S -> 'a'
    */

    val rejectedInput = "b"
    Assert.assertTrue(earley.buildChart(rejectedInput).getLastStates.isEmpty)
  }

  def testAllTerminals() = {
    /*
    This grammar accepts the string 'abc' and doesn't accept anything else.
     */
    val grammar = Grammar("S",
      List(
        Rule("S", List(TerminalSymbol("a"), TerminalSymbol("b"), TerminalSymbol("c"))),
      )
    )

    val input = "abc"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    println(chart.repr())
    /*
    ---- 0 ---
    S ->  •  'a'  'b'  'c' 		(0)

    ---- 1 ---
    S ->  'a'  •  'b'  'c' 		(0)

    ---- 2 ---
    S ->  'a'  'b'  •  'c' 		(0)

    ---- 3 ---
    S ->  'a'  'b'  'c'  • 		(0)
    */

    val end = chart.getLastStates.head
    TreeUtil.createLeaves(end, input)
    //abc

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
    /*
    This grammar accepts a palindrome of an odd number of characters.
    The grammar could be modified to accept a palindrome of an even number of characters if a rule S->() is added
     */
    val grammar = Grammar("S",
      List(
        Rule("S", List(TerminalSymbol("a"), NonTerminalSymbol("S"), TerminalSymbol("a"))),
        Rule("S", List(TerminalSymbol("a"))),
        Rule("S", List(TerminalSymbol("b"), NonTerminalSymbol("S"), TerminalSymbol("b"))),
        Rule("S", List(TerminalSymbol("b"))),
      )
    )

    val input = "baaab"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    println(chart.repr())
    /*
    ---- 0 ---
    S ->  •  'a'  S  'a' 		(0)
    S ->  •  'a' 		(0)
    S ->  •  'b'  S  'b' 		(0)
    S ->  •  'b' 		(0)

    ---- 1 ---
    S ->  'b'  •  S  'b' 		(0)
    S ->  'b'  • 		(0)
    S ->  •  'a'  S  'a' 		(1)
    S ->  •  'a' 		(1)
    S ->  •  'b'  S  'b' 		(1)
    S ->  •  'b' 		(1)

    ---- 2 ---
    S ->  'a'  •  S  'a' 		(1)
    S ->  'a'  • 		(1)
    S ->  •  'a'  S  'a' 		(2)
    S ->  •  'a' 		(2)
    S ->  •  'b'  S  'b' 		(2)
    S ->  •  'b' 		(2)
    S ->  'b'  S  •  'b' 		(0)

    ---- 3 ---
    S ->  'a'  •  S  'a' 		(2)
    S ->  'a'  • 		(2)
    S ->  •  'a'  S  'a' 		(3)
    S ->  •  'a' 		(3)
    S ->  •  'b'  S  'b' 		(3)
    S ->  •  'b' 		(3)
    S ->  'a'  S  •  'a' 		(1)

    ---- 4 ---
    S ->  'a'  •  S  'a' 		(3)
    S ->  'a'  • 		(3)
    S ->  'a'  S  'a'  • 		(1)
    S ->  •  'a'  S  'a' 		(4)
    S ->  •  'a' 		(4)
    S ->  •  'b'  S  'b' 		(4)
    S ->  •  'b' 		(4)
    S ->  'a'  S  •  'a' 		(2)
    S ->  'b'  S  •  'b' 		(0)

    ---- 5 ---
    S ->  'b'  •  S  'b' 		(4)
    S ->  'b'  • 		(4)
    S ->  'b'  S  'b'  • 		(0)
    S ->  •  'a'  S  'a' 		(5)
    S ->  •  'a' 		(5)
    S ->  •  'b'  S  'b' 		(5)
    S ->  •  'b' 		(5)
    S ->  'a'  S  •  'a' 		(3)
    */

    val end = chart.getLastStates.head
    TreeUtil.createLeaves(end, input)
    //baaab

    println()
    TreeUtil.createTree(end, input)
    /*
      b
        a
          a
        S -> 'a'
        a
      S -> 'a' S 'a'
      b
    S -> 'b' S 'b'
    */
  }
}
