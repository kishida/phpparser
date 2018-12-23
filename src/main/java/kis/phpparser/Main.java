package kis.phpparser;

import java.util.List;
import org.jparsec.Parser;

public class Main {
    private final static String SAMPLE = 
            //"<?php\n" +
            "function fib($n) {\n" +
            "    if ($n < 2) {\n" +
            "        return $n;\n" +
            "    }\n" +
            "    return fib($n - 1) + fib($n - 2);\n" +
            "}\n" +
            "$start = microtime();\n" +
            "echo \"fib:\".fib(31);\n" +
            "echo \"\\n\";\n" +
            "echo \"time:\".(microtime() - $start);\n" +
            "echo \"\\n\";\n" +
            "";
    
    public static void main(String[] args) {
        PHPExecutor exec = new PHPExecutor();
        Parser<List<PHPParser.AST>> parser = PHPParser.script()
                .from(PHPParser.tokenizer, PHPParser.ignored);
        List<PHPParser.AST> script = parser.parse(SAMPLE);
        exec.visit(script);
    }
}
