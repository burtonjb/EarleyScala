package earleyscala.recognizer_testcases

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
    val ambiguousInput = "a+a+a"

    val chart = earley.buildChart(ambiguousInput)

    val expectedChart =
      """
        |---- 0 ---
        |A ->  •  A  '\+'  A 		(0)
        |A ->  •  A  '-'  A 		(0)
        |A ->  •  'a' 		(0)
        |
        |---- 1 ---
        |A ->  'a'  • 		(0)
        |A ->  A  •  '\+'  A 		(0)
        |A ->  A  •  '-'  A 		(0)
        |
        |---- 2 ---
        |A ->  A  '\+'  •  A 		(0)
        |A ->  •  A  '\+'  A 		(2)
        |A ->  •  A  '-'  A 		(2)
        |A ->  •  'a' 		(2)
        |
        |---- 3 ---
        |A ->  'a'  • 		(2)
        |A ->  A  '\+'  A  • 		(0)
        |A ->  A  •  '\+'  A 		(2)
        |A ->  A  •  '-'  A 		(2)
        |A ->  A  •  '\+'  A 		(0)
        |A ->  A  •  '-'  A 		(0)
        |
        |---- 4 ---
        |A ->  A  '\+'  •  A 		(2)
        |A ->  A  '\+'  •  A 		(0)
        |A ->  •  A  '\+'  A 		(4)
        |A ->  •  A  '-'  A 		(4)
        |A ->  •  'a' 		(4)
        |
        |---- 5 ---
        |A ->  'a'  • 		(4)
        |A ->  A  '\+'  A  • 		(2)
        |A ->  A  '\+'  A  • 		(0)
        |A ->  A  •  '\+'  A 		(4)
        |A ->  A  •  '-'  A 		(4)
        |A ->  A  •  '\+'  A 		(2)
        |A ->  A  •  '-'  A 		(2)
        |A ->  A  •  '\+'  A 		(0)
        |A ->  A  •  '-'  A 		(0)
        |""".stripMargin.replace("\r", "")
    Assert.assertEquals(expectedChart, chart.repr())

    Assert.assertEquals("A ->  A  '\\+'  A  • \t\t(0)", chart.getLastStates.head.repr)
  }
}
