package kis.phpparser;

import org.jparsec.Parser;

/**
 *
 * @author naoki
 */
public class Main {
    private static String SAMPLE = 
            //"        <?php\n" +
            "        function fib($n) {\n" +
            "            if ($n < 1) {\n" +
            "                return 0;\n" +
            "            }\n" +
            "            return fib($n - 1) + fib($n - 2);\n" +
            "        }\n" +
            "        echo fib(10);\n" +
            "    ";
    
    public static void main(String[] args) {
        var intParser= PHPParser.integer().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(intParser.parse("123"));
        
        var identifier = PHPParser.identifier().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(identifier.parse("abc"));
        
        var parser = PHPParser.variable().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(parser.parse("$hoge"));
        
        var valueParser = PHPParser.value().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(valueParser.parse("$hoge"));
        System.out.println(valueParser.parse("123"));
        
        var op = PHPParser.operator().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(op.parse("12+3"));
        System.out.println(op.parse("12+$a"));
        System.out.println(op.parse("$ab+123"));
        
        var comm = parser(PHPParser.command());
        System.out.println(comm.parse("echo 123"));
        System.out.println(comm.parse("echo 123<3"));
        System.out.println(comm.parse("echo 23+3"));
        System.out.println(comm.parse("echo $a"));
        System.out.println(comm.parse("return $abc"));
        System.out.println(comm.parse("echo fib(12)"));
        System.out.println(comm.parse("return fib($n - 1) + fib($n - 2)"));
        
        var ident = PHPParser.identifier().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(ident.parse("f"));
        
        var assignment = parser(PHPParser.assignment());
        System.out.println(assignment.parse("$a=123"));
        
        var func = PHPParser.funcCall().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(func.parse("f(12)"));

        var statement = parser(PHPParser.statement());
        System.out.println(statement.parse("$a;"));
        System.out.println(statement.parse("$a=$a+1;"));
        
        var ifs = parser(PHPParser.ifStatement());
        System.out.println(ifs.parse("if ($a < 3) echo $a;"));
        System.out.println(ifs.parse("if ($a < 3){ func();echo $a;}"));
        
        var fun = parser(PHPParser.function());
        System.out.println(fun.parse("function fib($a) { return $a + 1; }"));
        
        var script = parser(PHPParser.script());
        System.out.println(script.parse(SAMPLE));
    }
    static <T> Parser<T> parser(Parser<T> p) {
        return p.from(PHPParser.tokenizer, PHPParser.ignored);
    }
}
