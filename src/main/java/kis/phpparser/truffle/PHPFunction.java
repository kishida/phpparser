package kis.phpparser.truffle;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.Node.Children;
import com.oracle.truffle.api.nodes.NodeInfo;
import kis.phpparser.truffle.TrufllePHPNodes.PHPExpression;
import kis.phpparser.truffle.TrufllePHPNodes.PHPNode;
import kis.phpparser.truffle.TrufllePHPNodes.PHPStatement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 *
 * @author naoki
 */
public abstract class PHPFunction {
    
    @RequiredArgsConstructor
    static class FunctionObject {
        public final RootCallTarget callTarget;
        @Setter
        @Getter
        private MaterializedFrame lexicalScope;
    }
    
    static class InvokeNode extends PHPExpression {
        @Child protected PHPNode functionNode;
        @Children protected final PHPNode[] argumentNodes;
        @Child protected IndirectCallNode callNode;

        public InvokeNode(PHPNode functionNode, PHPNode[] argumentNodes) {
            this.functionNode = functionNode;
            this.argumentNodes = argumentNodes;
            this.callNode = Truffle.getRuntime().createIndirectCallNode();
        }

        @Override
        @ExplodeLoop
        Object executeGeneric(VirtualFrame virtualFrame) {
            //PHPFunction func = 
            return null;
        }
        

    }
    
    @NodeInfo(shortName = "body")
    @AllArgsConstructor
    static class PHPFunctionBody extends PHPNode {
        @Child private PHPStatement statement;
        
        
    }
}
