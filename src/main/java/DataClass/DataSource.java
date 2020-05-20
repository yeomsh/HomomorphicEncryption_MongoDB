package DataClass;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

public interface DataSource {
    interface Callback {
        void onDataLoaded() throws Exception;
        void onDataFailed();
    }
}