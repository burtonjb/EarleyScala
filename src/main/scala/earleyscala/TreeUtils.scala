package earleyscala

object TreeUtils {
  def postOrderTraversal[T](root: EarleyState, input: String, f: (EarleyState, Int) => T, depth: Int = 0): T = {
    root.predecessors.filter(p => true).foreach(p => postOrderTraversal(p.to, input, f, depth + 1))
    f(root, depth)
  }

  def printLeaves(root: EarleyState, input: String): String = {
    //Useful for checking that the leaves of the tree match the input, but will not work if there's multiple derivations
    val sb = new StringBuilder()
    val f = (es: EarleyState, depth: Int) => {
      if (es.createdFrom == "scan") sb.append(input.charAt(es.endPosition - 1))
      sb
    }
    postOrderTraversal(root, input, f)
    sb.toString
  }

  def printCompleteAndLeaves(root: EarleyState, input: String): String = {
    val sb = new StringBuilder()
    val f = (es: EarleyState, depth: Int) => {
      if (es.createdFrom == "scan") sb.append("\t" * (depth + 1) + input.charAt(es.endPosition - 1) + s" (${es.startPosition}, ${es.endPosition}) [${depth + 1}]" + "\n")
      if (es.complete) sb.append("\t" * depth + es.rule.repr + s" (${es.startPosition}, ${es.endPosition}) [$depth]" + "\n")
      sb
    }
    postOrderTraversal(root, input, f)
    sb.toString
  }
}
