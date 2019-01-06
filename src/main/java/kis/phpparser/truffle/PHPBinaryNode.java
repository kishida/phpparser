package kis.phpparser.truffle;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;

/**
 *
 * @author naoki
 */
@NodeChild("leftNode")
@NodeChild("rightNode")
public abstract class PHPBinaryNode extends PHPNodes.PHPExpression {
    @NodeInfo(shortName = "+")
    public static abstract class PHPAddNode extends PHPBinaryNode {
        @Specialization
        public double add(double left, double right) {
            return left + right;
        }
    }
    
    @NodeInfo(shortName= "-")
    public static abstract class PHPSubNode extends PHPBinaryNode {
        @Specialization
        public double sub(double left, double right) {
            return left - right;
        }
    }

    @NodeInfo(shortName= "<")
    public static abstract class PHPLessThanNode extends PHPBinaryNode {
        @Specialization
        public boolean lessThan(double left, double right) {
            return left < right;
        }
    }
    @NodeInfo(shortName= "-")
    public static abstract class PHPGreaterThanNode extends PHPBinaryNode {
        @Specialization
        public boolean greaterThan(double left, double right) {
            return left > right;
        }
    }
    
    @NodeInfo(shortName= ".")
    public static abstract class PHPConcatNode extends PHPBinaryNode {
        @Specialization
        public String concat(Object left, Object right) {
            return String.valueOf(left) + String.valueOf(right);
        }
    }
}
