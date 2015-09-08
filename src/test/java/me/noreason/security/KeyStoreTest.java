package me.noreason.security;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * Created on 2015-03-13
 * <p/>
 * author: MSK
 * description:
 *  keyStore useage test
 *  keytool -import -file somecert.cer -keystore keystorename
 */
public class KeyStoreTest {

    @Test
    public void load() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(KeyStoreTest.class.getResourceAsStream("/api.keystore"), "abcd1234".toCharArray());
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()){
            System.out.println(aliases.nextElement());
        }
    }

}
