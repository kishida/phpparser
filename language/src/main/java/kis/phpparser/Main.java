package kis.phpparser;

import java.util.List;
import kis.phpparser.truffle.PHPMain;
import org.jparsec.Parser;

public class Main {
    public final static String SAMPLE = 
            "<?php\n" +
            "function fib($n) {\n" +
            "    if ($n < 2) {\n" +
            "        return $n;\n" +
            "    }\n" +
            "    return fib($n - 1) + fib($n - 2);\n" +
            "}\n" +
            "fib(31);fib(31);fib(31);fib(31);fib(31);" +
            "$start = microtime();\n" +
            "echo \"fib:\".fib(31);\n" +
            "echo \"\\n\";\n" +
            "echo \"time:\".(microtime() - $start);\n" +
            "echo \"\\n\";\n" +
            "echo \"12\".\"3\" + \"4\".\"56\";" +
            "echo \"\\n\";\n" +
            "echo \"ab\".\"3\" + \"4\".\"cd\";" +
            "";
    
    public static void main(String[] args) {
        PHPExecutor exec = new PHPExecutor();
        Parser<List<PHPParser.AST>> parser = PHPParser.createParser();
        List<PHPParser.AST> script = parser.parse(SAMPLE);
        exec.visit(script);
    }
}
