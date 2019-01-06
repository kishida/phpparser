package kis.phpparser.truffle;

import kis.phpparser.Main;
import org.graalvm.polyglot.Context;

/**
 *
 * @author naoki
 */
public class PHPMain {
    private static final String SCRIPT = ""
            + "function f($b) {"
            + "  return 4;"
            + "}"
            + "$a = 3;"
            + "echo f(3) - 3 < 5;"
            + "echo $a;";
    private static final String FIB = "function fib($n) {\n" +
            "    if ($n < 2) {\n" +
            "        return $n;\n" +
            "    }\n" +
            "    return fib($n - 1) + fib($n - 2);\n" +
            "}\n"
            + "fib(31);fib(31);fib(31);fib(31);fib(31);" +
            "$start = microtime();\n" +
            "echo \"fib:\".fib(31);\n" +
            "echo \"\\n\";\n" +
            "echo \"time:\".(microtime() - $start);\n";
    public static void main(String[] args) {
        Context ctx = Context.create("php");
        ctx.eval("php", FIB);
    }
}
