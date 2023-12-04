import java.util.*;

public class SymbolTable{

    public void printSymbols() {
        System.out.println("Global Scope Symbols:");
        globalScopeTable.forEach((key, symbol) -> System.out.println("Symbol: " + key + ", " + symbol)); // Modify as needed
        
        System.out.println("Local Scope Symbols:");
        localScopeTable.forEach((key, symbol) -> System.out.println("Symbol: " + key + ", " + symbol)); // Modify as needed
    }

    public Collection<Symbol> getAllSymbols() {
        // This method combines symbols from all scopes (global and local)
        // Adjust the implementation based on how you've structured your symbol table

        List<Symbol> allSymbols = new ArrayList<>();
        allSymbols.addAll(globalScopeTable.values());
        allSymbols.addAll(localScopeTable.values());
        return allSymbols;
    }

     
    public Stack<String> undoStack; //an undo stack will keep track of symbols when in scope and determine what elements we need to pop off when we exit a scope
    public Map<String, Symbol> globalScopeTable; //This will contain the elements in the global scope. The scopes will either be global or local
    //When looking up a symbol in the hashmap, we can determine if the symbol is in the local scope or the global scope
    public Map<String, Symbol> localScopeTable;
    public Map<String, Map<String, Symbol>> symbolTable; //a symbol table will map a scope to a symbol.

    public SymbolTable(){
        globalScopeTable = new HashMap<String, Symbol>();
        localScopeTable = new HashMap<String, Symbol>();
        symbolTable = new HashMap<String, Map<String, Symbol>>();
        symbolTable.put("global", globalScopeTable);
        symbolTable.put("local", localScopeTable);
        undoStack = new Stack<String>();

    }

    public Map<String, Boolean> functionCallStatus = new HashMap<>();


    public void insertToSymbolTable(String identifier, Symbol symbol, String scope) {

        Map<String, Symbol> currentScopeTable = (scope.equals("global")) ? globalScopeTable : localScopeTable;

        if (currentScopeTable.containsKey(identifier)) {
            throw new RuntimeException("Error: Identifier '" + identifier + "' is already declared in the " + scope + " scope.");
        } else {
            currentScopeTable.put(identifier, symbol);
        }
    }

    public void destroyScope(){
        localScopeTable.clear(); //Removes all of the mappings from the localScopeTable map
        
    }
    public void clearUndoStack(String specialCharacter){
        while(undoStack.pop() != specialCharacter){
            undoStack.pop();
        }
    }

    public Symbol getSymbol(String id, String scope){
        if(scope == "global"){
            return globalScopeTable.get(id);
        }
        else{
            return localScopeTable.get(id);
        }
    }

    public String getScope(String id){
        if(globalScopeTable.containsKey(id)){
            return "global";
        }
        else if(localScopeTable.containsKey(id)){
            return "local";
        }
        return "id not defined";
    }

    public String checkIfPresent(String id, String scope){
        //This method checks what scope the symbol is in and gives us a pointer to that scope
        if(scope == "local" && localScopeTable.containsKey(id)){
            return "local";
        }
        if(scope == "global" && globalScopeTable.containsKey(id)){
            return "global";
        }
        else{
            return "null";
        }
    }


    public static void main(String[] args) {
    }

}
