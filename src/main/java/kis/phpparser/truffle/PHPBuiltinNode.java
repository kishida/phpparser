/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kis.phpparser.truffle;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import kis.phpparser.truffle.PHPNodes.PHPExpression;
import kis.phpparser.truffle.PHPNodes.PHPStatement;

/**
 *
 * @author naoki
 */
public abstract class PHPBuiltinNode {
    
    @NodeInfo(shortName = "echo")
    @NodeChild(value="param", type=PHPExpression.class)
    public static abstract class PHPEchoNode extends PHPStatement {
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
    public static abstract class PHPMicrotime extends PHPExpression {
        @Specialization
        double microtime() {
            return System.nanoTime() / 1000 / 1000000.;
        }
    }
}
