package earleyscala

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

    var earley = Earley(grammar)
    var acceptedInput = earley.buildChart("123")
    var rejectedInput = earley.buildChart("12a3")

    Assert.assertTrue(acceptedInput.accepts)
    Assert.assertFalse(rejectedInput.accepts)

    println(acceptedInput.repr)
    /* Output chart
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
    println(acceptedInput.getLastStates.head.repr)
    /*
    number ->  number  '[0-9]'  • 		(0)
     */

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

    var earley = Earley(grammar)
    var acceptedInput = earley.buildChart("123")
    var rejectedInput = earley.buildChart("12a3")

    Assert.assertTrue(acceptedInput.accepts)
    Assert.assertFalse(rejectedInput.accepts)

    println(acceptedInput.repr)
    /* Output chart
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
    println(acceptedInput.getLastStates.head.repr)
    /*
    number ->  '[0-9]'  number  • 		(0)
     */

    //As you can see from the output of the two test cases, the EarleyChart for the right recursive (RR) grammar is bigger than the output of the left recursive (LR) grammar.
    //if you're using this algorithm, I'd recommend writing your grammars in LR form.
  }
}
