package kis.phpparser.truffle;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.ConditionProfile;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import kis.phpparser.truffle.TrufllePHPNodes.PHPExpression;
import kis.phpparser.truffle.TrufllePHPNodes.PHPStatement;
import lombok.AllArgsConstructor;

/**
 *
 * @author naoki
 */
public class PHPControlFlow {
    @NodeInfo(shortName = "if")
    @AllArgsConstructor
    static class PHPIfNode extends PHPStatement {
        @Child private PHPExpression condition;
        @Child private PHPStatement thenStatement;

        private ConditionProfile profile = ConditionProfile.createCountingProfile();
        
        @Override
        void executeVoid(VirtualFrame virtualFrame) {
            boolean result;
            try {
                result = condition.executeBoolean(virtualFrame);
            } catch (UnexpectedResultException ex) {
                throw new RuntimeException("need boolean");
            }
            if (profile.profile(result)) {
                thenStatement.executeVoid(virtualFrame);
            }
        }
    }
    
    @NodeInfo(shortName = "block")
    @AllArgsConstructor
    static class PHPBlock extends PHPStatement {
        @Children PHPStatement[] statements;

        @Override
        @ExplodeLoop
        void executeVoid(VirtualFrame virtualFrame) {
            CompilerAsserts.compilationConstant(statements.length);
            for (PHPStatement st : statements) {
                st.executeVoid(virtualFrame);
            }
        }
        
        List getStatements() {
            return Collections.unmodifiableList(Arrays.asList(statements));
        }
    }
}
