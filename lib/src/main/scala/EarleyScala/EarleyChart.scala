package earleyscala

import scala.collection.mutable.ArrayBuffer

//Do not construct instances of this class. Use Earley(grammar).buildChart(input) to make this object
class EarleyChart(val grammar: Grammar, val input: String) {
  val S: ArrayBuffer[ArrayBuffer[EarleyState]] = new ArrayBuffer[ArrayBuffer[EarleyState]]()
  init()

  def init() {
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
    out.S(0).clear
    for (i <- 0 until S.size) {
      S(i).foreach(item => {
        if (item.complete) {
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
      state.rule.name == grammar.startRuleName && state.complete && state.startPosition == 0
    }).toList
  }

  def repr(): String = {
    repr(earleyState => earleyState.repr + '\n')
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
