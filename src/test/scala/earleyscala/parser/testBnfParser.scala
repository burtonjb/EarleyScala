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
      Rule("expression", List(NonTerminalSymbol("expression"), NonTerminalSymbol("whitespace"), TerminalSymbol("\\|"), NonTerminalSymbol("whitespace"), NonTerminalSymbol("list"))),

      Rule("line-end", List(NonTerminalSymbol("EOL"))),

      Rule("list", List(NonTerminalSymbol("term"))),
      Rule("list", List(NonTerminalSymbol("list"), NonTerminalSymbol("whitespace"), NonTerminalSymbol("term"))),

      Rule("term", List(NonTerminalSymbol("literal"))),
      Rule("term", List(TerminalSymbol("<"), NonTerminalSymbol("rule-name"), TerminalSymbol(">"))),

      Rule("literal", List(TerminalSymbol("'"), NonTerminalSymbol("literal-def"), TerminalSymbol("'"))),

      Rule("character", List(NonTerminalSymbol("letter"))),
      Rule("character", List(NonTerminalSymbol("digit"))),
      Rule("character", List(NonTerminalSymbol("symbol"))),

      Rule("letter", List(TerminalSymbol("[A-Za-z]"))),
      Rule("digit", List(TerminalSymbol("[0-9]"))),
      Rule("symbol", List(TerminalSymbol("(<|>|:|=| |\\||-|\n)"))), //TODO: complete this regex with all available symbols

      Rule("rule-name", List(NonTerminalSymbol("rule-char"))),
      Rule("rule-name", List(NonTerminalSymbol("rule-name"), NonTerminalSymbol("rule-char"))),

      Rule("literal-def", List(NonTerminalSymbol("character"))),
      Rule("literal-def", List(NonTerminalSymbol("literal-def"), NonTerminalSymbol("character"))),

      Rule("rule-char", List(NonTerminalSymbol("letter"))),
      Rule("rule-char", List(NonTerminalSymbol("digit"))),
      Rule("rule-char", List(NonTerminalSymbol("dash"))),

      Rule("dash", List(TerminalSymbol("-"))), //grammar hack so that I don't have to rewrite parseString in the action function

      Rule("EOL", List(TerminalSymbol("\n")))
    )
  )

  private def Actions(root: Node, grammar: Grammar): Grammar = {
    def parseRules(root: Node): List[Rule] = {
      if (root.value.rule == grammar.rules(0)) parseRule(root.children(0))
      else parseRules(root.children(0)).concat(parseRule(root.children(1)))
    }

    def parseRule(node: Node): List[Rule] = {
      val ruleName = parseString(node.children(2))
      val symbols = parseExpression(node.children(9))
      symbols.map(s => Rule(ruleName, s))
    }

    def parseString(node: Node): String = {
      if (node.children.length > 1) parseString(node.children(0)) + node.children(1).children(0).children(0).repr
      else node.children(0).children(0).children(0).repr
    }

    def parseExpression(node: Node): List[List[Symbol]] = {
      if (node.children.size == 1) List(parseList(node.children(0)))
      else parseExpression(node.children(0)).appended(parseList(node.children(4)))
    }

    def parseList(node: Node): List[Symbol] = {
      if (node.value.rule == grammar.rules(8)) parseTerm(node.children(0))
      else parseList(node.children(0)).concat(parseTerm(node.children(2)))
    }

    def parseTerm(node: Node): List[Symbol] = {
      if (node.value.rule == grammar.rules(10)) parseString(node.children(0).children(1)).toList.map(c => TerminalSymbol(c.toString))
      else List(NonTerminalSymbol(parseString(node.children(1))))
    }

    val rules = parseRules(root)
    Grammar(rules.head.name, rules)

  }

  def testSimpleTerminalGrammar: Unit = {
    val earley = Earley(grammar)
    val input = " <rule> ::= 'rule'\n"
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head

    val parseTree = new ParseTree().traversal(end, input)
    val out = Actions(parseTree, grammar)
    println(out.repr)
    /*
    start: rule
    rule -> 'rule'
    */
  }

  def testSimpleNonTerminalGrammar: Unit = {
    val earley = Earley(grammar)
    val input = " <rule> ::= <rule>\n"
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head

    val parseTree = new ParseTree().traversal(end, input)
    val out = Actions(parseTree, grammar)
    println(out.repr)
    /*
    start: rule
    rule -> rule
    */
  }

  def testOrRuleGrammar: Unit = {
    val earley = Earley(grammar)
    val input = " <rule> ::= <rule> | 'rule'\n"
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head

    val parseTree = new ParseTree().traversal(end, input)
    val out = Actions(parseTree, grammar)
    println(out.repr)
    /*
    start: rule
    rule -> rule
    rule -> 'rule'
    */
  }

  def testMultiRuleGrammar: Unit = {
    val earley = Earley(grammar)
    val input = " <S> ::= <A> 'bc' | '0'\n" +
      " <A> ::= 'a'\n"
    val chart = earley.buildChart(input)
    val end = chart.getLastStates.head

    val parseTree = new ParseTree().traversal(end, input)
    val out = Actions(parseTree, grammar)
    println(out.repr)
    /*
    start: S
    S -> A 'b' 'c'
    S -> '0'
    A -> 'a'
    */

    val earley2 = Earley(out)
    val chart2 = earley2.buildChart("abc")
    val chart3 = earley2.buildChart("0")

    TreeUtils.createLeaves(chart2.getLastStates.head, "abc")
    println()
    TreeUtils.createLeaves(chart3.getLastStates.head, "0")
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

    val chart = earley.buildChart(input)

    val end = chart.getLastStates.head

    val parseTree = new ParseTree().traversal(end, input)
    val out = Actions(parseTree, grammar)
    println(out.repr)
    /*
    start: syntax
    syntax -> rule
    syntax -> rule syntax
    rule -> whitespace '<' rule-name '>' whitespace '::=' whitespace expression line-end
    whitespace -> whitespace ' '
    whitespace -> ' '
    expression -> list
    expression -> list whitespace '|' whitespace expression
    line-end -> EOL
    list -> term
    list -> term whitespace list
    term -> literal
    term -> '<' rule-name '>'
    literal -> 'literal-def'
    character -> letter
    character -> digit
    character -> symbol
    letter -> 'A'
    letter -> 'B'
    letter -> 'C'
    letter -> 'D'
    letter -> 'E'
    letter -> 'F'
    letter -> 'G'
    letter -> 'H'
    letter -> 'I'
    letter -> 'J'
    letter -> 'K'
    letter -> 'L'
    letter -> 'M'
    letter -> 'N'
    letter -> 'O'
    letter -> 'P'
    letter -> 'Q'
    letter -> 'R'
    letter -> 'S'
    letter -> 'T'
    letter -> 'U'
    letter -> 'V'
    letter -> 'W'
    letter -> 'X'
    letter -> 'Y'
    letter -> 'Z'
    letter -> 'a'
    letter -> 'b'
    letter -> 'c'
    letter -> 'd'
    letter -> 'e'
    letter -> 'f'
    letter -> 'g'
    letter -> 'h'
    letter -> 'i'
    letter -> 'j'
    letter -> 'k'
    letter -> 'l'
    letter -> 'm'
    letter -> 'n'
    letter -> 'o'
    letter -> 'p'
    letter -> 'q'
    letter -> 'r'
    letter -> 's'
    letter -> 't'
    letter -> 'u'
    letter -> 'v'
    letter -> 'w'
    letter -> 'x'
    letter -> 'y'
    letter -> 'z'
    digit -> '0'
    digit -> '1'
    digit -> '2'
    digit -> '3'
    digit -> '4'
    digit -> '5'
    digit -> '6'
    digit -> '7'
    digit -> '8'
    digit -> '9'
    symbol -> '<'
    symbol -> '>'
    symbol -> ':'
    symbol -> '='
    symbol -> ' '
    symbol -> '|'
    symbol -> '-'
    rule-name -> letter
    rule-name -> rule-name rule-char
    rule-char -> letter
    rule-char -> digit
    rule-char -> '-'
    EOL -> '
    '
  */
  }

}
