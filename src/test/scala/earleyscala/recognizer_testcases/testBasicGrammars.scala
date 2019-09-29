package earleyscala.recognizer_testcases

import earleyscala._
import junit.framework.TestCase
import org.junit.Assert

class testBasicGrammars extends TestCase{
  def testPalindrome: Unit = {
    val grammar = Grammar("S",
      List(
        Rule("S", List(TerminalSymbol("a".r), NonTerminalSymbol("S"), TerminalSymbol("a".r))),
        Rule("S", List(TerminalSymbol("b".r), NonTerminalSymbol("S"), TerminalSymbol("b".r))),
        Rule("S", List(TerminalSymbol("a|b".r))),
      )
    )
    //This grammar will accept palindromes with an a or b in the middle (so 'aa' is not accepted).

    val acceptedInputs = List(
      "a",
      "b",
      "aaa",
      "aba",
      "ababa",
      "baabaab"
    )
    val rejectedInputs = List(
      "ab",
      "abb",
      "abab",
      "abbabaab"
    )

    val earley = Earley(grammar)

    acceptedInputs.foreach(input => {
      Assert.assertTrue(earley.buildChart(input).accepts)
    })
    rejectedInputs.foreach(input => {
      Assert.assertFalse(earley.buildChart(input).accepts)
    })
  }
}
