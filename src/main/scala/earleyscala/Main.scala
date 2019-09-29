package earleyscala

object Main {
  def main(args: Array[String]): Unit = {
    val grammar = Grammar("S",
      List(
        Rule("S", List(TerminalSymbol("a".r), NonTerminalSymbol("S"), TerminalSymbol("a".r))),
        Rule("S", List(TerminalSymbol("a".r))),
      )
    )
    //This grammar will accept palindromes with an a or b in the middle (so 'aa' is not accepted).

    val input = "aaa"
    val earley = Earley(grammar)
    val end = earley.buildChart(input).getLastStates.head
    println(TreeUtils.printCompleteAndLeaves(end, input))
  }
}
