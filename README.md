# Earley Parser
The Earley parser is an algorithm for parsing context-free languages. The algorithm is a chart parser.
If implemented correctly, it runs in O(n^3) time for the general case, O(n^2) for unambiguous grammars
and O(n) time for all LR(k) grammars. 

Its named after its original inventor - Jay Earley (and is not a typo on early). 

The strengths of this algorithm is that it can parse all context-free grammars, unlike other parsing algorithms. For example, compilers typically uses LR and LL parsing algorithms, which are performant, but they only work on a subset of context-free grammars/languages.  

## Background

### Production rule
A production rule is a rule specifying a symbol substitution that can be performed recursively to generate a new set of symbols. There can be two kinds of symbols - a terminal symbol, which is a final rule -> character replacement or a non-terminal symbol which replaces a rule with at least 1 other rule (and possibly more rules and/or characters. The rule can even be replaced with itself!). 
They are simple replacements, for example (using the notation that CAPS are non-terminal symbols and lower case are terminal symbols)
<pre>
A -> a
A -> b
means that A can be replaced with 'a' or 'b'
</pre>

A more complicated grammar could be
<pre>
S -> aSa
S -> bSb
S -> c
will match any of the below strings (basically any palidrome of a's and b's) 
bbcbb
aacaa
aca
abacaba
</pre>

### Context free grammar
A context free grammar (CFG) is a type of formal grammar. It is a set of production rules that can by used to describe
all possible strings in a Context free language. 
A context free grammar has a start symbol, a set of non-terminal production rules and a set of terminal characters (sometimes represented using the above notations)
Most modern programming languages are context-free as this makes it simple to write the parser (though C and C++ are context sensitive)

The above production rules can also be (very simple) context-free grammars, if there was a start symbol defined. 

e.g. 
<pre>
start = S
S -> aSa
S -> bSb
S -> c
</pre>

## Earley Recognizer
The recognizer is the first part of the algorithm and it's better understood (It was introduced in 1968 and hasn't changed too much, while the parser still had papers published about it in 2002)

For every input character, there is a state set, with each state in the set having the following values:
* production rule being matched (S -> aSa)
* current position processing that rule
* the origin position, where the matching of the production rule began.

The states basically represent: the corresponding production rule, how much of the rule is matched. 
The state set is pretty similar to a non-deterministic finite state machine, with all the production rules/early states being matched in parallel.

There is Earley's dot notation which is S -> a • S (i) with the dot displaying the current position processing the rule, and (i) being where the processing started

The state set S(0) is seeded with the top-level rule. Each state set at the input position k is called S(k).

The recognizer then repeatedly executes the 3 following operations:
* predict: for each state in S(k) of the form `X -> a • Y, (i)` add `Y -> • y, (k)` to S(k) for the every rule with Y on the LHS. This is preparing the state set for any scans or completes that can happen for when the next character is consumed. 
* scan: if a is the next symbol in the input stream, for every state in S(k) for the form `X -> A • a B (i)` add `X -> A a • B (i)` to S(k+1). This is advancing any of the Early States that match a terminal symbol to the next symbol in that state. 
* complete: for every state in S(k) of the form `Y -> A • (i)`, find all states in S(i) of the form `X -> A • Y B (j)` and add `X -> A Y • B (j)` to S(k). This is advancing any of the Earley States that refer to a non-terminal symbol that has just been completely matched. 

Duplicates aren't added to the state set.      

### Handling Ɛ productions 
Epsilon (Ɛ) productions are rules that can produce an empty string. They are usually represented as `S -> Ɛ`, or in my system as S -> List().
Handling epsilon productions is tricky as after predicting an epsilon production the epsilon rule/symbol should be immediately completed.

One way to do this is to scan, and the repeatedly predict/complete until no new productions are created. This doesn't perform well, and is kind of ugly.

In "Practical Earley Parsing (Aycock & Horspool, 2002)", they proved that after predicting, if the next symbol is nullable (defined below), immediately complete that symbol (and repeat until a symbol is not nullable) 
This is a bit cleaner than the above solution and is fairly easy to implement. 
 
A nullable variable is a value A such that A is either an epsilon production or all rules in A are eventually epsilon productions. Note that terminal symbols are never nullable, and neither are rules that have all derivations with terminal symbols.

Then:
* A -> Ɛ is trivially nullable
* S -> X is nullable if all symbols of X are nullable. So if X -> ABC, and only A is nullable - X is not nullable, but if B and C are nullable then X is nullable. 

Note that A -> a | Ɛ is nullable because there is one derivation of A that is nullable. 

## Earley Parser
The recognizer doesn't directly give a parse tree, but the sets produced by the recognizer can be used to construct a parse tree.
The technique is to create a top-down parser and walk through the completed Earley states and the scanned terminals.

You can tell if there can be a successful parse if there is a rule in the final set states that: is complete (the dot is at the end of the rule), is in S(n), where n is the length of the string, and has a start position of 0 (the start of the string) and the rule matches the grammar's starting rule.

Then because there is a complete state in the final state set, there must be completed states in earlier set states, but the parser must find out where they exist.

The technique I used to construct the parse tree is from "SPPF-Style Parsing From Earley Recognisers (Elizabeth Scott, 2008)" using the technique described in section 4.
Its actually pretty simple - 
* For a scan add a predecessor pointer from the post scan point to the prescan point (there is an error in the paper where Scott incorrectly has the from/to reversed)
* For a complete add a reduction pointer from the new states generated from complete to the completed state and if the symbol is not and epsilon a predecessor pointer from each old state to the new state 

Note that predicting a nullable symbol will have the nullable symbol be immediately completed

The parse algorithm will then just walk down the tree of pointers. 

### Ambiguous grammars
This algorithm will be able to handle ambiguous grammars and can return the parse forest. 

There is another algorithm to disambiguate the parse forest, returning a single parse tree. To do this the algorithm picks one of the reduction/predecessor or reduction pairs. Though I wouldn't really trust this algorithm. It seemed to work for my test cases, but I never tried to prove its correctness.

## Improvements
### Handling scanned/lexed/tokenized input
I believe that the input text could be pre-processed by a tokenizer, and then the TerminalSymbol class could be subclassed to match these tokens.
The input text would then be converted to a list of input tokens. Doing this should simplify the grammar and make the algorithm perform better, since the worst case runtime is O(n^3), where n is the input length. 
This wouldn't improve the asymptotic complexity though

### Shared packed parse forest construction
Elizabeth Scott in section 5 describes how to convert the parse tree into a binarized parse tree. This would cap the parse runtime from unbounded runtime to O(n^3)

More details about the SPPF can be found here: http://www.bramvandersanden.com/post/2014/06/shared-packed-parse-forest/ 

# Example grammars and parses

## Simple palindrome grammar
The first example grammar is a simple odd-palindrome grammar - it will accept any input string with an odd number of 'a's and 'b's added that make a palindrome

<pre>
Grammar - Start: S
S -> aSa
S -> a
S -> bSb
S -> b
</pre>

Since this is the first example, I'll run through an explanation of the grammar using common (but not necessarily exact) notation. 

The character "S" on the left is the rule name. The capital S on the right hand side means refers to the rule S (a non-terminal symbol), and the lower case 'a's and 'b's refer to terminal symbols. The trivial cases for "S" are matching a single 'a' or 'b'. Since S also refers to itself, it can also recursively match itself.

it can match the following:
<pre>
a - base case
b - base case
aaa - simple 1-time replacement of S with a (replace S = a, then match S = aSa)
aba - simple 1-time replacement of S with b (replace S = b, then match S = aSa)
baaab - worked below
</pre>

With the input of 'baaab' below is the following Earley chart, with my annotations prefixed with //:
<pre>
// initially there is no matches
    ---- 0 ---
    S ->  •  'a'  S  'a' 		(0)
    S ->  •  'a' 		        (0)
    S ->  •  'b'  S  'b' 		(0)
    S ->  •  'b' 		        (0)
// the character 'b' is matched, scanning both the S = bSb and S = b rules into the S(1) stateset

    ---- 1 ---
    S ->  'b'  •  S  'b' 		(0)
    S ->  'b'  • 		        (0)
    S ->  •  'a'  S  'a' 		(1)
    S ->  •  'a' 		        (1)
    S ->  •  'b'  S  'b' 		(1)
    S ->  •  'b' 		        (1)
// the character 'a' is matched, scanning both S = aSa and S = a into S(2) stateset. Additionally S = bSb completes, adding the completed S = bSb state into S(2) 

    ---- 2 ---
    S ->  'a'  •  S  'a' 		(1)
    S ->  'a'  • 		        (1)
    S ->  •  'a'  S  'a' 		(2)
    S ->  •  'a' 		        (2)
    S ->  •  'b'  S  'b' 		(2)
    S ->  •  'b' 		        (2)
    S ->  'b'  S  •  'b' 		(0)
// more states of S = a, S = aSa complete, advancing the respective states. 

    ---- 3 ---
    S ->  'a'  •  S  'a' 		(2)
    S ->  'a'  • 		        (2)
    S ->  •  'a'  S  'a' 		(3)
    S ->  •  'a' 		        (3)
    S ->  •  'b'  S  'b' 		(3)
    S ->  •  'b' 		        (3)
    S ->  'a'  S  •  'a' 		(1)
// again, more states of S = a, S = aSa complete, advancing the respective states. 

    ---- 4 ---
    S ->  'a'  •  S  'a' 		(3)
    S ->  'a'  • 		        (3)
    S ->  'a'  S  'a'  • 		(1)
    S ->  •  'a'  S  'a' 		(4)
    S ->  •  'a' 		        (4)
    S ->  •  'b'  S  'b' 		(4)
    S ->  •  'b' 		        (4)
    S ->  'a'  S  •  'a' 		(2)
    S ->  'b'  S  •  'b' 		(0)

    ---- 5 ---
    S ->  'b'  •  S  'b' 		(4)
    S ->  'b'  • 		        (4)
    S ->  'b'  S  'b'  • 		(0)
    S ->  •  'a'  S  'a' 		(5)
    S ->  •  'a' 		        (5)
    S ->  •  'b'  S  'b' 		(5)
    S ->  •  'b' 		        (5)
    S ->  'a'  S  •  'a' 		(3)
// we've finally hit the end of the input string. There is one rule that matches the acceptance rules, the [zero indexed, 2]nd state. The rule name S matches the intial rule, it starts at position 0, and the dot is at the end of the rule; so this means that there was a successful parse.
</pre> 

Creating the parse tree from only the completed states, it looks like (with horizontal indentiation representing child states (which is very hard to read,  but I didn't want to spend too long formatting trees in text)):
<pre>
    S -> 'b' S 'b'
      b
      S -> 'a' S 'a'
        a
        S -> 'a'
          a
        a
      b
</pre>

More basic test cases can be found [here](./lib/src/test/scala/EarleyScala/testBasicGrammars.scala) (note that the parse tree notation I'm using is extremely weird, you read it bottom to top due to how I do the in-order traversal and the fact that I'm building the tree from the last matched character to the first character. It takes a lot of getting used to)

## Demonstration of LR and LL grammar parsing
The next example can be found [here](./lib/src/test/scala/EarleyScala/testBasicNumericGrammar.scala). This case defines a grammar that can be used to parse a 0 or a positive integer. 

There are two ways to parse a number, left recursive or right recursive. 

The left-recursive (LR) grammar is (where \d is the regex to match a digit character):
```
N -> N '\d'
N -> '\d'
```

This is called "left recursive" because the recursion is on the left side of the definition. 

There is an alternative right-recursive (RR) grammar to match a number. Its:
```
N -> '\d' N
N -> '\d'
```

Both are fine grammars to parse a number, but Earley parsers perform much better on LR grammars. Other algorithms can only handle either LR or RR grammars. You can see this in the Earley charts and how the LR grammar's chart is much smaller than the RR chart

For parsing the string "123" see the below charts. 

```
LR
    ---- 0 ---
    number ->  •  number  '[0-9]' 		(0)
    number ->  •  '[0-9]' 		(0)

    ---- 1 ---
    number ->  '[0-9]'  • 		(0)
    number ->  number  •  '[0-9]' 		(0)

    ---- 2 ---
    number ->  number  '[0-9]'  • 		(0)
    number ->  number  •  '[0-9]' 		(0)

    ---- 3 ---
    number ->  number  '[0-9]'  • 		(0)
    number ->  number  •  '[0-9]' 		(0)
```

```
RR
    ---- 0 ---
    number ->  •  '[0-9]'  number 		(0)
    number ->  •  '[0-9]' 		(0)

    ---- 1 ---
    number ->  '[0-9]'  •  number 		(0)
    number ->  '[0-9]'  • 		(0)
    number ->  •  '[0-9]'  number 		(1)
    number ->  •  '[0-9]' 		(1)

    ---- 2 ---
    number ->  '[0-9]'  •  number 		(1)
    number ->  '[0-9]'  • 		(1)
    number ->  •  '[0-9]'  number 		(2)
    number ->  •  '[0-9]' 		(2)
    number ->  '[0-9]'  number  • 		(0)

    ---- 3 ---
    number ->  '[0-9]'  •  number 		(2)
    number ->  '[0-9]'  • 		(2)
    number ->  •  '[0-9]'  number 		(3)
    number ->  •  '[0-9]' 		(3)
    number ->  '[0-9]'  number  • 		(1)
    number ->  '[0-9]'  number  • 		(0)
```

LR is much smaller than RR, in fact it seems like the LR chart is ~O(n) in size, while for the RR chart each state set seems to be ~O(n), so the total chart size is ~O(n^2). 

Additionally I've included parsing actions that will take the matched rule and convert it to an output. Its a horrible mess of recursive code, but it seems to work. The LR actions are much simpler and nicer to handle than the RR actions, which require walking down the parse tree first to get the max depth before being able to parse the string to a number (or pass in a max depth). Anyway, I thought this was a cool finding more than anything else, but it also gives some insight into multiple ways to write grammars.  

## Numeric parsing and actions
The next example is the [arithmetic grammar](./lib/src/test/scala/EarleyScala/testArithmeticGrammar.scala), in particular test cases 3 and 4.

The grammar for parsing an arithemtic expression (for only integers) is:

```
Start rule = sum
sum -> sum '[+-]' product
sum -> product
product -> product '[/*]' factor
product -> factor
factor -> '(' sum ')'
factor -> number
number -> number '[0-9]'
number -> '[0-9]'
```



## BNF parsing

## Sources
* https://en.wikipedia.org/wiki/Earley_parser
* http://loup-vaillant.fr/tutorials/earley-parsing/ 
* https://courses.engr.illinois.edu/cs421/sp2012/project/PracticalEarleyParsing.pdf
* https://www.sciencedirect.com/science/article/pii/S1571066108001497?via%3Dihub