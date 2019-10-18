package earleyscala.parser

import earleyscala._
import junit.framework.TestCase
import org.junit.Assert

class testArithmeticGrammar extends TestCase {
  val TreeUtils = new FullTreeUtils

  def testGrammar(): Unit = {
    //grammar from - https://en.wikipedia.org/wiki/Ambiguous_grammar#Addition_and_subtraction
    val grammar = Grammar("A",
      List(
        Rule("A", List(NonTerminalSymbol("A"), TerminalSymbol("\\+"), NonTerminalSymbol("A"))),
        Rule("A", List(TerminalSymbol("a")))
      )
    ) //A → A + A | a

    val earley = Earley(grammar)
    val input = "a+a"
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head
    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)
    /*
        a
      A -> 'a'
      +
        a
      A -> 'a'
    A -> A '\+' A
    */
  }

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
        Rule("number", List(NonTerminalSymbol("number"), TerminalSymbol("[0-9]"))),
        Rule("number", List(TerminalSymbol("[0-9]")))
      )
    )

    val input = "1+(2*3-4)"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    println(chart.repr())
    /*
    ---- 0 ---
    sum ->  •  sum  '[+-]'  product 		(0)
    sum ->  •  product 		(0)
    product ->  •  product  '[/*]'  factor 		(0)
    product ->  •  factor 		(0)
    factor ->  •  '\('  sum  '\)' 		(0)
    factor ->  •  number 		(0)
    number ->  •  number  '[0-9]' 		(0)
    number ->  •  '[0-9]' 		(0)

    ---- 1 ---
    number ->  '[0-9]'  • 		(0)
    factor ->  number  • 		(0)
    number ->  number  •  '[0-9]' 		(0)
    product ->  factor  • 		(0)
    sum ->  product  • 		(0)
    product ->  product  •  '[/*]'  factor 		(0)
    sum ->  sum  •  '[+-]'  product 		(0)

    ---- 2 ---
    sum ->  sum  '[+-]'  •  product 		(0)
    product ->  •  product  '[/*]'  factor 		(2)
    product ->  •  factor 		(2)
    factor ->  •  '\('  sum  '\)' 		(2)
    factor ->  •  number 		(2)
    number ->  •  number  '[0-9]' 		(2)
    number ->  •  '[0-9]' 		(2)

    ---- 3 ---
    factor ->  '\('  •  sum  '\)' 		(2)
    sum ->  •  sum  '[+-]'  product 		(3)
    sum ->  •  product 		(3)
    product ->  •  product  '[/*]'  factor 		(3)
    product ->  •  factor 		(3)
    factor ->  •  '\('  sum  '\)' 		(3)
    factor ->  •  number 		(3)
    number ->  •  number  '[0-9]' 		(3)
    number ->  •  '[0-9]' 		(3)

    ---- 4 ---
    number ->  '[0-9]'  • 		(3)
    factor ->  number  • 		(3)
    number ->  number  •  '[0-9]' 		(3)
    product ->  factor  • 		(3)
    sum ->  product  • 		(3)
    product ->  product  •  '[/*]'  factor 		(3)
    factor ->  '\('  sum  •  '\)' 		(2)
    sum ->  sum  •  '[+-]'  product 		(3)

    ---- 5 ---
    product ->  product  '[/*]'  •  factor 		(3)
    factor ->  •  '\('  sum  '\)' 		(5)
    factor ->  •  number 		(5)
    number ->  •  number  '[0-9]' 		(5)
    number ->  •  '[0-9]' 		(5)

    ---- 6 ---
    number ->  '[0-9]'  • 		(5)
    factor ->  number  • 		(5)
    number ->  number  •  '[0-9]' 		(5)
    product ->  product  '[/*]'  factor  • 		(3)
    sum ->  product  • 		(3)
    product ->  product  •  '[/*]'  factor 		(3)
    factor ->  '\('  sum  •  '\)' 		(2)
    sum ->  sum  •  '[+-]'  product 		(3)

    ---- 7 ---
    sum ->  sum  '[+-]'  •  product 		(3)
    product ->  •  product  '[/*]'  factor 		(7)
    product ->  •  factor 		(7)
    factor ->  •  '\('  sum  '\)' 		(7)
    factor ->  •  number 		(7)
    number ->  •  number  '[0-9]' 		(7)
    number ->  •  '[0-9]' 		(7)

    ---- 8 ---
    number ->  '[0-9]'  • 		(7)
    factor ->  number  • 		(7)
    number ->  number  •  '[0-9]' 		(7)
    product ->  factor  • 		(7)
    sum ->  sum  '[+-]'  product  • 		(3)
    product ->  product  •  '[/*]'  factor 		(7)
    factor ->  '\('  sum  •  '\)' 		(2)
    sum ->  sum  •  '[+-]'  product 		(3)

    ---- 9 ---
    factor ->  '\('  sum  '\)'  • 		(2)
    product ->  factor  • 		(2)
    sum ->  sum  '[+-]'  product  • 		(0)
    product ->  product  •  '[/*]'  factor 		(2)
    sum ->  sum  •  '[+-]'  product 		(0)
    */*/*/*/*/*/*/*/*/*/*/*/

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

  def testArithmeticGrammar_WithBraces_AndActions: Unit = {
    // This is using Lou's grammar, but there are actions defined to convert the parse tree into a number
    val grammar = Grammar("sum",
      List(
        Rule("sum", List(NonTerminalSymbol("sum"), TerminalSymbol("[+-]"), NonTerminalSymbol("product"))),
        Rule("sum", List(NonTerminalSymbol("product"))),
        Rule("product", List(NonTerminalSymbol("product"), TerminalSymbol("[/*]"), NonTerminalSymbol("factor"))),
        Rule("product", List(NonTerminalSymbol("factor"))),
        Rule("factor", List(TerminalSymbol("\\("), NonTerminalSymbol("sum"), TerminalSymbol("\\)"))),
        Rule("factor", List(NonTerminalSymbol("number"))),
        Rule("number", List(NonTerminalSymbol("number"), TerminalSymbol("[0-9]"))),
        Rule("number", List(TerminalSymbol("[0-9]")))
      )
    )

    def checkParse(input: String, expected: Int): Unit = {
      val earley = Earley(grammar)
      val chart = earley.buildChart(input)
      val end = chart.getLastStates.head
      val parseTree = new ParseTree().traversal(end, input)
      val out = Actions(parseTree, grammar)
      Assert.assertEquals(expected, out)
    }

    //Simple math cases
    checkParse("10+11", 21)
    checkParse("10-11", -1)
    checkParse("2*5", expected = 10)
    checkParse("10/2", 5)

    //With brackets
    checkParse("10+11-1", 20)
    checkParse("10-(11-7)", 6)
    checkParse("5/6*2", 0) //Integer division causes the first term to be 0
    checkParse("40/2/2", 10)
    checkParse("40/(2/2)", 40)

    //Full expression
    checkParse("10+2*5", 20)
    checkParse("(10+2)*5", 60)
  }

  private def Actions(node: Node, grammar: Grammar): Int = {
    //This method is used to convert the parse tree to a number
    if (node.value.rule == grammar.rules(0)) {
      if (node.children(1).repr == "+") Actions(node.children(0), grammar) + Actions(node.children(2), grammar)
      else Actions(node.children(0), grammar) - Actions(node.children(2), grammar)
    } else if (node.value.rule == grammar.rules(1)) {
      Actions(node.children(0), grammar)
    } else if (node.value.rule == grammar.rules(2)) {
      if (node.children(1).repr == "/") Actions(node.children(0), grammar) / Actions(node.children(2), grammar)
      else Actions(node.children(0), grammar) * Actions(node.children(2), grammar)
    } else if (node.value.rule == grammar.rules(3)) {
      Actions(node.children(0), grammar)
    } else if (node.value.rule == grammar.rules(4)) {
      Actions(node.children(1), grammar)
    } else if (node.value.rule == grammar.rules(5)) {
      Actions(node.children(0), grammar)
    } else if (node.value.rule == grammar.rules(6)) {
      Actions(node.children(0), grammar) * 10 + node.children(1).repr.toInt
    } else if (node.value.rule == grammar.rules(7)) {
      node.children(0).repr.toInt
    } else {
      throw new RuntimeException("Unexpected rule encountered")
    }
  }
}
