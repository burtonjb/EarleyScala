# Earley Parser
The Earley parser is an algorithm for parsing context-free languages. The algorithm is a chart parser.
If implemented correctly, it runs in O(n^3) time for the general case, but O(n^2) for unambiguous grammars
and O(n) time for all LR(k) grammars.

## Background
### Production rule
A production rule is a rule specifying a symbol substitution that can be performed recursively to generate a new set of symbols.
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

## Earley Recognizer
The recognizer is the first part of the algorithm and it's better understood (It was introduced in 1968 and hasn't changed too much, while the parser still had papers published about it in 2002)
For every input character, there is a state set, with each state having the following values
* production rule being matched (S -> aSa)
* current position processing that rule
* the origin position, where the matching of the production rule began

There is Earley's dot notation which is S -> a • S (i) with the dot displaying the current position processing the rule, and (i) being where the processing started

The state set S(0) is seeded with the top-level rule. Each state set at the input position k is called S(k).

The recognizer then repeatedly executes the 3 following operations:
* predict: for each state in S(k) of the form `X -> a • Y, (i)` add `Y -> • y, (k)` to S(k) for the every rule with Y on the LHS
* scan: if a is the next symbol in the input stream, for every state in S(k) for the form `X -> A • a B (i)` add `X -> A a • B (i)` to S(k+1)
* Complete: for every state in S(k) of the form `Y -> A • (i)`, find all states in S(i) of the form `X -> A • Y B (j)` and add `X -> A Y • B (j)` to S(k)

Duplicates aren't added to the state set.      

### Handling Ɛ productions 
Epsilon (Ɛ) productions are rules that can produce an empty string. They are usually represented as `S -> Ɛ`, or in my system as S -> List().
Handling epsilon productions is tricky as after predicting an epsilon production the epsilon rule/symbol should be immediatly completed.

One way to do this is to scan, and the repeatedly predict/complete until no new productions are created. This doesn't perform well, and is kind of ugly.
In "Practical Earley Parsing (Aycock & Horspool, 2002)", they proved that after predicting, if the next symbol is nullable, immediately complete that symbol (and repeat until a symbol is not nullable) 
This is a bit cleaner than the above solution and is fairly easy to implement. 
 
A nullable variable is a value A such that A is either an epsilon production or all rules in A are eventually epsilon productions. Note that terminal symbols are never nullable, and neither are rules that have all derivations with terminal symbols

Then:
* A -> Ɛ is trivially nullable
* S -> X is nullable if all symbols of X are nullable. 

Note that A -> a | Ɛ is nullable because there is one derivation of A that is nullable. 

## Earley Parser
The recognizer doesn't directly give a parse tree, but the sets produced by the recognizer can be used to construct a parser tree.
The technique is to create a top-down parser and walk through the completed Earley states, and also the scanned terminals

You can tell if there can be a successful parse if there is a rule in the final completed set states that has a start position of rule and the rule matches the initial rule.
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

## Sources
* https://en.wikipedia.org/wiki/Earley_parser
* http://loup-vaillant.fr/tutorials/earley-parsing/ 
* https://courses.engr.illinois.edu/cs421/sp2012/project/PracticalEarleyParsing.pdf
* https://www.sciencedirect.com/science/article/pii/S1571066108001497?via%3Dihub