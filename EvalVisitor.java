import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import java.util.*;

public class EvalVisitor extends ccalBaseVisitor<String> {
    private SymbolTable symbolTable;
    String scope = "global";

    public EvalVisitor() {
        this.symbolTable = new SymbolTable();
    }

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    // Check 1: Is every identifier declared within scope before it is used?
    // Check 2: Is no identifier declared more than once in the same scope?
    // Every time a declaration node is visited call insertToSymbolTable to ensure the new identifier is not already declared in the current scope.
    @Override
    public String visitVar_decl(ccalParser.Var_declContext ctx) {
        String id = ctx.ID().getText();
        String type = ctx.type().getText();
        Symbol symbol = new Symbol(id, type, "var", "null", false);
        symbolTable.insertToSymbolTable(id, symbol, scope);
        return type;
    }

    @Override
    public String visitConst_decl(ccalParser.Const_declContext ctx) {
        String id = ctx.ID().getText();
        String type = ctx.type().getText();
        Symbol symbol = new Symbol(id, type, "var", "null", false);
        symbolTable.insertToSymbolTable(id, symbol, scope);
        return type;
    }

    @Override 
    public String visitParameterList(ccalParser.ParameterListContext ctx) {
        scope = "local";
        String id = ctx.ID().getText();
        String type = ctx.type().getText();
        Symbol symbol = new Symbol(id, type, "null", "null", false);
        symbolTable.insertToSymbolTable(id, symbol, scope);
        return type;
    }

    @Override 
    public String visitParameterListWithItems(ccalParser.ParameterListWithItemsContext ctx) {
        String id = ctx.ID().getText();
        String type = ctx.type().getText();
        Symbol symbol = new Symbol(id, type, "null", "null", false);
        symbolTable.insertToSymbolTable(id, symbol, scope);
        visit(ctx.nemp_parameter_list());
        return type;
    }

     // Check 4: Are the arguments of an arithmetic operator integer variables or integer constants?
    @Override
    public String visitArithmeticExpression(ccalParser.ArithmeticExpressionContext ctx) {
        String leftType = visit(ctx.fragments(0));
        String rightType = visit(ctx.fragments(1));
        if (!"INTEGER".equals(leftType) || !"INTEGER".equals(rightType)) {
            throw new RuntimeException("Arithmetic operations require integer operands.");
        }
        return "INTEGER";
    }

    // Check 1: Is every identifier declared within scope before its use?
    @Override 
    public String visitFragmentsID(ccalParser.FragmentsIDContext ctx) {
        String id = ctx.ID().getText();
        String idPointer = symbolTable.checkIfPresent(id, scope); 
        if (idPointer == null) {
            throw new RuntimeException("Identifier " + id + " is not declared.");
        }
        Symbol symbol = symbolTable.getSymbol(id, idPointer);
        String value = symbol.getValue(); 
        String type = symbol.getType();
        String valueAndType = value + "&" + type;
        if (ctx.getParent() instanceof ccalParser.ArithmeticExpressionContext && !"INTEGER".equals(type)) {
            throw new IllegalArgumentException("Identifier '" + id + "' used in an arithmetic expression must be of INTEGER type");
        }
        
        symbol.markAsRead();
        return valueAndType;
    }

    @Override 
    public String visitFragmentsMinusID(ccalParser.FragmentsMinusIDContext ctx) {

        String id = ctx.ID().getText();
        String idPointer = symbolTable.checkIfPresent(id, scope);

        if (idPointer == "global") {
            Symbol symbol = symbolTable.getSymbol(id, idPointer);
            if (symbol.type.equalsIgnoreCase("boolean")) {
                throw new IllegalArgumentException("Cannot negate a boolean value: " + id);
            }
            if (symbol.type.equalsIgnoreCase("integer")) {
                String value = symbolTable.getSymbol(id, idPointer).getValue();
                value = "-" + value;
                String type = symbolTable.getSymbol(id, idPointer).getType();
                String valueAndType = value + "&" + type;
                return valueAndType;
            }
        }   
        if (idPointer == "local") {
            Symbol symbol = symbolTable.getSymbol(id, idPointer);
            if (symbol.type.equalsIgnoreCase("boolean")) {
                throw new IllegalArgumentException("Cannot negate a boolean value: " + id);
            }
            if (symbol.type.equalsIgnoreCase("integer")) {
                String value = symbolTable.getSymbol(id, idPointer).getValue();
                value = "-" + value;
                String type = symbolTable.getSymbol(id, idPointer).getType();
                String valueAndType = value + "&" + type;
                return valueAndType;
            }  
        }
        else if (idPointer == null) {
            throw new IllegalArgumentException("Identifier '" + id + "' has not been declared in the current scope.");
        }
        return "";
    }

    @Override 
    public String visitFragmentsInt(ccalParser.FragmentsIntContext ctx) {
        String value = ctx.getText();
        String type = "INTEGER";
        String valueAndType = value + "&" + type;
        return valueAndType;
    }

    @Override
    public String visitFragmentsTrue(ccalParser.FragmentsTrueContext ctx) {
        return "BOOLEAN";
    }

    @Override
    public String visitFragmentsFalse(ccalParser.FragmentsFalseContext ctx) {
        return "BOOLEAN";
    }

    // Check 5: Are the arguments of a boolean operator boolean variables or boolean constants?
    @Override
    public String visitBooleanExpression(ccalParser.BooleanExpressionContext ctx) {
    String leftType = visit(ctx.expression(0)); // Visit left expr
    String rightType = visit(ctx.expression(1)); // Visit right expr

    // Check if both operands are of type BOOLEAN
    if (!"BOOLEAN".equals(leftType) || !"BOOLEAN".equals(rightType)) {
        throw new RuntimeException("Boolean operations require boolean operands.");
    }

    return "BOOLEAN";
    }


    private void Error(String error) {
        System.out.println(error);
        System.exit(1);
    }

    @Override 
    public String visitFunction(ccalParser.FunctionContext ctx) {
        String id = ctx.ID().getText();
        String type = ctx.type().getText();
        scope = "global";
        String IDPointer = symbolTable.checkIfPresent(id, scope);

        symbolTable.functionCallStatus.put(id, false);

        if ("global".equals(IDPointer)) {
            throw new IllegalStateException("Function '" + id + "' has already been defined.");
        }
        
        Symbol symbol = new Symbol(id, type, "null", "null", true);
        symbolTable.insertToSymbolTable(id, symbol, "global");

        visit(ctx.parameter_list());
        symbolTable.undoStack.push("specialCharacter");
        symbolTable.undoStack.push(id);

        scope = "local";
        visit(ctx.decl_list());
        visit(ctx.statement_block());
        String expressionResult = visit(ctx.children.get(10)); 
        symbolTable.destroyScope();
        symbolTable.clearUndoStack("specialCharacter");
        scope = "global";
        return type;
    }


    // Check 3: Is the left-hand side of an assignment a variable of the correct type?
    // Check 8: ensuring that every variable is both written to and read from
    @Override 
    public String visitExprStatement(ccalParser.ExprStatementContext ctx) {
        String id = ctx.ID().getText(); // left side

        String scopeOfId = symbolTable.checkIfPresent(id, scope);
        if ("null".equals(scopeOfId)) {
            throw new IllegalStateException("Identifier '" + id + "' has not been declared");
        }

        Symbol symbol = symbolTable.getSymbol(id, scopeOfId);
        String idType = symbol.getType();

        // Evaluate the expression on the right-hand side and get its type
        String exprType = visit(ctx.expression()).split("&")[0]; // Assuming this returns 'type&value'

        // Type Checking
        if (!idType.equalsIgnoreCase(exprType)) {
            throw new IllegalArgumentException("Type mismatch in assignment: '" + id + "' is of type '" + idType + "', but the assigned expression is of type '" + exprType + "'");
        }


        // If types match, update the value of the symbol
        String value = visit(ctx.expression()).split("&")[1];
        symbol.setValue(value);

        symbol.markAsWritten();

        return idType; // Return the type of the identifier
}


    @Override 
    public String visitFunctionCallStatement(ccalParser.FunctionCallStatementContext ctx) {
        visit(ctx.arg_list());
        return "";
    }

    @Override 
    public String visitSingleArg(ccalParser.SingleArgContext ctx) {
        String id = ctx.ID().getText();
        String IDPointer = symbolTable.checkIfPresent(id, scope);
        if (IDPointer == "local" || IDPointer == "global") {
            return id;
        }
        if (IDPointer == "null") {
            throw new IllegalStateException(id + " Has not been declared");
        }
        return "";
    }

    @Override 
    public String visitMultiArg(ccalParser.MultiArgContext ctx) {
        String id = ctx.ID().getText();
        String IDPointer = symbolTable.checkIfPresent(id, scope);
        if (IDPointer == "local" || IDPointer == "global") {
            return id;
        }
        if (IDPointer == "null") {
            throw new IllegalStateException(id + " Has not been declared");
        }
        String comma = visit(ctx.COMMA());
        if (comma == null) {
            throw new IllegalArgumentException("Missing COMMA between args");
        }

        visit(ctx.nemp_arg_list());
        return "";
    }

    // Check 9: Is every function called?
    @Override 
    public String visitMain(ccalParser.MainContext ctx) {
        scope = "local";
        symbolTable.undoStack.push("specialCharacter");
        visit(ctx.decl_list());
        visit(ctx.statement_block());
        symbolTable.destroyScope();
        symbolTable.clearUndoStack("specialCharacter");
        scope = "global";

        for (Symbol symbol : symbolTable.getAllSymbols()) {
        if (!symbol.isRead() || !symbol.isWritten()) {
            System.out.println("Warning: Variable '" + symbol.getId() + "' is not fully utilized (Read: " + symbol.isRead() + ", Written: " + symbol.isWritten() + ")");
        }
    }
        // Check for uncalled functions at the end of parsing the main program
        for (Map.Entry<String, Boolean> entry : symbolTable.functionCallStatus.entrySet()) {
            if (!entry.getValue()) {
                System.out.println("Warning: Function '" + entry.getKey() + "' is declared but never called.");
            }
    }
        return "";
}


    // Check 6: Ensuring that there is a function for every invoked identifier
    // Check 7: Ensuring that every function call has the correct number of arguments
    // Check 9: Is every function called?
    @Override 
    public String visitArg_listStatement(ccalParser.Arg_listStatementContext ctx) {
        String functionName = ctx.ID().getText();
        String IDPointer = symbolTable.checkIfPresent(functionName, scope);

        // Check if the function is declared
        if ("null".equals(IDPointer)) {
            throw new IllegalStateException(functionName + " has not been declared");
        }

        // Retrieve the function symbol and check if it's indeed a function
        Symbol functionSymbol = symbolTable.getSymbol(functionName, IDPointer);
        if (functionSymbol == null || !functionSymbol.isFunction()) {
            throw new IllegalArgumentException("'" + functionName + "' is not a function");
        }


        // Mark the function as called (true)
        if (symbolTable.functionCallStatus.containsKey(functionName)) {
            symbolTable.functionCallStatus.put(functionName, true);
        }

        // Count the number of arguments in the function call
        int numberOfArguments = ctx.arg_list().getChildCount();

        // Retrieve the number of parameters from the function symbol
        int numberOfParameters = functionSymbol.getNumberOfParameters();

        // Check if the number of arguments matches the number of parameters
        if (numberOfArguments != numberOfParameters) {
            throw new IllegalArgumentException("Function '" + functionName + "' called with incorrect number of arguments. Expected: " + numberOfParameters + ", Found: " + numberOfArguments);
        }

        return "";
    }

}