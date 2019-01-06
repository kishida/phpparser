package kis.phpparser.truffle;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kis.phpparser.PHPParser.*;
import kis.phpparser.truffle.PHPBuiltinNodeFactory.PHPEchoNodeGen;
import kis.phpparser.truffle.PHPControlFlow.PHPBlock;
import kis.phpparser.truffle.PHPControlFlow.PHPIfNode;
import kis.phpparser.truffle.PHPFunctions.PHPReturnNode;
import kis.phpparser.truffle.PHPVariableFactory.PHPVariableAssignmentNodeGen;
import kis.phpparser.truffle.PHPVariableFactory.PHPVariableRefNodeGen;
import kis.phpparser.truffle.PHPNodes.*;
import kis.phpparser.truffle.PHPBinaryNodeFactory.*;
import kis.phpparser.truffle.PHPBuiltinNode.PHPMicrotime;
import kis.phpparser.truffle.PHPBuiltinNodeFactory.PHPMicrotimeNodeGen;
import kis.phpparser.truffle.PHPFunctions.FunctionObject;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author naoki
 */
@RequiredArgsConstructor
public class PHPTreeGenerater {
    private final PHPLanguage lang;
    private final Map<String, FunctionObject> functions = new HashMap<>();
    private final PHPMicrotime microtime = PHPMicrotimeNodeGen.create();
    
    public PHPStatement[] visit(FrameDescriptor frame, List<AST> script) {
        script.stream()
                .filter(ast -> ast instanceof ASTFunction)
                .forEach(ast -> visit((ASTFunction)ast));
        return script.stream()
                .filter(ast -> !(ast instanceof ASTFunction))
                .map(ast -> visit(frame, ast))
                .toArray(PHPStatement[]::new);
    }

    void visit(ASTFunction func) {
        FunctionObject function = new FunctionObject();
        functions.put(func.getName(), function);
        
        String[] params = func.getParams().stream()
                .map(ASTVariable::getName)
                .toArray(String[]::new);
        final FrameDescriptor frame = new FrameDescriptor();
        PHPStatement[] statements = func.getStatements().stream()
                .map(ast -> visit(frame, ast))
                .toArray(PHPStatement[]::new);
        function.setupFunction(lang, frame, params, statements);
    }
    
    PHPStatement visit(FrameDescriptor frame, AST ast) {
        if (ast instanceof ASTExp) {
            return visit(frame, (ASTExp)ast);
        } else if (ast instanceof ASTCommand) {
            return visit(frame, (ASTCommand)ast);
        } else if (ast instanceof ASTIf) {
            return visit(frame, (ASTIf)ast);
        }
        throw new RuntimeException("Unknown ast:" + ast);
    }
    
    PHPStatement visit(FrameDescriptor frame, ASTCommand command) {
        PHPExpression param = visit(frame, command.getParam());
        if ("echo".equals(command.getCommand())) {
            return PHPEchoNodeGen.create(param);
        } else if ("return".equals(command.getCommand())) {
            return new PHPReturnNode(param);
        }
        throw new RuntimeException("Unknown command:" + command);
    }
    PHPIfNode visit(FrameDescriptor frame, ASTIf ifNode) {
        PHPExpression cond = visit(frame, ifNode.getExpression());
        PHPStatement[] statements = ifNode.getStatements().stream()
                .map(ast -> visit(frame, ast))
                .toArray(PHPStatement[]::new);
        return new PHPIfNode(cond, new PHPBlock(statements));
    }
    
    PHPExpression visit(FrameDescriptor frame, ASTExp exp) {
        if (exp instanceof ASTBinaryOp) {
            return visit(frame, (ASTBinaryOp)exp);
        } else if (exp instanceof ASTFuncCall) {
            return visit(frame, (ASTFuncCall) exp);
        } else if (exp instanceof ASTAssignment) {
            return visit(frame, (ASTAssignment)exp);
        } else if (exp instanceof ASTVariable) {
            return visit(frame, (ASTVariable)exp);
        } else if (exp instanceof IntValue) {
            return visit(frame, (IntValue)exp);
        } else if (exp instanceof StringValue) {
            return visit(frame, (StringValue)exp);
        }
        throw new RuntimeException("Unknown ast " + exp);
    }
    PHPExpression visit(FrameDescriptor frame, ASTBinaryOp op) {
        PHPExpression left = visit(frame, op.getLeft());
        PHPExpression right = visit(frame, op.getRight());
        
        switch (op.getOp()) {
            case "+":
                return PHPAddNodeGen.create(left, right);
            case "-":
                return PHPSubNodeGen.create(left, right);
            case "<":
                return PHPLessThanNodeGen.create(left, right);
            case ">":
                return PHPGreaterThanNodeGen.create(left, right);
            case ".":
                return PHPConcatNodeGen.create(left, right);
        }
        throw new RuntimeException("Unknown binop " + op);
    }
    PHPExpression visit(FrameDescriptor frame, ASTFuncCall func) {
        if ("microtime".equals(func.getName())) {
            return microtime;
        }
        FunctionObject funcObj = functions.get(func.getName());
        if (funcObj == null) {
            throw new RuntimeException("Function not found:" + func.getName());
        }
        PHPExpression[] argValues = func.getParams().stream()
                .map(ast -> visit(frame, ast))
                .toArray(PHPExpression[]::new);
        return new PHPFunctions.PHPInvokeNode(funcObj, argValues);
    }
    PHPExpression visit(FrameDescriptor frame, ASTAssignment assign) {
        FrameSlot slot = frame.findOrAddFrameSlot(assign.getVariable().getName(), FrameSlotKind.Illegal);
        return PHPVariableAssignmentNodeGen.create(visit(frame, assign.getExpression()), slot);
    }
    PHPExpression visit(FrameDescriptor frame, ASTVariable variable) {
        FrameSlot slot = frame.findOrAddFrameSlot(variable.getName(), FrameSlotKind.Illegal);
        return PHPVariableRefNodeGen.create(slot);
    }

    PHPExpression visit(FrameDescriptor frame, IntValue value) {
        return new DoubleNode(value.getValue());
    }
    PHPExpression visit(FrameDescriptor frame, StringValue value) {
        return new StringNode(value.getValue());
    }
    
}
