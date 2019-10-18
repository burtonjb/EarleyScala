package earleyscala

import scala.collection.mutable

trait TreeUtils[T] {
  /*The traversal is a post-order traversal, so it will output the leaves before outputting the root.
   * This makes the display upside-down, but its somewhat easy to get used to.
  */
  def traversal(root: EarleyState, input: String, callback: (EarleyState, String, Int) => T, depth: Int = 0): T

  def createTree(root: EarleyState, input: String, depth: Int = 0): T

  def createLeaves(root: EarleyState, input: String, depth: Int = 0): T
}

class FullTreeUtils extends TreeUtils[Unit] {
  override def traversal(root: EarleyState, input: String, callback: (EarleyState, String, Int) => Unit, depth: Int = 0): Unit = {
    root.predecessors.reverse.foreach(p => {
      if (root.complete) traversal(p.to, input, callback, depth + 1)
      else traversal(p.to, input, callback, depth)
    })
    callback(root, input, depth)
  }

  override def createTree(root: EarleyState, input: String, depth: Int): Unit = {
    traversal(root, input, printTree)
  }

  override def createLeaves(root: EarleyState, input: String, depth: Int = 0): Unit = {
    traversal(root, input, printLeaves)
  }

  protected def printTree(root: EarleyState, input: String, depth: Int): Unit = {
    if (root.complete) {
      println("\t" * depth + root.rule.repr)
    } else if (root.rule.symbols(root.dotPosition).isInstanceOf[TerminalSymbol]) {
      println("\t" * depth + input.charAt(root.endPosition))
    }
  }

  protected def printLeaves(root: EarleyState, input: String, depth: Int): Unit = {
    if (root.complete) {}
    else if (root.rule.symbols(root.dotPosition).isInstanceOf[TerminalSymbol]) {
      print(input.charAt(root.endPosition))
    }
  }
}

class DisambiguatingTreeUtils extends FullTreeUtils {
  override def traversal(root: EarleyState, input: String, callback: (EarleyState, String, Int) => Unit, depth: Int = 0): Unit = {
    val children = root.predecessors.reverse
    disambiguate(children).foreach(p => {
      if (root.complete) traversal(p.to, input, callback, depth + 1)
      else traversal(p.to, input, callback, depth)
    })
    callback(root, input, depth)
  }

  protected def disambiguate(children: mutable.Buffer[Pointer]): mutable.Buffer[Pointer] = {
    //FIXME: pretty sure this will return incorrect results when a scan happens and then a state completes.
    //Completes have a predecessorPointer and then a reductionPointer. A scan just has a predecessorPointer, so this will take the scan and then the first part of the complete, which will be wrong
    val b = children.groupBy(p => p.label).toList.flatMap(p => {
      p._2.take(2)
    })
    b.toBuffer
  }
}
