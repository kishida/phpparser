/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kis.phpparser.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.NodeInfo;
import kis.phpparser.truffle.TrufllePHPNodes.PHPExpression;
import kis.phpparser.truffle.TrufllePHPNodes.PHPStatement;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author naoki
 */
public class PHPControlFlow {
    @AllArgsConstructor @Getter
    static class ReturnException extends ControlFlowException {
        Object result;
    }
    
    @NodeInfo(shortName = "return")
    public static class PHPReturnNode extends PHPStatement {
        @Child PHPExpression result;

        @Override
        void executeVoid(VirtualFrame virtualFrame) {
            Object value = result.executeGeneric(virtualFrame);
            throw new ReturnException(value);
        }
    }
}
