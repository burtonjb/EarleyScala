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
        val state = S(i)(j)
        val symbol = state.nextSymbol
        symbol match {
          //TODO: to handle epsilon productions repeat predict and complete until the size of S(i) stops changing
          case None => complete(S, i, j)
          case Some(nt: NonTerminalSymbol) => predict(S, i, nt)
          case Some(t: TerminalSymbol) => scan(S, i, j, t, input)
          case _ => throw new IllegalStateException("Unable to recognize pattern")
        }
        j += 1
      }
      i += 1
    }
    S //Return the EarleyChart
  }

  private def complete(S: EarleyChart, i: Int, j: Int): Unit = {
    val state = S(i)(j)
    S(state.startPosition).foreach(oldState => {
      val symbol = oldState.nextSymbol
      if (symbol.isDefined && symbol.get.isInstanceOf[NonTerminalSymbol]) {
        if (symbol.get.asInstanceOf[NonTerminalSymbol].ruleName == state.rule.name) {
          var newState = EarleyState(oldState.rule, oldState.dotPosition + 1, oldState.startPosition)(i, "complete")
          if (!S(i).contains(newState)) {
            S(i).append(newState)
          } else {
            newState = S(i).findLast(e => e == newState).get
          }
          val reductionPointer = ReductionPointer(i, newState, state)
          val predecessorPointer = PredecessorPointer(i, newState, oldState)
          newState.predecessors.append(reductionPointer)
          newState.predecessors.append(predecessorPointer)
        }
      }
    })
  }

  private def scan(S: EarleyChart, i: Int, j: Int, symbol: TerminalSymbol, input: String): Unit = {
    val state = S(i)(j)
    if (i < input.length && symbol.matches(input.substring(i, i + 1))) {
      val newState = EarleyState(state.rule, state.dotPosition + 1, state.startPosition)(i + 1, "scan")
      S(i + 1).append(newState)
      val pointer = PredecessorPointer(i, newState, state)
      newState.predecessors.append(pointer)
    }
  }

  private def predict(S: EarleyChart, i: Int, symbol: NonTerminalSymbol): Unit = {
    grammar.getRulesByName(symbol.ruleName) foreach (rule => {
      val state = EarleyState(rule, 0, i)(i, "predict")
      if (!S(i).contains(state)) {
        S(i).append(state)
      }
    })
  }
}
