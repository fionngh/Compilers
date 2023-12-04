import java.util.List;

public class Symbol {
    String id;
    String type;
    String declaration;
    String value;
    boolean function;
    private boolean isFunction;
    private List<String> parameterTypes;
    private boolean isRead = false;
    private boolean isWritten = false;

    public Symbol(String id, String type, String declr, String value, boolean function){
        this.id = id;
        this.type = type;
        this.declaration = declr;
        this.value = value;
        this.function = function; // Is the symbol a function or not
    }

    // Getter for id
    public String getId() {
        return id;
    }

    // Getter for type
    public String getType() {
        return type;
    }

    // Getter for declaration
    public String getDeclaration() {
        return declaration;
    }

    // Getter for value
    public String getValue() {
        return value;
    }

    // Setter for value
    public void setValue(String value) {
        this.value = value;
    }
    public String toString(){
        return "id: " + this.getId() + ", " + "type: " + this.getType() + ", " + "value: " + this.getValue() + ", " + "declaration: " + this.getDeclaration();
    }

    // Check 8: ensuring that every variable is both written to and read from
    // Method to mark as read
    public void markAsRead() {
        this.isRead = true;
    }

    // Method to mark as written
    public void markAsWritten() {
        this.isWritten = true;
    }

    // Checks if the symbol is read
    public boolean isRead() {
        return isRead;
    }

    // Checks if the symbol is written
    public boolean isWritten() {
        return isWritten;
    }

    // Methods for functions
    public boolean isFunction() {
        return isFunction;
    }

    public void setFunction(boolean isFunction) {
        this.isFunction = isFunction;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    // New method to get the number of parameters
    public int getNumberOfParameters() {
        return parameterTypes != null ? parameterTypes.size() : 0;
    }
}
