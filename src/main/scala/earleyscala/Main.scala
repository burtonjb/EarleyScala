package earleyscala

object Main {
  def main(args: Array[String]): Unit = {
    val grammar = Grammar("S",
      List(
        Rule("S", List(TerminalSymbol("(a|b|c)"), NonTerminalSymbol("S"), TerminalSymbol("(a|b|c)"))),
        Rule("S", List(TerminalSymbol("(a|b|c)"))),
      )
    )
    //This grammar will accept palindromes with an a or b in the middle (so 'aa' is not accepted).

    val input = "abc"
    val earley = Earley(grammar)
    val end = earley.buildChart(input).getLastStates.head

    FullTreeUtils.createLeaves(end, input)
    println()
    FullTreeUtils.createTree(end, input)
  }
}
