package earleyscala

import junit.framework.TestCase
import org.junit.Assert

class testBasicNumericGrammar extends TestCase {
  val TreeUtils = new FullTreeUtils

  def testBasicNumericGrammar_LeftRecursive: Unit = {
    /*
    This grammar matches a number. It is a Left Recursive (LR) grammar and the Earley algorithm performs better on LR grammars
    */
    val grammar = Grammar("number",
      List(
        Rule("number", List(NonTerminalSymbol("number"), TerminalSymbol("[0-9]"))),
        Rule("number", List(TerminalSymbol("[0-9]")))
      )
    )
    val input = "123"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    println(chart.repr())
    /*
    ---- 0 ---
    number ->  •  number  '[0-9]' 		(0)
    number ->  •  '[0-9]' 		(0)

    ---- 1 ---
    number ->  '[0-9]'  • 		(0)
    number ->  number  •  '[0-9]' 		(0)

    ---- 2 ---
    number ->  number  '[0-9]'  • 		(0)
    number ->  number  •  '[0-9]' 		(0)

    ---- 3 ---
    number ->  number  '[0-9]'  • 		(0)
    number ->  number  •  '[0-9]' 		(0)
    */

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

    val parseTree = new ParseTree().traversal(end, input)
    val out = LRActions(parseTree, grammar)
    Assert.assertEquals(123, out)
  }

  private def LRActions(node: Node, grammar: Grammar): Int = {
    //This method is used with the LR grammar to convert the parse tree into a number
    if (node.value.rule == grammar.rules(0)) {
      LRActions(node.children(0), grammar) * 10 + node.children(1).repr.toInt
    } else if (node.value.rule == grammar.rules(1)) {
      node.children(0).repr.toInt
    } else {
      throw new RuntimeException("Unexpected rule encountered")
    }
  }

  def testBasicNumericGrammar_RightRecursive: Unit = {
    /*
    This grammar also matches a number but its a Right Recursive (RR) grammar. As demonstrated by the chart output, it takes the Earley algorithm more time to parse a string if the grammar is a RR grammar
     */
    val grammar = Grammar("number",
      List(
        Rule("number", List(TerminalSymbol("[0-9]"), NonTerminalSymbol("number"))),
        Rule("number", List(TerminalSymbol("[0-9]")))
      )
    )

    val input = "123"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    println(chart.repr())
    /*
    ---- 0 ---
    number ->  •  '[0-9]'  number 		(0)
    number ->  •  '[0-9]' 		(0)

    ---- 1 ---
    number ->  '[0-9]'  •  number 		(0)
    number ->  '[0-9]'  • 		(0)
    number ->  •  '[0-9]'  number 		(1)
    number ->  •  '[0-9]' 		(1)

    ---- 2 ---
    number ->  '[0-9]'  •  number 		(1)
    number ->  '[0-9]'  • 		(1)
    number ->  •  '[0-9]'  number 		(2)
    number ->  •  '[0-9]' 		(2)
    number ->  '[0-9]'  number  • 		(0)

    ---- 3 ---
    number ->  '[0-9]'  •  number 		(2)
    number ->  '[0-9]'  • 		(2)
    number ->  •  '[0-9]'  number 		(3)
    number ->  •  '[0-9]' 		(3)
    number ->  '[0-9]'  number  • 		(1)
    number ->  '[0-9]'  number  • 		(0)
    */

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

    val parseTree = new ParseTree().traversal(end, input)
    //So not only does the Earley algorithm perform poorly on RR grammars, walking the parse tree is also way more annoying on RR grammars
    //The most significant digit is at the top of the tree, so we can't use the recursive multiplication trick that was used with the LR grammar.
    //As far as I can tell, the depth needs to be explicitly passed to get the value.
    val out = RRActions(parseTree, grammar, input, 0)
    Assert.assertEquals(123, out)
  }

  private def RRActions(node: Node, grammar: Grammar, input:String, depth: Int): Int = {
    //This method is used with the RR grammar to convert the parse tree into a number
    if (node.value.rule == grammar.rules(0)) {
      node.children(0).repr.toInt * math.pow(10, input.length - depth - 1).toInt + RRActions(node.children(1), grammar, input, depth+1)
    } else if (node.value.rule == grammar.rules(1)) {
      node.children(0).repr.toInt
    } else {
      throw new RuntimeException("Unexpected rule encountered")
    }
  }
}
