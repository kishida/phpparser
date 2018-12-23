package kis.phpparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        String script;
        try (BufferedReader bur = Files.newBufferedReader(Paths.get(args[0]));
             Stream<String> stream = bur.lines())
        {
            script = stream.map(line -> "<?php".equals(line) ? "" : line)
                    .collect(Collectors.joining("\n"));
        }
        
        Parser<List<PHPParser.AST>> parser = PHPParser.script()
                .from(PHPParser.tokenizer, PHPParser.ignored);
        List<PHPParser.AST> ast = parser.parse(script);
        PHPExecutor exec = new PHPExecutor();
        exec.visit(ast);
   }
}
