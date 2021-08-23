package earleyscala

import junit.framework.TestCase

class testEpsilonGrammar extends TestCase {
  val TreeUtils = new DisambiguatingTreeUtils
  def testEpsilonGrammar: Unit = {
    val grammar = Grammar("A",
      List(
        Rule("A", List()),
        Rule("A", List(NonTerminalSymbol("B"))),
        Rule("B", List(NonTerminalSymbol("A")))
      )
    ) //Matches the empty string with infinite derivations

    val input = ""
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)

    println(chart.repr())
    /*
    ---- 0 ---
    A ->  • 		(0)
    A ->  •  B 		(0)
    B ->  •  A 		(0)
    B ->  A  • 		(0)
    A ->  B  • 		(0)
    */

    val end = chart.getLastStates.head
    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)
    /*
    A ->
    */
  }

  def testAycockHorspoolExample: Unit = {
    //This test case was taken from Practical Earley Parsing (John Aycock, R. Nigel Horspool) [2002]
    val grammar = Grammar("Start",
      List(
        Rule("Start", List(NonTerminalSymbol("S"))),
        Rule("S", List(NonTerminalSymbol("A"), NonTerminalSymbol("A"), NonTerminalSymbol("A"), NonTerminalSymbol("A"))),
        Rule("A", List(TerminalSymbol("a"))),
        Rule("A", List(NonTerminalSymbol("E"))),
        Rule("E", List())
      )
    )

    val input = "a"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)

    println(chart.repr())
    /*
    ---- 0 ---
    Start ->  •  S 		(0)
    S ->  •  A  A  A  A 		(0)
    S ->  A  •  A  A  A 		(0)
    S ->  A  A  •  A  A 		(0)
    S ->  A  A  A  •  A 		(0)
    S ->  A  A  A  A  • 		(0)
    A ->  •  'a' 		(0)
    A ->  •  E 		(0)
    A ->  E  • 		(0)
    Start ->  S  • 		(0)
    E ->  • 		(0)

    ---- 1 ---
    A ->  'a'  • 		(0)
    S ->  A  •  A  A  A 		(0)
    S ->  A  A  •  A  A 		(0)
    S ->  A  A  A  •  A 		(0)
    S ->  A  A  A  A  • 		(0)
    A ->  •  'a' 		(1)
    A ->  •  E 		(1)
    A ->  E  • 		(1)
    Start ->  S  • 		(0)
    E ->  • 		(1)
    */

    val end = chart.getLastStates.head
    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)
    /*
            a
        A -> 'a'
          E ->
        A -> E
          E ->
        A -> E
          E ->
        A -> E
      S -> A A A A
    Start -> S
    */
    //There are 4 parse trees for this grammar and input, but I'm using a disamiguation strategy to only pick one parse tree
    //The complete parse forest is included below, but its not very useful to look at
    /*
            a
        A -> 'a'
          E ->
        A -> E
          E ->
        A -> E
          a
        A -> 'a'
          E ->
        A -> E
          E ->
        A -> E
          E ->
        A -> E
          E ->
        A -> E
          a
        A -> 'a'
          E ->
        A -> E
          E ->
        A -> E
          E ->
        A -> E
          E ->
        A -> E
          E ->
        A -> E
          E ->
        A -> E
          E ->
        A -> E
          E ->
        A -> E
          a
        A -> 'a'
      S -> A A A A
    Start -> S
    */

  }
}
