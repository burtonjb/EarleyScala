package earleyscala

import junit.framework.TestCase
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import org.junit.Test

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class testSimpleAmbiguousGrammar extends TestCase {
  val TreeUtils = new FullTreeUtils

  @Test
  def test_1_EScottExample2(): Unit = {
    //grammar from here: https://www.sciencedirect.com/science/article/pii/S1571066108001497 - Paper by Elizabeth Scott; the example 2 grammar
    val grammar = Grammar("S",
      List(
        Rule("S", List(NonTerminalSymbol("S"), NonTerminalSymbol("S"))),
        Rule("S", List(TerminalSymbol("b")))
      )
    ) // S -> SS|b

    val input = "bbb"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head
    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)
    /*
    (second derivation)
        b
      S -> 'b'
          b
        S -> 'b'
          b
        S -> 'b'
      S -> S S
    (first derivation)
          b
        S -> 'b'
          b
        S -> 'b'
      S -> S S
        b
      S -> 'b'
    (shared root)
    S -> S S
    */
  }

  @Test
  def test_2_Grammar(): Unit = {
    //grammar from - https://en.wikipedia.org/wiki/Ambiguous_grammar#Addition_and_subtraction
    val grammar = Grammar("A",
      List(
        Rule("A", List(NonTerminalSymbol("A"), TerminalSymbol("\\+"), NonTerminalSymbol("A"))),
        Rule("A", List(TerminalSymbol("a")))
      )
    ) //A → A + A | a

    val earley = Earley(grammar)
    val input = "a+a+a"
    // There's two ways to parse this input. ((a+a)+a) or (a+(a+a))
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head
    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)
    /*
    (second derivation)
        a
      A -> 'a'
      +
          a
        A -> 'a'
        +
          a
        A -> 'a'
      A -> A '\+' A
    (first derivation)
          a
        A -> 'a'
        +
          a
        A -> 'a'
      A -> A '\+' A
      +
        a
      A -> 'a'
    (shared root)
    A -> A '\+' A
    */
  }
}

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class testSimpleAmbiguousGrammarDisambiguated extends TestCase {
  val TreeUtils = new DisambiguatingTreeUtils

  @Test
  def test_1_EScottExample2(): Unit = {
    //grammar from here: https://www.sciencedirect.com/science/article/pii/S1571066108001497 - Paper by Elizabeth Scott; the example 2 grammar
    val grammar = Grammar("S",
      List(
        Rule("S", List(NonTerminalSymbol("S"), NonTerminalSymbol("S"))),
        Rule("S", List(TerminalSymbol("b")))
      )
    ) // S -> SS|b

    val input = "bbb"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head
    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)
    /*
        b
      S -> 'b'
          b
        S -> 'b'
          b
        S -> 'b'
      S -> S S
    S -> S S
    */
  }

  @Test
  def test_2_Grammar(): Unit = {
    //grammar from: https://en.wikipedia.org/wiki/Ambiguous_grammar#Addition_and_subtraction
    val grammar = Grammar("A",
      List(
        Rule("A", List(NonTerminalSymbol("A"), TerminalSymbol("\\+"), NonTerminalSymbol("A"))),
        Rule("A", List(TerminalSymbol("a")))
      )
    ) //A → A + A | a

    val earley = Earley(grammar)
    val input = "a+a+a"
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
        +
          a
        A -> 'a'
      A -> A '\+' A
    A -> A '\+' A
    */
  }
}