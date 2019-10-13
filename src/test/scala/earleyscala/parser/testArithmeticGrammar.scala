package earleyscala.parser

import earleyscala._
import junit.framework.TestCase

class testArithmeticGrammar extends TestCase {
  val TreeUtils = FullTreeUtils

  def testBasicArithmeticGrammar: Unit = {
    //This grammar was taken off the wikipedia - https://en.wikipedia.org/wiki/Earley_parser#Example
    val grammar = Grammar("P",
      List(
        Rule("P", List(NonTerminalSymbol("S"))),
        Rule("S", List(NonTerminalSymbol("S"), TerminalSymbol("\\+"), NonTerminalSymbol("M"))),
        Rule("S", List(NonTerminalSymbol("M"))),
        Rule("M", List(NonTerminalSymbol("M"), TerminalSymbol("\\*"), NonTerminalSymbol("T"))),
        Rule("M", List(NonTerminalSymbol("T"))),
        Rule("T", List(TerminalSymbol("[1-4]")))
      )
    )

    val input = "2+3*4"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head

    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)

    /*
              2
            T -> '[1-4]'
          M -> T
        S -> M
        +
              3
            T -> '[1-4]'
          M -> T
          *
            4
          T -> '[1-4]'
        M -> M '\*' T
      S -> S '\+' M
    P -> S
    */
  }


  def testArithmeticGrammar_WithBraces: Unit = {
    // This grammar was taken from here: http://loup-vaillant.fr/tutorials/earley-parsing/ which is an excellent resource on how to write an earley parser
    val grammar = Grammar("sum",
      List(
        Rule("sum", List(NonTerminalSymbol("sum"), TerminalSymbol("[+-]"), NonTerminalSymbol("product"))),
        Rule("sum", List(NonTerminalSymbol("product"))),
        Rule("product", List(NonTerminalSymbol("product"), TerminalSymbol("[/*]"), NonTerminalSymbol("factor"))),
        Rule("product", List(NonTerminalSymbol("factor"))),
        Rule("factor", List(TerminalSymbol("\\("), NonTerminalSymbol("sum"), TerminalSymbol("\\)"))),
        Rule("factor", List(NonTerminalSymbol("number"))),
        Rule("number", List(TerminalSymbol("[0-9]"), NonTerminalSymbol("number"))),
        Rule("number", List(TerminalSymbol("[0-9]")))
      )
    )

    val input = "1+(2*3-4)"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head

    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)

    /*
              1
            number -> '[0-9]'
          factor -> number
        product -> factor
      sum -> product
      +
          (
                      2
                    number -> '[0-9]'
                  factor -> number
                product -> factor
                *
                    3
                  number -> '[0-9]'
                factor -> number
              product -> product '[/*]' factor
            sum -> product
            -
                  4
                number -> '[0-9]'
              factor -> number
            product -> factor
          sum -> sum '[+-]' product
          )
        factor -> '\(' sum '\)'
      product -> factor
    sum -> sum '[+-]' product
    */*/
  }
}
