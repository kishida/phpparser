package kis.phpparser;

import java.util.List;
import org.jparsec.Parser;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class PHPParserTest {
    private final static String SAMPLE = 
            //"        <?php\n" +
            "        function fib($n) {\n" +
            "            if ($n < 2) {\n" +
            "                return $n;\n" +
            "            }\n" +
            "            return fib($n - 1) + fib($n - 2);\n" +
            "        }\n" +
            "        echo fib(10);\n" +
            "    ";
    
    public PHPParserTest() {
    }

    @Test
    public void testScript() {
        Parser<List<PHPParser.AST>> parser = parser(PHPParser.script());
        List<PHPParser.AST> asts = parser.parse(SAMPLE);
        assertThat(asts.size(), is(2));
    }
    
    public void test() {
        Parser<PHPParser.IntValue> intParser= PHPParser.integer().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(intParser.parse("123"));
        
        Parser<String> identifier = PHPParser.identifier().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(identifier.parse("abc"));
        
        Parser<PHPParser.ASTVariable> parser = PHPParser.variable().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(parser.parse("$hoge"));
        
        Parser<PHPParser.ASTExp> valueParser = PHPParser.value().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(valueParser.parse("$hoge"));
        System.out.println(valueParser.parse("123"));
        
        Parser<PHPParser.ASTExp> op = PHPParser.operator().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(op.parse("12+3"));
        System.out.println(op.parse("12+$a"));
        System.out.println(op.parse("$ab+123"));
        
        Parser<PHPParser.ASTCommand> comm = parser(PHPParser.command());
        System.out.println(comm.parse("echo 123"));
        System.out.println(comm.parse("echo 123<3"));
        System.out.println(comm.parse("echo 23+3"));
        System.out.println(comm.parse("echo $a"));
        System.out.println(comm.parse("return $abc"));
        System.out.println(comm.parse("echo fib(12)"));
        System.out.println(comm.parse("return fib($n - 1) + fib($n - 2)"));
        
        Parser<String> ident = PHPParser.identifier().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(ident.parse("f"));
        
        Parser<PHPParser.ASTAssignment> assignment = parser(PHPParser.assignment());
        System.out.println(assignment.parse("$a=123"));
        
        Parser<PHPParser.ASTFuncCall> func = PHPParser.funcCall().from(PHPParser.tokenizer, PHPParser.ignored);
        System.out.println(func.parse("f(12)"));

        Parser<PHPParser.AST> statement = parser(PHPParser.statement());
        System.out.println(statement.parse("$a;"));
        System.out.println(statement.parse("$a=$a+1;"));
        
        Parser<PHPParser.ASTIf> ifs = parser(PHPParser.ifStatement());
        System.out.println(ifs.parse("if ($a < 3) echo $a;"));
        System.out.println(ifs.parse("if ($a < 3){ func();echo $a;}"));
        
        Parser<PHPParser.ASTFunction> fun = parser(PHPParser.function());
        System.out.println(fun.parse("function fib($a) { return $a + 1; }"));
        
        Parser<List<PHPParser.AST>> scriptParser = parser(PHPParser.script());
        final List<PHPParser.AST> script = scriptParser.parse(SAMPLE);
        System.out.println(script);        
    }
    
    <T> Parser<T> parser(Parser<T> p) {
        return p.from(PHPParser.tokenizer, PHPParser.ignored);
    }

}
