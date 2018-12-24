package kis.phpparser.truffle;

import com.oracle.truffle.api.TruffleLanguage;

/**
 *
 * @author naoki
 */
public class PHPLanguage extends TruffleLanguage<PHPContext>{

    @Override
    protected PHPContext createContext(Env env) {
        return new PHPContext();
    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
