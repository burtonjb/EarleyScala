package earleyscala.parser

import earleyscala._
import junit.framework.TestCase

class testBnfParser extends TestCase {
  val TreeUtils = new DisambiguatingTreeUtils

  //grammar from - https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form#Further_examples
  //I've removed all the epsilon productions from the definition because I was running into performance issues (possibly because of the bug in the disambiguate call, or maybe because I'm not using the SPPF properly)
  val grammar = Grammar("syntax",
    List(
      Rule("syntax", List(NonTerminalSymbol("rule"))),
      Rule("syntax", List(NonTerminalSymbol("syntax"), NonTerminalSymbol("rule"))),

      Rule("rule", List(NonTerminalSymbol("whitespace"), TerminalSymbol("<"), NonTerminalSymbol("rule-name"), TerminalSymbol(">"), NonTerminalSymbol("whitespace"),
        TerminalSymbol(":"), TerminalSymbol(":"), TerminalSymbol("="), NonTerminalSymbol("whitespace"), NonTerminalSymbol("expression"), NonTerminalSymbol("line-end"))),

      Rule("whitespace", List(NonTerminalSymbol("whitespace"), TerminalSymbol(" "))),
      Rule("whitespace", List(TerminalSymbol(" "))),

      Rule("expression", List(NonTerminalSymbol("list"))),
      Rule("expression", List(NonTerminalSymbol("list"), NonTerminalSymbol("whitespace"), TerminalSymbol("\\|"), NonTerminalSymbol("whitespace"), NonTerminalSymbol("expression"))),

      Rule("line-end", List(NonTerminalSymbol("EOL"))),

      Rule("list", List(NonTerminalSymbol("term"))),
      Rule("list", List(NonTerminalSymbol("term"), NonTerminalSymbol("whitespace"), NonTerminalSymbol("list"))),

      Rule("term", List(NonTerminalSymbol("literal"))),
      Rule("term", List(TerminalSymbol("<"), NonTerminalSymbol("rule-name"), TerminalSymbol(">"))),

      Rule("literal", List(TerminalSymbol("'"), NonTerminalSymbol("literal-def"), TerminalSymbol("'"))),

      Rule("character", List(NonTerminalSymbol("letter"))),
      Rule("character", List(NonTerminalSymbol("digit"))),
      Rule("character", List(NonTerminalSymbol("symbol"))),

      Rule("letter", List(TerminalSymbol("[A-Za-z]"))),
      Rule("digit", List(TerminalSymbol("[0-9]"))),
      Rule("symbol", List(TerminalSymbol("(<|>|:|=| |\\||-|\n)"))), //TODO: complete this regex with all available symbols

      Rule("rule-name", List(NonTerminalSymbol("letter"))),
      Rule("rule-name", List(NonTerminalSymbol("rule-name"), NonTerminalSymbol("rule-char"))),

      Rule("literal-def", List(NonTerminalSymbol("character"))),
      Rule("literal-def", List(NonTerminalSymbol("literal-def"), NonTerminalSymbol("character"))),

      Rule("rule-char", List(NonTerminalSymbol("letter"))),
      Rule("rule-char", List(NonTerminalSymbol("digit"))),
      Rule("rule-char", List(TerminalSymbol("-"))),

      Rule("EOL", List(TerminalSymbol("\n")))
    )
  )

  private def Actions(node: Node, grammar: Grammar): Grammar = {
    //This method will be used to convert the BNF notation to a grammar
    if (node.value.rule == grammar.rules(0)) {
      val rule = createRule(node.children(0))
      Grammar(rule.name, List(rule))
    } else {
      Grammar("", List())
    }
  }

  def testSimpleGrammar: Unit = {
    val earley = Earley(grammar)
    val input = " <rule> ::= 'a'\n"
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head

    TreeUtils.createTree(end, input)

    val parseTree = new ParseTree().traversal(end, input)
    val out = Actions(parseTree, grammar)
  }

  def testGrammar: Unit = {
    val earley = Earley(grammar)
    val input = //I was running into issues using the scala multiline strings, but this seemed to work, so this is how it is
      " <syntax> ::= <rule> | <rule> <syntax>\n" +
        " <rule> ::= <whitespace> '<' <rule-name> '>' <whitespace> '::=' <whitespace> <expression> <line-end>\n" +
        " <whitespace> ::= <whitespace> ' ' | ' '\n" +
        " <expression> ::= <list> | <list> <whitespace> '|' <whitespace> <expression>\n" +
        " <line-end> ::= <EOL>\n" +
        " <list> ::= <term> | <term> <whitespace> <list>\n" +
        " <term> ::= <literal> | '<' <rule-name> '>'\n" +
        " <literal> ::= 'literal-def'\n" +
        " <character> ::= <letter> | <digit> | <symbol>\n" +
        " <letter> ::= 'A' | 'B' | 'C' | 'D' | 'E' | 'F' | 'G' | 'H' | 'I' | 'J' | 'K' | 'L' | 'M' | 'N' | 'O' | 'P' | 'Q' | 'R' | 'S' | 'T' | 'U' | 'V' | 'W' | 'X' | 'Y' | 'Z' | 'a' | 'b' | 'c' | 'd' | 'e' | 'f' | 'g' | 'h' | 'i' | 'j' | 'k' | 'l' | 'm' | 'n' | 'o' | 'p' | 'q' | 'r' | 's' | 't' | 'u' | 'v' | 'w' | 'x' | 'y' | 'z'\n" +
        " <digit> ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'\n" +
        " <symbol> ::= '<' | '>' | ':' | '=' | ' ' | '|' | '-'\n" +
        " <rule-name> ::= <letter> | <rule-name> <rule-char>\n" +
        " <rule-char> ::= <letter> | <digit> | '-'\n" +
        " <EOL> ::= '\n'\n"
    println(input)

    val chart = earley.buildChart(input)
//    println(chart.repr()) printing the chart is only useful to find out where parsing failed.

    val end = chart.getLastStates.head
//    TreeUtils.createLeaves(end, input)
    println()
//    TreeUtils.createTree(end, input)

    val parseTree = new ParseTree().traversal(end, input)
    val out = Actions(parseTree, grammar)
  }

}
