package earleyscala

object Main {
  def main(args: Array[String]): Unit = {
    val TreeUtils = new DisambiguatingTreeUtils
    val grammar = Grammar("A",
      List(
        Rule("A", List(NonTerminalSymbol("A"), TerminalSymbol("\\+"), NonTerminalSymbol("A"))),
        Rule("A", List(TerminalSymbol("\\d")))
      )
    ) //A → A + A | a

    val earley = Earley(grammar)
    val input = "7+7+5+1"
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head

    TreeUtils.createLeaves(end, input)
    println()
    TreeUtils.createTree(end, input)
    println()

    val n = new ParseTree().traversal(end, input)
    println(actions(n, grammar))

    /*
    7+7+5+1

        7
      A -> '\d'
      +
          7
        A -> '\d'
        +
            5
          A -> '\d'
          +
            1
          A -> '\d' 
        A -> A '\+' A
      A -> A '\+' A
    A -> A '\+' A

    20
    */
  }

  def actions(n: Node, grammar: Grammar): Int = {
    if (n.value.rule == grammar.rules(0)) {
      actions(n.children(0), grammar) + actions(n.children(2), grammar)
    }
    else if (n.value.rule == grammar.rules(1)) {
      n.children(0).repr.toInt
    }
    else {
      throw new IllegalStateException("Somehow did not get a rule")
    }
  }
}
