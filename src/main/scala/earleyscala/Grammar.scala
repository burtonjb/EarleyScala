package earleyscala

import java.util.UUID

import scala.util.matching.Regex

/*
 * This file has classes that are used to build a grammar.
 */

sealed trait Symbol {
  def repr: String //Used to pretty print the symbols. toString will have the class name too
}

final case class NonTerminalSymbol(ruleName: String) extends Symbol {
  override def repr: String = ruleName
}

final case class TerminalSymbol(r: Regex) extends Symbol { //r should be a regex to match a single character
  override def repr: String = s"'${r.pattern.toString}'"
}


/*
 * Rules (Production rules) are simple replacements. They have a non-terminal left hand side and a set of symbols (terminal or non-terminal) on the right hand side.
 * An example rule could be:
 * A -> A a
 * I'm currently not supporting Or statements, but they are easily supported by having two rules, so if A could be replaced with aA or A then it could be represented with the two following rules:
 * A -> a A
 * A -> A
 */
case class Rule(name: String, symbols: List[Symbol], id: String = UUID.randomUUID().toString) { //duplicate rules are technically allowed, so id is to make the rules unique
  def repr: String = {
    val sb = new StringBuilder()
    sb.append(name + " -> ")
    symbols.foreach(s => sb.append(s.repr + " "))
    sb.toString
  }
  /* TODO: add a flag if the rule is a nullable rule.
 * A nullable rule is composed of only nullable symbols
 * A nullable symbol is a symbol that names at least 1 nullable rule.
 */
}
/*
 * A grammar represents all possible strings in a language.
 * For my parser, a grammar consists of a starting rule and a set of production rules; though formally they are usually a 4 tuple, all information can be derived from my representation.
 */
case class Grammar(startRuleName: String, rules: List[Rule]) {
  def repr: String = {
    val sb = new StringBuilder()
    sb.append(s"start: $startRuleName\n")
    rules.foreach(r => sb.append(r.repr + '\n'))
    sb.toString
  }

  def getRulesByName(name: String): List[Rule] = {
    rules.filter(r => r.name == name)
  }
}
