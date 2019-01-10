package kis.phpparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.jparsec.Parser;

/**
 *
 * @author naoki
 */
public class Loader {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Need PHP file");
            System.exit(1);
        }
        
        Parser<List<PHPParser.AST>> parser = PHPParser.createParser();
        List<PHPParser.AST> ast;
        try (BufferedReader bur = Files.newBufferedReader(Paths.get(args[0]))) {
            ast = parser.parse(bur);
        }
        PHPExecutor exec = new PHPExecutor();
        exec.visit(ast);
   }
}
