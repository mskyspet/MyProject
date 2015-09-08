package me.noreason.security;


import com.sun.org.apache.xml.internal.security.keys.storage.implementations.CertsInFilesystemDirectoryResolver;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.bouncycastle.asn1.cms.KEKIdentifier;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

/**
 * Created on 2015-03-10
 * <p/>
 * author: MSK
 * description:
 *  httpclient校验证书
 *
 *  获取证书方法:
 *      1. chrome打开https://api.noreason.me/
 *      2. 点击左上角的https标识, 找到复制到文件, 一路确认, 保存在api.cer
 *      3. 命令行执行keytool -importcert -file api.cer -keystore api.keystore -alias jlbank
 *      4. 输入keyStore保护密码, 如123456
 *      5. 生成jlbank.keystore文件
 */
public class HttpsCientTest {

    Logger logger = Logger.getAnonymousLogger();

    KeyStore getKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(HttpsCientTest.class.getResourceAsStream("/jlbank.keystore"), "infohold".toCharArray());
        return keyStore;
    }

    @Test
    public void request_verify(){
        /**
         *  不指定证书授信证书库
         *  使用默认系统授信链
         */
        // all is right
        HttpClient client = null;
        try {
            client = HttpClients.createDefault();
            client.execute(new HttpGet("https://www.baidu.com/"));
            Assert.assertTrue("always right", true);
        } catch (IOException e) {
            Assert.assertTrue("baidu should be ok", false);
        }

        // url is not compatible
        try {
            client.execute(new HttpGet("https://192.241.196.94/"));
            Assert.assertFalse("req should faild", true);
        } catch (SSLHandshakeException e) {
            logger.info("get right excetpion");
        } catch (IOException e) {
            e.printStackTrace();
            Assert.assertFalse("req should not fail due to this", true);
        }
    }

    @Test
    public void request_with_keystore() throws Exception {
        /**
         * 使用授信证书库
         */


        KeyStore keyStore = getKeyStore();
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keyStore, null).build();
        HttpClient client = HttpClients.custom().setSslcontext(sslContext).build();

        try {
            // 吉林银行在授信证书库内, 因此可以验证通过
            client.execute(new HttpGet("https://mobile.jlbank.com.cn/d/"));
            Assert.assertTrue("always right", true);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.assertTrue("jlbank domain should be ok", false);
        }


        try {
            // 百度不在授信证书库内, 不可通过
            client.execute(new HttpGet("https://www.baidu.com/"));
            Assert.assertFalse("req should faild", true);
        } catch (SSLHandshakeException e) {
            logger.info("get right excetpion");
        } catch (IOException e) {
            e.printStackTrace();
            Assert.assertFalse("req should not fail due to this", true);
        }
    }

    @Test
    public void requestWithExpiredKeyStore() throws Exception {
        /**
         *  查询过期时间
         */
        KeyStore keyStore = getKeyStore();
        X509Certificate x509Certificate = (X509Certificate) keyStore.getCertificate("jlbank");
        System.out.println(x509Certificate.getNotAfter());
    }

    @Test
    public void addCertificationByCer() throws Exception{
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream("E://ya.cer"));
        Certificate certificate = certificateFactory.generateCertificate(bis);
        KeyStore keyStore = getKeyStore();
        keyStore.setCertificateEntry("yabank", certificate);
        FileOutputStream os = new FileOutputStream("E://new.keystore");
        keyStore.store(os, "infohold".toCharArray());
        os.flush();
        os.close();
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream("E://new.keystore"), "infohold".toCharArray());
        X509Certificate x509Certificate = (X509Certificate) keyStore.getCertificate("yabank");
        System.out.println(x509Certificate.getNotAfter());
    }

    @Test
    public void request_with_duplicate_cert() throws Exception {
        String apiUrl = "https://jluat.smallpay.cn/platform/api";
        String certUrl = "https://jluat.smallpay.cn/2_jluat.smallpay.cn_bundle.crt";

        KeyStore keyStore = getKeyStore();


        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream("E://jluat.cer"));
        Certificate certificate = certificateFactory.generateCertificate(bis);
        keyStore.setCertificateEntry("jluat1", certificate);

        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keyStore, null).build();
        HttpClient client = HttpClients.custom().setSslcontext(sslContext).build();

        try {
            // 吉林银行在授信证书库内, 因此可以验证通过
            client.execute(new HttpGet(apiUrl));
            Assert.assertTrue("always fail", false);
        } catch (IOException e) {
            Assert.assertTrue("should be fail", true);
        }

        logger.info("test existed cert success!");

//        SSLContext trustAllSslContext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {
//            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                return true;
//            }
//        }).build();
        // 沃通证书不在jre默认证书中
        // 导出沃通根证书
        // cd $JAVA_HOME\jre\lib\security
        // keytool -importcert -file wosign.cer -keystore cacerts -alias wosign
        // 默认密码 changeit
        HttpClient getCertClient = HttpClients.createDefault();// HttpClients.custom().setSslcontext(trustAllSslContext).build();
        HttpResponse response = getCertClient.execute(new HttpGet(certUrl));
        Certificate newCertificate = certificateFactory.generateCertificate(response.getEntity().getContent());
        keyStore.setCertificateEntry("jluat2", newCertificate);

        logger.info("load cert into keystore");

        sslContext = SSLContexts.custom().loadTrustMaterial(keyStore, null).build();
        client = HttpClients.custom().setSslcontext(sslContext).build();
        try {
            client.execute(new HttpGet(apiUrl));
            Assert.assertTrue("should ok now", true);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.assertTrue("no failed", false);
        }

        logger.info("test new cert success");


    }
}