
package kis.phpparser.truffle;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.TypeSystem;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import lombok.AllArgsConstructor;

/**
 *
 * @author naoki
 */
public class PHPNodes {
    static class PHPRootNode extends RootNode {
        @Child PHPStatement body;

        public PHPRootNode(TruffleLanguage<?> language, FrameDescriptor frameDescriptor,
                PHPStatement body) {
            super(language, frameDescriptor);
            this.body = body;
        }
        
        @Override
        public Object execute(VirtualFrame frame) {
             body.executeVoid(frame);
             return true;
        }
    }
    
    @TypeSystem({double.class, boolean.class, String.class})
    static abstract class PHPTypes {
    }
    
    @NodeInfo(language = "TrufflePHP", description = "Base class for PHP")
    @TypeSystemReference(PHPTypes.class)
    static abstract class PHPNode extends Node {
    }

    static abstract class PHPStatement extends PHPNode {
        abstract void executeVoid(VirtualFrame virtualFrame);
    }
    
    static abstract class PHPExpression extends PHPStatement {
        abstract Object executeGeneric(VirtualFrame virtualFrame);

        @Override
        void executeVoid(VirtualFrame virtualFrame) {
            executeGeneric(virtualFrame);
        }
        
        double executeDouble(VirtualFrame vf) throws UnexpectedResultException {
            return PHPTypesGen.expectDouble(this.executeGeneric(vf));
        }
        boolean executeBoolean(VirtualFrame vf) throws UnexpectedResultException {
            return PHPTypesGen.expectBoolean(this.executeGeneric(vf));
        }
        String executeString(VirtualFrame vf) throws UnexpectedResultException {
            return PHPTypesGen.expectString(this.executeGeneric(vf));
        }
    }
    
    @AllArgsConstructor
    static class DoubleNode extends PHPExpression {
        final double value;

        @Override
        double executeDouble(VirtualFrame vf) throws UnexpectedResultException {
            return value;
        }
        
        @Override
        Object executeGeneric(VirtualFrame virtualFrame) {
            return value;
        }

        @Override
        public String toString() {
            return "" + value;
        }
    }
    
    @AllArgsConstructor
    static class BooleanNode extends PHPExpression {
        final boolean value;

        @Override
        boolean executeBoolean(VirtualFrame vf) throws UnexpectedResultException {
            return value;
        }
        
        @Override
        Object executeGeneric(VirtualFrame virtualFrame) {
            return value;
        }

        @Override
        public String toString() {
            return "" + value;
        }
    }
    
    @AllArgsConstructor
    static class StringNode extends PHPExpression {
        final String value;

        @Override
        String executeString(VirtualFrame vf) throws UnexpectedResultException {
            return value;
        }

        @Override
        Object executeGeneric(VirtualFrame virtualFrame) {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
