package earleyscala.recognizer_testcases

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
    println(grammar.repr) /*
    start: number
    number -> number '[0-9]'
    number -> '[0-9]'
    */

    val earley = Earley(grammar)
    val acceptedInput = earley.buildChart("123")
    val rejectedInput = earley.buildChart("12a3")

    Assert.assertTrue(acceptedInput.accepts)
    Assert.assertFalse(rejectedInput.accepts)

    Assert.assertEquals(acceptedInput.size, 4)
    Assert.assertEquals(acceptedInput(0).size, 2)
    Assert.assertEquals(acceptedInput(1).size, 2)
    Assert.assertEquals(acceptedInput(2).size, 2)
    Assert.assertEquals(acceptedInput(3).size, 2)


    val expectedChart = """
    |---- 0 ---
    |number ->  •  number  '[0-9]' 		(0)
    |number ->  •  '[0-9]' 		(0)
    |
    |---- 1 ---
    |number ->  '[0-9]'  • 		(0)
    |number ->  number  •  '[0-9]' 		(0)
    |
    |---- 2 ---
    |number ->  number  '[0-9]'  • 		(0)
    |number ->  number  •  '[0-9]' 		(0)
    |
    |---- 3 ---
    |number ->  number  '[0-9]'  • 		(0)
    |number ->  number  •  '[0-9]' 		(0)
    |""".stripMargin.replace("\r", "")
    Assert.assertEquals(expectedChart, acceptedInput.repr) //This is really hack, but its an easy way to compare the expected chart with the real one

    val expectedLastState = "number ->  number  '[0-9]'  • \t\t(0)"
    //number ->  number  '[0-9]'  • \t\t(0)
    Assert.assertEquals(expectedLastState, acceptedInput.getLastStates.head.repr)

    println(rejectedInput.repr)
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

    ---- 4 ---

    */
  }

  def testBasicNumericGrammar_RightRecursive: Unit = {
    val grammar = Grammar("number",
      List(
        Rule("number", List(TerminalSymbol("[0-9]".r), NonTerminalSymbol("number"))),
        Rule("number", List(TerminalSymbol("[0-9]".r)))
      )
    )
    println(grammar.repr) /*
    start: number
    number -> '[0-9]' number
    number -> '[0-9]'
    */

    val earley = Earley(grammar)
    var acceptedInput: EarleyChart = earley.buildChart("123")
    val rejectedInput = earley.buildChart("12a3")

    Assert.assertTrue(acceptedInput.accepts)
    Assert.assertFalse(rejectedInput.accepts)

    Assert.assertEquals(acceptedInput.size, 4)
    Assert.assertEquals(acceptedInput(0).size, 2)
    Assert.assertEquals(acceptedInput(1).size, 4)
    Assert.assertEquals(acceptedInput(2).size, 5)
    Assert.assertEquals(acceptedInput(3).size, 6)


    println(acceptedInput.repr)
    val expectedChart = """
    |---- 0 ---
    |number ->  •  '[0-9]'  number 		(0)
    |number ->  •  '[0-9]' 		(0)
    |
    |---- 1 ---
    |number ->  '[0-9]'  •  number 		(0)
    |number ->  '[0-9]'  • 		(0)
    |number ->  •  '[0-9]'  number 		(1)
    |number ->  •  '[0-9]' 		(1)
    |
    |---- 2 ---
    |number ->  '[0-9]'  •  number 		(1)
    |number ->  '[0-9]'  • 		(1)
    |number ->  •  '[0-9]'  number 		(2)
    |number ->  •  '[0-9]' 		(2)
    |number ->  '[0-9]'  number  • 		(0)
    |
    |---- 3 ---
    |number ->  '[0-9]'  •  number 		(2)
    |number ->  '[0-9]'  • 		(2)
    |number ->  •  '[0-9]'  number 		(3)
    |number ->  •  '[0-9]' 		(3)
    |number ->  '[0-9]'  number  • 		(1)
    |number ->  '[0-9]'  number  • 		(0)
    |""".stripMargin.replace("\r", "")
    Assert.assertEquals(expectedChart, acceptedInput.repr)

    val expectedLastStateRepr = "number ->  '[0-9]'  number  • 		(0)"
    Assert.assertEquals(expectedLastStateRepr, acceptedInput.getLastStates.head.repr)

    //The EarleyChart for the right recursive (RR) grammar is bigger than the output of the left recursive (LR) grammar.
    //If possible, write the grammar in the LR form instead of the RR form as the algo. performs faster on LR grammars
  }
}
