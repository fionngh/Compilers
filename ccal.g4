grammar ccal;

prog: decl_list function_list main ;

decl_list: decl SEMICOLON decl_list | ;

decl: var_decl | const_decl ;

var_decl: Var ID COLON type ;

const_decl: Const ID COLON type EQUAL expression ;

function_list: function function_list | ;

function: type ID LBR parameter_list RBR LBRACE decl_list statement_block Return LBR ( expression | ) RBR SEMICOLON RBRACE ;

type: Integer | Boolean | Void ;

parameter_list: nemp_parameter_list | ;

nemp_parameter_list: ID COLON type #ParameterList | ID COLON type COMMA nemp_parameter_list #ParameterListWithItems ;

main: Main LBRACE decl_list statement_block RBRACE ;

statement_block: statement statement_block | ;

statement: ID EQUAL expression SEMICOLON #ExprStatement | ID LBR arg_list RBR SEMICOLON #FunctionCallStatement | LBRACE statement_block RBRACE #BlockStatement | If condition LBRACE statement_block RBRACE Else LBRACE statement_block RBRACE #IfElseStatement | While condition LBRACE statement_block RBRACE  #WhileLoopStatement| Skipped SEMICOLON #SkipStatement ;

expression: fragments binary_arith_op fragments #ArithmeticExpression | LBR expression RBR #ParenExpression| ID LBR arg_list RBR #Arg_listStatement | fragments #SimpleExpression ;

fragments: ID #FragmentsID
         | MINUS ID #FragmentsMinusID
         | Int #FragmentsInt
         | True #FragmentsTrue
         | False #FragmentsFalse
         | #FragmentsNone ;

binary_arith_op: PLUS | MINUS ;

condition: NEGATION condition #NegCondition | LBR condition RBR #BrCondition| expression comp_op expression #BooleanExpression | condition ( OR | AND ) condition #BooleanExpression ;

comp_op: EQUALTO | NOTEQUAL | LESSTHAN | LESSEQUAL | GREATERTHAN | GREATEREQUAL ;

arg_list: nemp_arg_list | ;

nemp_arg_list: ID #SingleArg
            | ID COMMA nemp_arg_list #MultiArg
            ;


// Language is not case sensitive
fragment A: 'a'|'A';
fragment B:	'b'|'B';
fragment C:	'c'|'C';
fragment D:	'd'|'D';
fragment E:	'e'|'E';
fragment F:	'f'|'F';
fragment G:	'g'|'G';
fragment H:	'h'|'H';
fragment I:	'i'|'I';
fragment J:	'j'|'J';
fragment K:	'k'|'K';
fragment L:	'l'|'L';
fragment M:	'm'|'M';
fragment N:	'n'|'N';
fragment O:	'o'|'O';
fragment P:	'p'|'P';
fragment Q:	'q'|'Q';
fragment R:	'r'|'R';
fragment S:	's'|'S';
fragment T:	't'|'T';
fragment U:	'u'|'U';
fragment V:	'v'|'V';
fragment W:	'w'|'W';
fragment X:	'x'|'X';
fragment Y:	'y'|'Y';
fragment Z:	'z'|'Z';

fragment Number: [0-9];
fragment Letter: [a-zA-Z];
fragment UnderScore: '_';


// Reserved words in the language
Var:        V A R;
Const:      C O N S T;
Return:     R E T U R N;
Integer:    I N T E G E R;
Boolean:    B O O L E A N;
Void:       V O I D;
Main:       M A I N;
If:         I F;
Else:       E L S E;
True:       T R U E;
False:      F A L S E;
While:      W H I L E;
Skipped:       S K I P; // Cant have skip as a reserved word as it would cause a conflict between 'skip' as a keyword and 'skip' as an identifier


// Tokens in the language
COMMA: ',' ;
SEMICOLON: ';' ;
COLON: ':' ;
EQUAL: '=' ;
LBRACE: '{' ;
RBRACE: '}' ;
LBR: '(' ;
RBR: ')' ;
PLUS: '+' ;
MINUS: '-' ;
NEGATION: '~' ;
OR: '||' ;
AND: '&&' ;
EQUALTO: '==' ;
NOTEQUAL: '!=' ;
LESSTHAN: '<' ;
LESSEQUAL: '<=' ;
GREATERTHAN: '>' ;
GREATEREQUAL: '>=' ;


// Identifiers are represented by a string of letters, digits or underscore character (‘ ’) beginning with a letter or underscore character. Identifiers cannot be reserved words.
ID : (Letter | UnderScore) (Letter | Number | UnderScore)*;
WS:     [ \t\r\n]+ -> skip;
Int: MINUS Number Number* | Number Number*;


// Comments can appear between any two tokens.
// There are two forms of comment: one is delimited by /* and */ and can be nested; the other begins with // and is delimited by the end of line and this type of comments may not be nested.
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' ( BLOCK_COMMENT | . )*? '*/' -> skip ; 

// Alternate solution is to use channel(Hidden) which will separate the stream of tokens from the main stream and essentially ignore them