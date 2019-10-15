package earleyscala

import scala.collection.mutable

case class Earley(grammar: Grammar) {

  /*
  A rule is nullable if:
    It has a production that can be nullable (S -> s|Ɛ is nullable)
    It is composed of only non-terminals that are nullable (T -> SSS is nullable, using S from above)
  A rule with only terminals is never nullable
   */
  private val nullableRules = new mutable.HashSet[String]()
  buildNullableRules(grammar)
  
  private def buildNullableRules(grammar: Grammar): Unit = {
    //FIXME: This is a O(n^2) way of finding nullable rules. It should probably be converted to a O(n) way of doing this eventually
    var s = nullableRules.size - 1
    while (s < nullableRules.size) {
      s = nullableRules.size
      for (r <- grammar.rules) {
        if (r.symbols.isEmpty) {
          nullableRules.add(r.name)
        }
        else {
          if (r.symbols.exists(t => t.isInstanceOf[NonTerminalSymbol] && nullableRules.contains(t.asInstanceOf[NonTerminalSymbol].ruleName))) {
            nullableRules.add(r.name)
          }
        }
      }
    }
  }

  def nullableRule(s: Symbol): Boolean = {
    s.isInstanceOf[NonTerminalSymbol] && nullableRules.contains(s.asInstanceOf[NonTerminalSymbol].ruleName)
  }

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
      completePredict(rule, S, i, state)
    })
  }

  private def completePredict(rule: Rule, S: EarleyChart, i: Int, predictedState: EarleyState): Unit = {
    var oldState = predictedState
    rule.symbols.zipWithIndex.foreach(pair => {
      val (s, index) = pair
      if (nullableRule(s)) {
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
