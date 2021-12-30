package earleyscala

import scala.collection.mutable.ArrayBuffer

//Do not construct instances of this class. Use Earley(grammar).buildChart(input) to make this object
class EarleyChart(val grammar: Grammar, val input: String) {
  val S: ArrayBuffer[ArrayBuffer[EarleyState]] = new ArrayBuffer[ArrayBuffer[EarleyState]]()
  init()

  def init(): Unit = {
    input.foreach(_ => S.append(new ArrayBuffer[EarleyState]))
    S.append(new ArrayBuffer[EarleyState])

    //put the initial rules into S[0]
    grammar.getRulesByName(grammar.startRuleName).foreach(rule => {
      S(0).append(EarleyState(rule, 0, 0)(0, "start"))
    })
  }

  // functions
  def size: Int = {
    S.size
  }

  def apply(i: Int): ArrayBuffer[EarleyState] = {
    //TODO: replace this with an EarleyStateSet object with O(1) contains and O(1) add
    S(i)
  }

  def completedView: EarleyChart = {
    //returns a copy of the chart with just the completed items.
    val out = new EarleyChart(grammar, input)
    out.S(0).clear()
    for (i <- 0 until S.size) {
      S(i).foreach(item => {
        if (item.isComplete) {
          out.S(i).append(item)
        }
      })
    }
    out
  }

  def accepts: Boolean = getLastStates.nonEmpty

  def getLastStates: List[EarleyState] = {
    //This method will return no states if the recognizer did not accept the input string from the grammar
    //The method will return 1 state for unambiguous grammars or 1-or-more states for ambiguous grammars if the recognizer accepts the input string
    S.last.filter(state => {
      state.rule.name == grammar.startRuleName && state.isComplete && state.startPosition == 0
    }).toList
  }

  def getLastRow: Int = {
    // Find the last row that isn't empty. If the parse failed, this provides an indication
    // of how far the parser got before it failed to find any more matches.
    for (pos <- S.indices) {
      if (S(pos).isEmpty) {
        return pos - 1
      }
    }
    S.size - 1
  }

  def getRow(num: Int): List[EarleyState] = {
    // Return any "interesting" states in the row. Combined with getLastRow() and a failed parse,
    // this can provide clues about what rule(s) the parser was "in the middle of" when parsing
    // failed. A state is interesting if it has predecessors because they're likely to be
    // less ambiguous than single token matches.
    if (num < 0 || num >= S.size) {
      ArrayBuffer.empty[EarleyState].toList
    } else {
      S(num).filter(state => {
        state.predecessors.nonEmpty
      }).toList
    }
  }

  def repr(): String = {
    repr(earleyState => earleyState.toString + '\n')
  }

  def completeRepr(): String = {
    repr(earleyState => earleyState.completeRepr + '\n')
  }

  // Util function
  def repr(f: EarleyState => String): String = {
    val s = new StringBuilder()
    S.zipWithIndex.foreach(p => {
      val (set, i) = p
      s.append("\n---- " + i + " ---\n")
      set.foreach(earleyItem => {
        s.append(f(earleyItem))
      })
    })
    s.toString()
  }
}
