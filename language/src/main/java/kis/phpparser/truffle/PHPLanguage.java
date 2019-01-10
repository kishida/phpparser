package kis.phpparser.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import java.util.List;
import kis.phpparser.PHPParser;
import kis.phpparser.truffle.PHPControlFlow.PHPBlock;
import kis.phpparser.truffle.PHPNodes.PHPRootNode;
import kis.phpparser.truffle.PHPNodes.PHPStatement;
import org.jparsec.Parser;

/**
 *
 * @author naoki
 */
@TruffleLanguage.Registration(name = "TrufflePHP", id = "php",
        defaultMimeType = PHPLanguage.MIME_TYPE, characterMimeTypes = PHPLanguage.MIME_TYPE)
@ProvidedTags({StandardTags.CallTag.class, StandardTags.StatementTag.class, 
    StandardTags.RootTag.class, DebuggerTags.AlwaysHalt.class})
public class PHPLanguage extends TruffleLanguage<PHPContext>{
    public static final String MIME_TYPE = "application/x-php";
    
    @Override
    protected CallTarget parse(ParsingRequest request) throws Exception {
        Parser<List<PHPParser.AST>> parser = PHPParser.createParser();
        List<PHPParser.AST> script = parser.parse(request.getSource().getReader());
        PHPTreeGenerater generator = new PHPTreeGenerater(this);
        FrameDescriptor frame = new FrameDescriptor();
        PHPStatement[] nodes = generator.visit(frame, script);
        PHPRootNode root = new PHPRootNode(this, frame, new PHPBlock(nodes));
        return Truffle.getRuntime().createCallTarget(root);
    }
    
    @Override
    protected PHPContext createContext(Env env) {
        return new PHPContext();
    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        return false;
    }
    
}
