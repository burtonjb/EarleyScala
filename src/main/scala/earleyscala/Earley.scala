package earleyscala

case class Earley(grammar: Grammar) {
  def buildChart(input: String): EarleyChart = {
    // build the initial chart
    val S = new EarleyChart(grammar, input)

    //create the rest of the chart
    var i = 0
    while (i < S.size) { //S and S[i] can change size as the loops are running
      var j = 0
      while (j < S(i).size) {
        val symbol = nextSymbol(grammar, S(i)(j))
        symbol match {
          case None => complete(S, i, j, grammar)
          case Some(nt: NonTerminalSymbol) => predict(S, i, nt, grammar)
          case Some(t: TerminalSymbol) => scan(S, i, j, t, input)
          case _ => throw new IllegalStateException("Unable to recognize pattern")
        }
        j += 1
      }
      i += 1
    }
    S //Return the EarleyChart
  }

  private def complete(S: EarleyChart, i: Int, j: Int, grammar: Grammar): Unit = {
    val item = S(i)(j)
    S(item.startPosition).foreach(earleyItem => {
      val oldSymbol = nextSymbol(grammar, earleyItem)
      if (oldSymbol.isDefined && oldSymbol.get.isInstanceOf[NonTerminalSymbol]) {
        if (oldSymbol.get.asInstanceOf[NonTerminalSymbol].ruleName == item.rule.name) {
          val newItem = EarleyState(earleyItem.rule, earleyItem.dotPosition + 1, earleyItem.startPosition)(i, "complete")
          if (!S(i).contains(newItem)) {
            S(i).append(newItem)
          }
        }
      }
    })
  }

  private def scan(S: EarleyChart, i: Int, j: Int, symbol: TerminalSymbol, input: String): Unit = {
    val item = S(i)(j)
    if (i < input.length && symbol.r.matches(input.substring(i, i + 1))) {
      S(i + 1).append(EarleyState(item.rule, item.dotPosition + 1, item.startPosition)(i + 1, "scan"))
    }
  }

  private def predict(S: EarleyChart, i: Int, symbol: NonTerminalSymbol, grammar: Grammar): Unit = {
    grammar.getRulesByName(symbol.ruleName) foreach (rule => {
      val earleyItem = EarleyState(rule, 0, i)(i, "predict")
      if (!S(i).contains(earleyItem)) {
        S(i).append(earleyItem)
      }
      //TODO: if the rule is nullable:
      //append to S[i] EarleyState(S(i)(j).ruleIndex, S(i)(j).dotIndex + 1, S(i)(j).origin(
    })
  }

  private def nextSymbol(grammar: Grammar, earleyItem: EarleyState): Option[Symbol] = {
    if (earleyItem.dotPosition >= earleyItem.rule.symbols.size)
      Option.empty
    else
      Option(earleyItem.rule.symbols(earleyItem.dotPosition))
  }
}
