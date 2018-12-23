package kis.phpparser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kis.phpparser.PHPParser.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
/**
 *
 * @author naoki
 */
public class PHPExecutor {
    @Data @AllArgsConstructor
    static class VariableHolder {
        Object value;
    }
    
    static class Context {

        Map<String, VariableHolder> variables = new HashMap<>();
        Map<String, ASTFunction> functions = new HashMap<>();
        Context parent;
        Context base;
        public Context() {
            base = this;
            parent = null;
        }
        public Context(Context parent) {
            this.parent = parent;
            this.base = parent.base;
        }
        public Optional<ASTFunction> findFunction(String name) {
            return Optional.ofNullable(base.functions.get(name));
        }
        public Optional<VariableHolder> findVariable(String name) {
            VariableHolder o = variables.get(name);
            if (o != null) {
                return Optional.of(o);
            }
            if (parent == null) {
                return Optional.empty();
            }
            return parent.findVariable(name);
        }
        public void putVariable(String name, Object value) {
            variables.put(name, new VariableHolder(value));
        }
        public Context newContext() {
            return new Context(this);
        }
    }
    
    public void visit(List<AST> script) {
        Context base = new Context();
        for (AST ast : script) {
            if (ast instanceof ASTFunction) {
                ASTFunction func = (ASTFunction)ast;
                base.functions.put(func.getName(), func);
            }
        }
        for (AST ast : script) {
            if (ast instanceof ASTFunction) {
                continue;
            }
            visit(base, ast);
        }
    }
    
    public Escape visit(Context ctx, AST ast) {
        if (ast instanceof ASTExp) {
            visit(ctx, (ASTExp)ast);
            return Escape.getInstance();
        } else if (ast instanceof ASTCommand) {
            return visit(ctx, (ASTCommand)ast);
        } else if (ast instanceof ASTIf) {
            return visit(ctx, (ASTIf)ast);
        }
        throw new RuntimeException("Unknown ast " + ast);
    }
    
    enum EscapeMode {
        NONE, RETURN;
    }
    static class Escape {
        static private Escape INSTANCE = new Escape();
        static Escape getInstance() {
            return INSTANCE;
        }
        EscapeMode getMode() {
            return EscapeMode.NONE;
        }
    }
    @Getter @AllArgsConstructor
    static class ReturnEscape extends Escape {
        Object value;

        @Override
        EscapeMode getMode() {
            return EscapeMode.RETURN;
        }
    }
    public Escape visit(Context ctx, ASTCommand command) {
        if ("echo".equals(command.getCommand())) {
            System.out.print(getString(visit(ctx, command.getParam())));
        } else if ("return".equals(command.getCommand())) {
            return new ReturnEscape(visit(ctx, command.getParam()));
        }
        return Escape.getInstance();
    }
    public Escape visit(Context ctx, ASTIf ifSt) {
        if (!getBoolean(visit(ctx, ifSt.getExpression()))) {
            return Escape.getInstance();
        }
        return visit(ctx.newContext(), ifSt.getStatements());
    }
    public Escape visit(Context ctx, List<AST> asts) {
        for(AST ast : asts) {
            Escape e = visit(ctx, ast);
            if (e.getMode() != EscapeMode.NONE) {
                return e;
            }
        }
        return Escape.getInstance();
    }
    public Object visit(Context ctx, ASTFunction func) {
        throw new RuntimeException("function should not invoke " + func.getName());
    }
    
    public double visit(Context ctx, IntValue value) {
        return value.getValue();
    }
    public String visit(Context ctx, StringValue value) {
        return value.getValue();
    }
    
    public Object visit(Context ctx, ASTExp value) {
        if (value instanceof ASTBinaryOp) {
            return visit(ctx, (ASTBinaryOp)value);
        } else if (value instanceof IntValue) {
            return visit(ctx, (IntValue)value);
        } else if (value instanceof ASTFuncCall) {
            return visit(ctx, (ASTFuncCall) value);
        } else if (value instanceof ASTAssignment) {
            return visit(ctx, (ASTAssignment)value);
        } else if (value instanceof ASTVariable) {
            return visit(ctx, (ASTVariable)value);
        } else if (value instanceof StringValue) {
            return visit(ctx, (StringValue)value);
        }
        throw new RuntimeException("Unknown ast " + value);
    }
    
    public Object visit(Context ctx, ASTVariable value) {
        return ctx.findVariable(value.getName())
                .map(VariableHolder::getValue)
                .orElseThrow(() -> new RuntimeException("Variable not found " + value.getName()));
    }
    
    public Object visit(Context ctx, ASTFuncCall value) {
        if ("time".equals(value.getName())) {
            return System.currentTimeMillis() / 1000;
        } else if("microtime".equals(value.getName())) {
            return System.nanoTime() / 1000 / 1000000.;
        }
        
        ASTFunction function = ctx.findFunction(value.getName())
                .orElseThrow(() -> new RuntimeException("function not found " + value.getName()));
        if (function.getParams().size() != value.getParams().size()) {
            throw new RuntimeException("Parameter count is not match for " + function.getName());
        }
        Context newCtx = ctx.newContext();
        for (int i = 0; i < function.getParams().size(); ++i) {
            newCtx.putVariable(function.getParams().get(i).getName(), visit(ctx, value.getParams().get(i)));
        }
        Escape esc = visit(newCtx, function.getStatements());
        if (esc instanceof ReturnEscape) {
            return ((ReturnEscape)esc).getValue();
        }
        return null;
    }
    
    public Object visit(Context ctx, ASTAssignment value) {
        Optional<VariableHolder> var = ctx.findVariable(value.getVariable().getName());
        Object result = visit(ctx, value.getExpression());
        if (var.isPresent()) {
            var.get().setValue(result);
        } else {
            ctx.putVariable(value.getVariable().getName(), result);
        }
        return result;
    }
    
    public Object visit(Context ctx, ASTBinaryOp value) {
        Object left = visit(ctx, value.getLeft());
        Object right = visit(ctx, value.getRight());
        
        if (".".equals(value.getOp())) {
            return getString(left) + getString(right);
        }
        
        double leftNum = getNum(left);
        double rightNum = getNum(right);
        
        switch (value.getOp()) {
            case "+":
                return leftNum + rightNum;
            case "-":
                return leftNum - rightNum;
            case "<":
                return leftNum < rightNum;
            case ">":
                return leftNum > rightNum;
            default:
                throw new RuntimeException("Unknown operator " + value.getOp());
        }
    }
    
    private boolean getBoolean(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof Boolean) {
            return (boolean)o;
        } else if (o instanceof Double) {
            return (double)o != 0;
        } else if (o instanceof String) {
            String s = o.toString();
            return !s.isEmpty() && !"0".equals(s);
        }
        throw new RuntimeException("unknown type " + o.getClass());
    }
    
    private double getNum(Object o) {
        if (o instanceof Double) {
            return (double)o;
        } else if (o instanceof String) {
            return Double.parseDouble((String)o); // todo convert top if non-num follows
        } else if (o instanceof Boolean) {
            return (boolean)o ? 1 : 0;
        }
        throw new RuntimeException("unknown type " + o.getClass());
    }
    
    private String getString(Object o) {
        if (o instanceof String) {
            return (String)o;
        } else if (o instanceof Double) {
            String s = o.toString();
            if (s.endsWith(".0")) {
                return s.substring(0, s.length() - 2);
            }
            return s;
        } else if (o instanceof Boolean) {
            return (boolean)o ? "1" : "";
        }
        throw new RuntimeException("unknown type " + o.getClass());
    }
}
