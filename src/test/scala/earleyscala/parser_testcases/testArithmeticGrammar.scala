package earleyscala.parser_testcases

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
    val end = chart.getLastStates.head
    println(TreeUtils.printCompleteAndLeaves(chart.getLastStates.head, input))
    /*
                      2 (0, 1) [7]
                T -> '[1-4]'  (0, 1) [6]
              M -> T  (0, 1) [5]
            S -> M  (0, 1) [4]
          + (0, 2) [3]
                  3 (2, 3) [7]
                T -> '[1-4]'  (2, 3) [6]
              M -> T  (2, 3) [5]
            * (2, 4) [4]
            4 (4, 5) [4]
          T -> '[1-4]'  (4, 5) [3]
        M -> M '\*' T  (2, 5) [2]
      S -> S '\+' M  (0, 5) [1]
    P -> S  (0, 5) [0]
    */
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
    val end = chart.getLastStates.head
    println(TreeUtils.printCompleteAndLeaves(end, input))
    /*
                      1 (0, 1) [7]
                number -> '[0-9]'  (0, 1) [6]
              factor -> number  (0, 1) [5]
            product -> factor  (0, 1) [4]
          sum -> product  (0, 1) [3]
        + (0, 2) [2]
              ( (2, 3) [5]
                                2 (3, 4) [14]
                              number -> '[0-9]'  (3, 4) [13]
                            factor -> number  (3, 4) [12]
                          product -> factor  (3, 4) [11]
                        * (3, 5) [10]
                          3 (5, 6) [11]
                        number -> '[0-9]'  (5, 6) [10]
                      factor -> number  (5, 6) [9]
                    product -> product '[/*]' factor  (3, 6) [8]
                  sum -> product  (3, 6) [7]
                - (3, 7) [6]
                    4 (7, 8) [8]
                  number -> '[0-9]'  (7, 8) [7]
                factor -> number  (7, 8) [6]
              product -> factor  (7, 8) [5]
            sum -> sum '[+-]' product  (3, 8) [4]
          ) (2, 9) [3]
        factor -> '\(' sum '\)'  (2, 9) [2]
      product -> factor  (2, 9) [1]
    sum -> sum '[+-]' product  (0, 9) [0]
    */*/
  }
}
