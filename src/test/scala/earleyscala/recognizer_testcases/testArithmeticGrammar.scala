package earleyscala.recognizer_testcases

import earleyscala._
import junit.framework.TestCase
import org.junit.Assert

class testArithmeticGrammar extends TestCase {
  def testBasicArithmeticGrammar: Unit = {
    //This grammar was taken off the wikipedia - https://en.wikipedia.org/wiki/Earley_parser#Example
    val grammar = Grammar("P",
      List(
        Rule("P", List(NonTerminalSymbol("S"))),
        Rule("S", List(NonTerminalSymbol("S"), TerminalSymbol("\\+".r), NonTerminalSymbol("M"))),
        Rule("S", List(NonTerminalSymbol("M"))),
        Rule("M", List(NonTerminalSymbol("M"), TerminalSymbol("\\*".r), NonTerminalSymbol("T"))),
        Rule("M", List(NonTerminalSymbol("T"))),
        Rule("T", List(TerminalSymbol("[1-4]".r)))
      )
    )

    val input = "2+3*4"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)

    Assert.assertTrue(chart.accepts)

    Assert.assertEquals(6, chart.size)
    Assert.assertEquals(6, chart(0).size)
    Assert.assertEquals(6, chart(1).size)
    Assert.assertEquals(4, chart(2).size)
    Assert.assertEquals(6, chart(3).size)
    Assert.assertEquals(2, chart(4).size)
    Assert.assertEquals(6, chart(5).size)

    val expectedChartRepr =
      """
        |---- 0 ---
        |P ->  •  S 		(0)
        |S ->  •  S  '\+'  M 		(0)
        |S ->  •  M 		(0)
        |M ->  •  M  '\*'  T 		(0)
        |M ->  •  T 		(0)
        |T ->  •  '[1-4]' 		(0)
        |
        |---- 1 ---
        |T ->  '[1-4]'  • 		(0)
        |M ->  T  • 		(0)
        |S ->  M  • 		(0)
        |M ->  M  •  '\*'  T 		(0)
        |P ->  S  • 		(0)
        |S ->  S  •  '\+'  M 		(0)
        |
        |---- 2 ---
        |S ->  S  '\+'  •  M 		(0)
        |M ->  •  M  '\*'  T 		(2)
        |M ->  •  T 		(2)
        |T ->  •  '[1-4]' 		(2)
        |
        |---- 3 ---
        |T ->  '[1-4]'  • 		(2)
        |M ->  T  • 		(2)
        |S ->  S  '\+'  M  • 		(0)
        |M ->  M  •  '\*'  T 		(2)
        |P ->  S  • 		(0)
        |S ->  S  •  '\+'  M 		(0)
        |
        |---- 4 ---
        |M ->  M  '\*'  •  T 		(2)
        |T ->  •  '[1-4]' 		(4)
        |
        |---- 5 ---
        |T ->  '[1-4]'  • 		(4)
        |M ->  M  '\*'  T  • 		(2)
        |S ->  S  '\+'  M  • 		(0)
        |M ->  M  •  '\*'  T 		(2)
        |P ->  S  • 		(0)
        |S ->  S  •  '\+'  M 		(0)
        |""".stripMargin.replace("\r", "")
    Assert.assertEquals(expectedChartRepr, chart.repr)

    val expectedEndStateRepr = "P ->  S  • \t\t(0)"
    Assert.assertEquals(expectedEndStateRepr, chart.getLastStates.head.repr)
  }


  def testArithmeticGrammar_WithBraces: Unit = {
    // This grammar was taken from here: http://loup-vaillant.fr/tutorials/earley-parsing/ which is an excellent resource on how to write an earley parser
    val grammar = Grammar("sum",
      List(
        Rule("sum", List(NonTerminalSymbol("sum"), TerminalSymbol("[+-]".r), NonTerminalSymbol("product"))),
        Rule("sum", List(NonTerminalSymbol("product"))),
        Rule("product", List(NonTerminalSymbol("product"), TerminalSymbol("[/*]".r), NonTerminalSymbol("factor"))),
        Rule("product", List(NonTerminalSymbol("factor"))),
        Rule("factor", List(TerminalSymbol("\\(".r), NonTerminalSymbol("sum"), TerminalSymbol("\\)".r))),
        Rule("factor", List(NonTerminalSymbol("number"))),
        Rule("number", List(TerminalSymbol("[0-9]".r), NonTerminalSymbol("number"))),
        Rule("number", List(TerminalSymbol("[0-9]".r)))
      )
    )

    val input = "1+(2*3-4)"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)

    val expectedChartRepr =
      """
        |---- 0 ---
        |sum ->  •  sum  '[+-]'  product 		(0)
        |sum ->  •  product 		(0)
        |product ->  •  product  '[/*]'  factor 		(0)
        |product ->  •  factor 		(0)
        |factor ->  •  '\('  sum  '\)' 		(0)
        |factor ->  •  number 		(0)
        |number ->  •  '[0-9]'  number 		(0)
        |number ->  •  '[0-9]' 		(0)
        |
        |---- 1 ---
        |number ->  '[0-9]'  •  number 		(0)
        |number ->  '[0-9]'  • 		(0)
        |number ->  •  '[0-9]'  number 		(1)
        |number ->  •  '[0-9]' 		(1)
        |factor ->  number  • 		(0)
        |product ->  factor  • 		(0)
        |sum ->  product  • 		(0)
        |product ->  product  •  '[/*]'  factor 		(0)
        |sum ->  sum  •  '[+-]'  product 		(0)
        |
        |---- 2 ---
        |sum ->  sum  '[+-]'  •  product 		(0)
        |product ->  •  product  '[/*]'  factor 		(2)
        |product ->  •  factor 		(2)
        |factor ->  •  '\('  sum  '\)' 		(2)
        |factor ->  •  number 		(2)
        |number ->  •  '[0-9]'  number 		(2)
        |number ->  •  '[0-9]' 		(2)
        |
        |---- 3 ---
        |factor ->  '\('  •  sum  '\)' 		(2)
        |sum ->  •  sum  '[+-]'  product 		(3)
        |sum ->  •  product 		(3)
        |product ->  •  product  '[/*]'  factor 		(3)
        |product ->  •  factor 		(3)
        |factor ->  •  '\('  sum  '\)' 		(3)
        |factor ->  •  number 		(3)
        |number ->  •  '[0-9]'  number 		(3)
        |number ->  •  '[0-9]' 		(3)
        |
        |---- 4 ---
        |number ->  '[0-9]'  •  number 		(3)
        |number ->  '[0-9]'  • 		(3)
        |number ->  •  '[0-9]'  number 		(4)
        |number ->  •  '[0-9]' 		(4)
        |factor ->  number  • 		(3)
        |product ->  factor  • 		(3)
        |sum ->  product  • 		(3)
        |product ->  product  •  '[/*]'  factor 		(3)
        |factor ->  '\('  sum  •  '\)' 		(2)
        |sum ->  sum  •  '[+-]'  product 		(3)
        |
        |---- 5 ---
        |product ->  product  '[/*]'  •  factor 		(3)
        |factor ->  •  '\('  sum  '\)' 		(5)
        |factor ->  •  number 		(5)
        |number ->  •  '[0-9]'  number 		(5)
        |number ->  •  '[0-9]' 		(5)
        |
        |---- 6 ---
        |number ->  '[0-9]'  •  number 		(5)
        |number ->  '[0-9]'  • 		(5)
        |number ->  •  '[0-9]'  number 		(6)
        |number ->  •  '[0-9]' 		(6)
        |factor ->  number  • 		(5)
        |product ->  product  '[/*]'  factor  • 		(3)
        |sum ->  product  • 		(3)
        |product ->  product  •  '[/*]'  factor 		(3)
        |factor ->  '\('  sum  •  '\)' 		(2)
        |sum ->  sum  •  '[+-]'  product 		(3)
        |
        |---- 7 ---
        |sum ->  sum  '[+-]'  •  product 		(3)
        |product ->  •  product  '[/*]'  factor 		(7)
        |product ->  •  factor 		(7)
        |factor ->  •  '\('  sum  '\)' 		(7)
        |factor ->  •  number 		(7)
        |number ->  •  '[0-9]'  number 		(7)
        |number ->  •  '[0-9]' 		(7)
        |
        |---- 8 ---
        |number ->  '[0-9]'  •  number 		(7)
        |number ->  '[0-9]'  • 		(7)
        |number ->  •  '[0-9]'  number 		(8)
        |number ->  •  '[0-9]' 		(8)
        |factor ->  number  • 		(7)
        |product ->  factor  • 		(7)
        |sum ->  sum  '[+-]'  product  • 		(3)
        |product ->  product  •  '[/*]'  factor 		(7)
        |factor ->  '\('  sum  •  '\)' 		(2)
        |sum ->  sum  •  '[+-]'  product 		(3)
        |
        |---- 9 ---
        |factor ->  '\('  sum  '\)'  • 		(2)
        |product ->  factor  • 		(2)
        |sum ->  sum  '[+-]'  product  • 		(0)
        |product ->  product  •  '[/*]'  factor 		(2)
        |sum ->  sum  •  '[+-]'  product 		(0)
        |""".stripMargin.replace("\r", "")

    Assert.assertEquals(expectedChartRepr, chart.repr)

    Assert.assertEquals("sum ->  sum  '[+-]'  product  • \t\t(0)", chart.getLastStates.head.repr)
  }
}
