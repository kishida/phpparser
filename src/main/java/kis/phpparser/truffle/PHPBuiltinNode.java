/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kis.phpparser.truffle;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import kis.phpparser.truffle.TrufllePHPNodes.PHPExpression;
import kis.phpparser.truffle.TrufllePHPNodes.PHPStatement;

/**
 *
 * @author naoki
 */
@NodeChild(value = "arguments", type=PHPExpression[].class)
public abstract class PHPBuiltinNode extends PHPStatement {
    
    @NodeInfo(shortName = "echo")
    abstract static class PHPEchoNode extends PHPBuiltinNode {
        @Specialization
        void echo(double value) {
            System.out.print(value);
        }
        @Specialization
        void echo(boolean value) {
            System.out.print(value);
        }
        @Specialization
        void echo(Object value) {
            System.out.print(value);
        }
    }
    
    @NodeInfo(shortName = "microtime")
    @NodeChild(value="param", type=PHPExpression.class)
    abstract static class PHPMicrotime extends PHPExpression {
        @Specialization
        double microtime(double param) {
            return System.nanoTime() / 1000 / 1000000.;
        }
    }
}
