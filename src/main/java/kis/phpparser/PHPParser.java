/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kis.phpparser;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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
            "<", ">", "+", "-", "(", ")", ";", "="};
    static String[] keywords = {
            "function", "return", "echo"};
    static Terminals terms = Terminals.operators(operators).words(Scanners.IDENTIFIER).keywords(keywords).build();
    static Parser<Void> ignored = Scanners.WHITESPACES.optional();
    static Pattern varToken = Patterns.isChar('$').next(Patterns.isChar(CharPredicates.IS_ALPHA)).many1();

    static Parser<String> varParser = varToken.toScanner("variable").source();

    static Parser<?> tokenizer = Parsers.or(
            terms.tokenizer(),
            varParser,
            Terminals.Identifier.TOKENIZER,
            Terminals.IntegerLiteral.TOKENIZER);

    interface AST{}
    interface ASTExp extends AST{}

    @AllArgsConstructor @EqualsAndHashCode
    public static class IntValue implements ASTExp {
        int value;

        @Override
        public String toString() {
            return value + "";
        }
    }

    public static Parser<IntValue> integer() {
        return Terminals.IntegerLiteral.PARSER.map(s -> new IntValue(Integer.parseInt(s)));
    }

    @AllArgsConstructor @EqualsAndHashCode
    public static class ASTVariable implements ASTExp {
        String name;
    }

    public static Parser<ASTVariable> variable() {
        return varParser.map(v -> new ASTVariable(v.substring(1)));
    }

    public static Parser<ASTExp> value() {
        return Parsers.or(integer(), variable());
    }

    @AllArgsConstructor @ToString
    public static class ASTBinaryOp implements ASTExp {
        ASTExp left;
        ASTExp right;
        String op;
    }

    public static Parser<ASTExp> operator() {
        return new OperatorTable<ASTExp>()
                .infixl(terms.token("+").retn((l, r) -> new ASTBinaryOp(l, r, "+")), 10)
                .infixl(terms.token("-").retn((l, r) -> new ASTBinaryOp(l, r, "-")), 10)
                .build(value());
    }

    public static Parser<ASTExp> bicond() {
        return operator().next(l ->
                terms.token("<", ">").source()
                     .next(op -> operator().map(r -> (ASTExp)new ASTBinaryOp(l, r, op))).optional(l));
    }

    @AllArgsConstructor @ToString
    public static class ASTCommand implements AST {
        String command;
        ASTExp param;
    }
    public static Parser<ASTCommand> command() {
        return terms.token("return").or(terms.token("echo"))
                .next(t -> bicond().map(exp -> new ASTCommand(t.toString(), exp)));
    }
}
