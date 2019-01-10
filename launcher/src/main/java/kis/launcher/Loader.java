package kis.launcher;

import java.io.File;
import java.io.IOException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

/**
 *
 * @author naoki
 */
public class Loader {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("need php file");
            System.exit(1);
        }
        Context ctx = Context.create("php");
        Source source = Source.newBuilder("php", new File(args[0])).build();
        ctx.eval(source);
    }
}
