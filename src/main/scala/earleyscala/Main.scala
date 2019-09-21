package earleyscala

object Main {
  def main(args: Array[String]): Unit = {
    val grammar = Grammar("sum",
      List(
        Rule("sum", List(NonTerminalSymbol("sum"), TerminalSymbol("[+-]".r), NonTerminalSymbol("product"))),
        Rule("sum", List(NonTerminalSymbol("product"))),
        Rule("product", List(NonTerminalSymbol("product"), TerminalSymbol("[/*]".r), NonTerminalSymbol("factor"))),
        Rule("product", List(NonTerminalSymbol("factor"))),
        Rule("factor", List(TerminalSymbol("\\(".r), NonTerminalSymbol("sum"), TerminalSymbol("\\)".r))),
        Rule("factor", List(NonTerminalSymbol("number"))),
        Rule("number", List(TerminalSymbol("[0-9]".r), NonTerminalSymbol("number"))),
        Rule("number", List(TerminalSymbol("[0-9]".r)))
      )
    )

    val input = "1+(2*3-4)"
    val earley = Earley(grammar)
    val chart = earley.buildChart(input)

    println(chart.repr)
    println(chart.completedView.repr)
    println(chart.accepts)
  }
}
