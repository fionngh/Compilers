import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;
import java.io.FileInputStream;
import java.io.InputStream;


public class ccal
{
    public static void main (String [] args) throws Exception
    {
        try
        {
            String inputFile = null;
            if (args.length > 0)
                inputFile = args[0];
            InputStream IS = System.in;
            if (inputFile != null)
                IS = new FileInputStream(inputFile);
            else
                IS = System.in;

            ccalLexer lexer = new ccalLexer(CharStreams.fromStream(IS));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ccalParser parser = new ccalParser(tokens);
            ParseTree tree = parser.prog();

            EvalVisitor eval = new EvalVisitor();
            eval.visit(tree); // Assuming 'tree' is your parsed tree

        // Retrieve and print the symbol table
            SymbolTable symbolTable = eval.getSymbolTable();
            symbolTable.printSymbols();

            System.out.println(args[0] + " parsed successfully");
        }
        catch (Exception e)
        {
            System.err.println("Error parsing " + ((args.length > 0) ? args[0] : "input"));
            e.printStackTrace();
        }
}
}