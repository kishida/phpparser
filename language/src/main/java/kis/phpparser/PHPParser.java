package kis.phpparser;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import org.jparsec.*;
import org.jparsec.pattern.CharPredicates;
import org.jparsec.pattern.Pattern;
import org.jparsec.pattern.Patterns;

/**
 *
 * @author naoki
 */
public class PHPParser {
    static String[] operators = {
            "<?php", "<", ">", "+", "-", "(", ")", ";", "=", ",", "{", "}", "=","."};
    static String[] keywords = {
            "function", "return", "echo", "if"};
    static Terminals terms = Terminals.operators(operators).words(Scanners.IDENTIFIER).keywords(keywords).build();
    static Parser<Void> ignored = Scanners.WHITESPACES.optional();
    static Pattern varToken = Patterns.isChar('$').next(Patterns.isChar(CharPredicates.IS_ALPHA).many1());

    static Parser<String> varParser = varToken.toScanner("variable").source();

    enum PHPTag {
        VARIABLE;
    }
    
    public static final Parser<Tokens.Fragment> VAR_TOKENIZER =
        varParser.map(text -> Tokens.fragment(text, PHPTag.VARIABLE));    

    public static final Parser<String> VAR_PARSER = Parsers.token(t -> {
        Object val = t.value();
        if (val instanceof Tokens.Fragment) {
            Tokens.Fragment c = (Tokens.Fragment) val;
            return PHPTag.VARIABLE.equals(c.tag()) ? c.text() : null;
        }
        return null;
    });

    static Parser<?> tokenizer = Parsers.or(
            terms.tokenizer(),
            Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
            VAR_TOKENIZER,
            Terminals.IntegerLiteral.TOKENIZER,
            Terminals.Identifier.TOKENIZER);

    public interface AST{}
    public interface ASTExp extends AST{}

    @Value
    public static class IntValue implements ASTExp {
        double value;
    }

    public static Parser<IntValue> integer() {
        return Terminals.IntegerLiteral.PARSER.map(s -> new IntValue(Double.parseDouble(s)));
    }

    @Value
    public static class StringValue implements ASTExp {
        String value;
    }
    
    public static Parser<StringValue> string() {
        return Terminals.StringLiteral.PARSER.map(StringValue::new);
    }
    
    @Value
    public static class ASTVariable implements ASTExp {
        String name;
    }

    public static Parser<ASTVariable> variable() {
        return VAR_PARSER.map(var -> new ASTVariable(var.substring(1)));
    }

    public static Parser<ASTExp> value() {
        return Parsers.or(integer(), assignment(), variable(), funcCall(), string(),
                terms.token("(").next(pr -> expression().followedBy(terms.token(")"))));
    }

    @Value
    public static class ASTBinaryOp implements ASTExp {
        ASTExp left;
        ASTExp right;
        String op;
    }

    public static Parser<ASTExp> operator() {
        return new OperatorTable<ASTExp>()
                .infixl(terms.token(".").retn((l, r) -> new ASTBinaryOp(l, r, ".")), 10)
                .infixl(terms.token("+").retn((l, r) -> new ASTBinaryOp(l, r, "+")), 10)
                .infixl(terms.token("-").retn((l, r) -> new ASTBinaryOp(l, r, "-")), 10)
                .build(value());
    }

    public static Parser<ASTExp> bicond() {
        return operator().next(l ->
                terms.token("<", ">").source()
                     .next(op -> operator().map(r -> (ASTExp)new ASTBinaryOp(l, r, op.trim()))).optional(l));
    }
    
    public static Parser<ASTExp> concat() {
        return bicond().next(l ->
            terms.token(".").source()
                .next(op -> bicond().map(r -> (ASTExp)new ASTBinaryOp(l, r, "."))).optional(l));
    }
    
    public static Parser<ASTExp> expression() {
        return concat();
    }
    @Value
    public static class ASTCommand implements AST {
        String command;
        ASTExp param;
    }
    public static Parser<ASTCommand> command() {
        return terms.token("return").or(terms.token("echo"))
                .next(t -> expression().map(exp -> new ASTCommand(t.toString(), exp)));
    }
    
    @Value
    public static class ASTAssignment implements ASTExp {
        ASTVariable variable;
        ASTExp expression;
    }
    
    public static Parser<ASTAssignment> assignment() {
        return variable().followedBy(terms.token("="))
                .next(v -> expression().map(exp -> new ASTAssignment(v, exp)));
    }
    
    public static Parser<String> identifier() {
        return Terminals.Identifier.PARSER;
    }
    
    @RequiredArgsConstructor
    public static class ASTFuncCall implements ASTExp {
        @Getter
        final String name;
        @Getter
        final List<ASTExp> params;
        @Setter
        @Getter
        ASTFunction cache = null;
    }
    
    public static Parser<ASTFuncCall> funcCall() {
        return identifier().next(id ->
            expression().sepBy(terms.token(",")).between(terms.token("("), terms.token(")"))
                .map(param -> new ASTFuncCall(id, param)));
    }
    
    @Value
    public static class ASTIf implements AST {
        ASTExp expression;
        List<AST> statements;
    }
    
    public static Parser<ASTIf> ifStatement() {
        return terms.token("if").next(t -> expression().between(terms.token("("), terms.token(")"))
                .next(exp -> statements()
                .map(statements -> new ASTIf(exp, statements))));
    }
    
    public static Parser<AST> statement() {
        return Parsers.or(Parsers.or(bicond(), command()).followedBy(terms.token(";")),
                ifStatement());
    }
    
    public static Parser<List<AST>> statements() {
        return Parsers.or(
                statement().map(s -> Arrays.asList(s)), 
                statement().many().between(terms.token("{"), terms.token("}")));
    }
    @Value
    public static class ASTFunction implements AST {
        String name;
        List<ASTVariable> params;
        List<AST> statements;
    }
    
    public static Parser<ASTFunction> function() {
        return terms.token("function").next(identifier().next(
                name -> variable().sepBy(terms.token(",")).between(terms.token("("), terms.token(")")).next(
                params -> statement().many().between(terms.token("{"), terms.token("}")).map(st -> new ASTFunction(name, params, st)))));
    }
    
    public static Parser<List<AST>> script() {
        return terms.token("<?php").next(Parsers.or(function(), statement()).many());
    }
    
    public static Parser<List<AST>> createParser() {
        return script().from(tokenizer, ignored);
    }
}
