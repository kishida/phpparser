package kis.phpparser.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import kis.phpparser.truffle.PHPControlFlow.PHPBlock;
import kis.phpparser.truffle.PHPVariableFactory.PHPVariableAssignmentNodeGen;
import kis.phpparser.truffle.PHPNodes.PHPExpression;
import kis.phpparser.truffle.PHPNodes.PHPStatement;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author naoki
 */
public class PHPFunctions {
    public static class FunctionObject {
        @Getter
        private CallTarget target;
        
        public void setupFunction(PHPLanguage lang, FrameDescriptor f,
                String[] arguments, PHPStatement[] statements) {
            PHPStatement[] args = new PHPStatement[arguments.length + statements.length];
            for (int i = 0; i < arguments.length; ++i) {
                FrameSlot slot = f.findOrAddFrameSlot(arguments[i], i, FrameSlotKind.Illegal);
                args[i] = PHPVariableAssignmentNodeGen.create(new ReadArgNode(i), slot);
            }
            System.arraycopy(statements, 0, args, arguments.length, statements.length);
            target = Truffle.getRuntime().createCallTarget(
                    new FunctionRootNode(lang, f, new FunctionBodyNode(this, new PHPBlock(args))));
        }
    }
    
    static class FunctionRootNode extends RootNode {

        @Child PHPExpression body;

        public FunctionRootNode(TruffleLanguage<?> language, FrameDescriptor frameDescriptor,
                PHPExpression body) {
            super(language, frameDescriptor);
            this.body = body;
        }
        
        @Override
        public Object execute(VirtualFrame frame) {
            return body.executeGeneric(frame);
        }
    }
    
    static class PHPInvokeNode extends PHPExpression {
        FunctionObject function;
        @Children PHPExpression[] argValues;
        @CompilerDirectives.CompilationFinal
        DirectCallNode callNode;

        public PHPInvokeNode(FunctionObject function, PHPExpression[] argValues) {
            this.function = function;
            this.argValues = argValues;
        }

        @Override
        Object executeGeneric(VirtualFrame virtualFrame) {
            if (callNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                callNode = Truffle.getRuntime().createDirectCallNode(function.getTarget());
            }

            Object[] args = evalArgs(virtualFrame);
            for(;;) {
                try {
                    return callNode.call(args);
                } catch (RecursiveException rec) {
                    args = rec.getArgs();
                }
            }
        }
        
        @ExplodeLoop
        Object[] evalArgs(VirtualFrame virtualFrame) {
            CompilerAsserts.compilationConstant(argValues.length);
            Object[] args = new Object[argValues.length];
            for (int i = 0; i < argValues.length; ++i) {
                args[i] = argValues[i].executeGeneric(virtualFrame);
            }
            return args;
        }
    }
    
    @AllArgsConstructor
    static class ReadArgNode extends PHPExpression {
        private int index;

        @Override
        Object executeGeneric(VirtualFrame virtualFrame) {
            return virtualFrame.getArguments()[index];
        }
    }
    
    @AllArgsConstructor @Getter
    static class ReturnException extends ControlFlowException {
        Object result;
    }
    
    @AllArgsConstructor @Getter
    static class RecursiveException extends ControlFlowException {
        Object[] args;
    }
    
    @NodeInfo(shortName = "return")
    static class PHPReturnNode extends PHPStatement {
        @Child private PHPExpression result;
        @CompilationFinal Boolean recursive;

        public PHPReturnNode(PHPExpression result) {
            this.result = result;
        }

        @Override
        void executeVoid(VirtualFrame virtualFrame) {
            if (recursive == null) {
                if (result instanceof PHPInvokeNode) {
                    PHPInvokeNode invoke = (PHPInvokeNode) result;
                    for (Node node = getParent(); node != null; node = node.getParent()) {
                        if (node instanceof FunctionBodyNode) {
                            CompilerDirectives.transferToInterpreterAndInvalidate();
                            recursive = ((FunctionBodyNode)node).function == invoke.function;
                            break;
                        }
                    }
                }
                if (recursive == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    recursive = false;
                }
            }
            if (recursive) {
                throw new RecursiveException(((PHPInvokeNode)result).evalArgs(virtualFrame));
            } else {
                Object value = result.executeGeneric(virtualFrame);
                throw new ReturnException(value);
            }
        }
    }
    
    @AllArgsConstructor
    static class FunctionBodyNode extends PHPExpression {
        FunctionObject function;
        @Child private PHPStatement body;

        @Override
        Object executeGeneric(VirtualFrame virtualFrame) {
            try {
                body.executeVoid(virtualFrame);
            } catch (ReturnException ex) {
                return ex.getResult();
            }
            return false;
        }
    }
}
