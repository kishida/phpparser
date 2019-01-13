package kis.phpparser.truffle;

import java.io.File;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

/**
 *
 * @author naoki
 */
public class PHPMain {
    public static void main(String[] args) throws Exception {
        Context ctx = Context.create("php");
        ctx.eval(Source.newBuilder("php", new File("../sample/smallfib.php")).build());
    }
}
