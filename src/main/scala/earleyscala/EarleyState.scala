package earleyscala

/*
Each state is a tuple (X → α • β, i), consisting of
* the production currently being matched (X → α β)
* our current position in that production (represented by the dot)
* the position i in the input at which the matching of this production began: the origin position
*
* The second set of fields (the curried fields) are just used to store more debugging information
*/
case class EarleyState(rule: Rule, dotPosition: Int, startPosition: Int) //scala case class trick - only these values will be used for equals, hashcode and toString
                      (val endPosition: Int, val createdFrom: String) { //These values are for displaying the state, but are not part of the original definition for the E.S.

  def repr: String = {
    val sb = new StringBuilder
    sb.append(rule.name + " -> ")
    rule.symbols.zipWithIndex.foreach(p => {
      val (symbol, index) = p
      if (index == dotPosition) sb.append(" • ")
      sb.append(" " + symbol.repr + " ")
    })
    if (complete) sb.append(" • ")
    sb.append("\t\t(" + startPosition + ")")
    sb.toString
  }

  def complete: Boolean = {
    rule.symbols.size == dotPosition
  }
}