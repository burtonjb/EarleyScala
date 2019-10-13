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
TODO!    

## Earley Parser
The recognizer doesn't directly give a parse tree, but the sets produced by the recognizer can be used to construct a parser tree.
The technique is to create a top-down parser and walk through the completed Earley states.

You can tell if there can be a successful parse if there is a rule in the final completed set states that has a start position of rule and the rule matches the initial rule.
Then because there is a complete state in the final state set, there must be completed states in earlier set states, but the parser must find out where they exist.

For example, if you have 6 set states (indexing from 0) and you have: 
`A -> A B C • (5)` as the final state.
A, B must be completed in earlier set states, and C must be completed in this set state.
Using the start position of C, you can search the corresponding set state for the completion of B, and then repeat for A. (This is like a breadth first search)
This will give the first level of the parse tree. You would then repeat this process for each NonTerminal symbol child of the current node.
If its a terminal symbol, then just take the character at the current input position

For example (using lower case characters to represent terminal character):
<pre>
A->BAC | a
B->b
C->c
</pre>

Below is an example of constructing the parse tree, with the above grammar, and with the input `bbacc`. Values in parens are the end of the matches for the completed states
<pre>
    A->BAC•(5)
B(?)   A(?)     C(5)
C is just a terminal rule, so just subtract 1 from the search index 

    A->BAC•(5)
B(1)    A(4)    C(5)
A is a nonterminal rule and starts at 1, so B must be in state 1
        
A(4) then needs to be determined, repeating the above process, except with A(3) as the root node.
B(1) and C(5) are terminal rules, so they can just use the symbol. The fully constructed parse tree would look like:

A   ->B ->  b    
    ->A ->B ->b
        ->A ->a
        ->C ->c
    ->C ->  c 
</pre>

### Ambiguous grammars
This algorithm will work for ambiguous grammars, but will only return one of the possible parse trees from the possible parses.

## Improvements

## Sources
* https://en.wikipedia.org/wiki/Earley_parser
* http://loup-vaillant.fr/tutorials/earley-parsing/ 
* https://dickgrune.com/Books/PTAPG_1st_Edition/
* https://courses.engr.illinois.edu/cs421/sp2012/project/PracticalEarleyParsing.pdf
* https://www.sciencedirect.com/science/article/pii/S1571066108001497?via%3Dihub