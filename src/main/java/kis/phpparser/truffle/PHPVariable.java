package kis.phpparser.truffle;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import kis.phpparser.truffle.TrufllePHPNodes.PHPExpression;
import lombok.AllArgsConstructor;

/**
 *
 * @author naoki
 */
public abstract class PHPVariable { // can't make it an interface since APT can't generate proper code
    @NodeInfo(shortName = "variable")
    @NodeField(name = "slot", type = FrameSlot.class)
    @AllArgsConstructor
    public static abstract class PHPVariableRef extends PHPExpression{
        abstract FrameSlot getSlot();

        @Specialization(guards = "isLong(vf)")
        long readLong(VirtualFrame vf) {
            return FrameUtil.getLongSafe(vf, getSlot());
        }
        
        @Specialization(guards = "isBoolean(vf)")
        boolean readBoolean(VirtualFrame vf) {
            return FrameUtil.getBooleanSafe(vf, getSlot());
        }
        
        Object readGeneric(VirtualFrame vf) {
            if (!vf.isObject(getSlot())) {
                CompilerDirectives.transferToInterpreter();
                Object result = vf.getValue(getSlot());
                vf.setObject(getSlot(), result);
                return result;
            }
            return FrameUtil.getObjectSafe(vf, getSlot());
        }
        
        boolean isLong(VirtualFrame frame) {
            return frame.getFrameDescriptor().getFrameSlotKind(getSlot()) == FrameSlotKind.Long;
        }

        boolean isBoolean(VirtualFrame frame) {
            return frame.getFrameDescriptor().getFrameSlotKind(getSlot()) == FrameSlotKind.Boolean;
        }
    
    }
    
    @NodeInfo(shortName = "assignment")
    @NodeChild(value = "valueNode")
    @NodeField(name = "slot", type = FrameSlot.class)
    public static abstract class PHPVariableAssignment extends TrufllePHPNodes.PHPExpression {

        protected abstract FrameSlot getSlot();

        @Specialization(guards = "isLongKind(vf)")
        long writeLong(VirtualFrame vf, long value) {
            vf.getFrameDescriptor().setFrameSlotKind(getSlot(), FrameSlotKind.Long);
            vf.setLong(getSlot(), value);
            return value;
        }

        @Specialization(guards = "isBooleanKind(vf)")
        boolean writeBoolean(VirtualFrame vf, boolean value) {
            vf.getFrameDescriptor().setFrameSlotKind(getSlot(), FrameSlotKind.Boolean);
            vf.setBoolean(getSlot(), value);
            return value;
        }
        
        @Specialization(replaces = {"writeLong", "writeBoolean"})
        Object writeGeneric(VirtualFrame vf, Object value) {
            vf.getFrameDescriptor().setFrameSlotKind(getSlot(), FrameSlotKind.Object);
            vf.setObject(getSlot(), value);
            return value;
        }
    

        protected boolean isLongKind(VirtualFrame vf) {
            FrameSlotKind kind = vf.getFrameDescriptor().getFrameSlotKind(getSlot());
            return kind == FrameSlotKind.Long;
        }

        protected boolean isBooleanKind(VirtualFrame vf) {
            FrameSlotKind kind = vf.getFrameDescriptor().getFrameSlotKind(getSlot());
            return kind == FrameSlotKind.Boolean;
        }
    }
}
