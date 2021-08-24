package earleyscala

import scala.collection.mutable.ArrayBuffer

case class Node(repr: String, value: EarleyState, children: ArrayBuffer[Node]) {
  override def toString: String = {
    repr
  }
}

class ParseTree {
  def traversal(root: EarleyState, input: String, parent: Option[Node] = None): Node = {
    val children = root.predecessors.reverse

    var repr = ""
    if (root.isComplete) {
      repr = root.rule.toString
    } else if (root.rule.symbols(root.dotPosition).isInstanceOf[TerminalSymbol]) {
      repr = input.charAt(root.endPosition).toString
    }
    val node = Node(repr, root, new ArrayBuffer[Node]())
    children.foreach(p => {
      if (root.isComplete) traversal(p.to, input, Option(node))
      else traversal(p.to, input, parent)
    })

    if (root.isComplete || root.rule.symbols(root.dotPosition).isInstanceOf[TerminalSymbol]) {
      if (parent.isEmpty) return node
      else parent.get.children.append(node)
    }
    node
  }

}
