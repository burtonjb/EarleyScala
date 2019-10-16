package earleyscala

/*
* The state set at input position k is called S(k). The parser is seeded with S(0) consisting of only the top-level rule.
* The parser then repeatedly executes three operations: prediction, scanning, and completion.
*
* Prediction: For every state in S(k) of the form (X → α • Y β, j) (where j is the origin position as above), add (Y → • γ, k) to S(k) for every production in the grammar with Y on the left-hand side (Y → γ).
* Scanning: If a is the next symbol in the input stream, for every state in S(k) of the form (X → α • a β, j), add (X → α a • β, j) to S(k+1).
* Completion: For every state in S(k) of the form (Y → γ •, j), find all states in S(j) of the form (X → α • Y β, i) and add (X → α Y • β, i) to S(k).
*
* Handling epsilon productions is done by running complete after predict if the symbol(s) after the dot are nullable.
* If (S → • N ... , i) is the predicted state, and N is nullable, complete the state with (S → N • ... , i). Then repeat the check/complete for the next symbol in S
 */
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
          case Some(t: TerminalSymbol) => scan(S, i, j, t, input)
          case Some(nt: NonTerminalSymbol) => predict(S, i, nt)
          case None => complete(S, i, j)
          case _ => throw new IllegalStateException("Unable to recognize pattern")
        }
        j += 1
      }
      i += 1
    }
    S //Return the EarleyChart
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
    grammar.getRulesByName(symbol.ruleName).foreach(rule => {
      val state = EarleyState(rule, 0, i)(i, "predict")
      if (!S(i).contains(state)) {
        S(i).append(state)
      }
      completePrediction(rule, S, i, state)
    })
  }

  private def complete(S: EarleyChart, i: Int, j: Int): Unit = {
    val completedState = S(i)(j)
    S(completedState.startPosition).foreach(oldState => {
      val symbol = oldState.nextSymbol
      if (symbol.isDefined && symbol.get.isInstanceOf[NonTerminalSymbol]) {
        if (symbol.get.asInstanceOf[NonTerminalSymbol].ruleName == completedState.rule.name) {
          var newState = EarleyState(oldState.rule, oldState.dotPosition + 1, oldState.startPosition)(i, "complete")
          if (!S(i).contains(newState)) {
            S(i).append(newState)
          } else {
            newState = S(i).findLast(e => e == newState).get
          }
          val reductionPointer = ReductionPointer(i, newState, completedState)
          val predecessorPointer = PredecessorPointer(i, newState, oldState)
          newState.predecessors.append(reductionPointer)
          newState.predecessors.append(predecessorPointer)
        }
      }
    })
  }

  private def completePrediction(rule: Rule, S: EarleyChart, i: Int, predictedState: EarleyState): Unit = {
    var oldState = predictedState
    rule.symbols.zipWithIndex.foreach(pair => {
      val (s, index) = pair
      if (s.nullable) {
        var newState = EarleyState(rule, index + 1, i)(i, "complete")
        if (!S(i).contains(newState)) {
          S(i).append(newState)
        } else {
          newState = S(i).findLast(e => e == newState).get
        }
        val reductionPointer = ReductionPointer(i, newState, oldState)
        newState.predecessors.append(reductionPointer)
        if (!S(i).contains(newState)) {
          S(i).append(newState)
        }
        oldState = newState
      }
    })
  }
}
