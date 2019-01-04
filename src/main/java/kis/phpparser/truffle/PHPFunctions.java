package kis.phpparser.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.NodeInfo;
import kis.phpparser.truffle.TrufllePHPNodes.PHPExpression;
import kis.phpparser.truffle.TrufllePHPNodes.PHPStatement;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author naoki
 */
public class PHPFunctions {
    static class FunctionObject {
        PHPStatement[] statements;
        static FunctionObject createFunction(String[] arguments, PHPStatement statement) {
            
        }
        
    }
    static class InvokeNode {
        
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
    
    @NodeInfo(shortName = "return")
    @AllArgsConstructor
    static class PHPReturnNode extends PHPStatement {
        @Child private PHPExpression result;

        @Override
        void executeVoid(VirtualFrame virtualFrame) {
            Object value = result.executeGeneric(virtualFrame);
            throw new ReturnException(value);
        }
    }
    
    static class FunctionBody extends PHPExpression {
        @Child private PHPStatement body;

        @Override
        Object executeGeneric(VirtualFrame virtualFrame) {
            try {
                body.executeVoid(virtualFrame);
            } catch (ReturnException ex) {
                return ex.getResult();
            }
            return null;
        }
        
    }
}
