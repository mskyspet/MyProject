package me.noreason.security;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * Created on 2014-12-04
 * <p/>
 * author: MSK
 * description:
 */
public class RSATest {


    @Test
    public void verify() throws Exception{

        // get X509EncodedKey from base64 String
        String keyString = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCe7cSc5PPGPn1HE2PBjhJyTCflRxoOqqmMous86nJJMwEy6PpK2j9404epJovZhIfNpubWyQMlZnMIsnRX3+tD3e2Kh4aS2pW9vyKUt151FowUC0bTbdRJ53gnbBLeIOkbFVWggePD0RhzOq8D5fbe5EQkEvtZ4io/RTwptp8zXwIDAQAB";
        byte[] res = Base64.decodeBase64(keyString);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(res);

        // load public key
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(spec);

        // start to verify data and signature
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initVerify(publicKey);
        String value = "amt=&card_no=100000853768&fee_type=&id_no=230304196311134623&name=牛春梅&phone=15512345201&settlement_period=20144309 15:43:56&t=1418110863167&type=3";
        System.out.println(value);
        signature.update(value.getBytes());
        boolean is_ok = signature.verify(Base64.decodeBase64("D8aX17ICgJW+q0cTvvnKDVbMslBibgijkYqszPuiTpWfz2TGIhuLIqeR02bFMv+3K4CkcS+OFQYR9FltpgS35mxbicUq4AI6pTpBUxe43HwGWW+t/xIpvYGaysul4BCGnBgdhngHZhVx6X3DUykWdzqQiR72i5XBIsoaOMHewm4="));
        System.out.println(is_ok);
    }

    @Test
    public void sign() throws Exception{
        // load privite key from base64 String
        String keyString = "MIICXAIBAAKBgQDK4Amd7uxLk9wLqWynfyNJLWOegA9iuisebeo5+U8g5PPOArsLdU1DdgXXiJDBaVTXKebYfdf0OmjcSHBWgd/P0NTbDNWCzl5vHnx8zlUU+KXah7fy0SWPyAMXHqqtL9FNZ7l0aAoeRTI9krzw7b/f+Ffdse8oHeBa8oCUbe4LBwIDAQABAoGAWvkT1upos4teRSx4OLPeUtYzscOjIpZw93g6bDzLquiAG4VF8uFrADFQdVIb918QWFvF1Ja53B7FHuKs3V3nyjAAsF38JYENXRjrpWji0JMd0OxlWboWQhu/OtCxYgs9A3GUeHshXw/DTCDJEZe4Sh9qpiL+gcN+1bCXRf6DL0ECQQDy80Qr5bXCL830uGFWwav98Bm0rZ6m2pYMFF35PCvFS/yUhjynwthPJwoEjpe5rtXUgIt+H6aefVQTgP8MXDzJAkEA1cW1+KBTblkV+cd3hU1uJ3+svCC/+ljyQo8E1BGfPL5m5MMkGjrZhCcK9hNtlJGHX1kjCz8R+pctn28qtSaBTwJAP4kU8/Hv9aew0eYCNsERbKiE4HZjPjl98gzFgq+UttUnaeQtKqZPEWwhN43e6gVqEFfJoyM93Mbj3PHoQWBB2QJBALiK9Yys21lnjpESoRtyvKVcksHVg+Wa44iR7pkknVLTDABwdbovE5gCHHRSUUbwWuz1BkuvJtYtrDT2wuefVgECQB8iqMCLtCXyqWwm/ps1q6XGv2/nAMgTa29tC+xK3XMQUG5G5hfI/mlQiiZcycbwSO0E3HFVUEJlw0AZzXXuzdM=";
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(keyString));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);


        // sign data
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(privKey);
        String value = URLEncoder.encode("amt=1518.5&card_no=10002224&fee_type=2&id_no=152502195910141214&name=张春贵&phone=18844445545&settlement_period=201412&t=1417658727898&type=4", "UTF-8");
        signature.update(value.getBytes());
        String result = Base64.encodeBase64String(signature.sign());

        System.out.println(result);
    }

    @Test
    public void decrypt() throws Exception{

        //
        byte[] buffers = Base64.decodeBase64("BqGpKFt3jk3Sw9+vWFjX6cylqFMmIQ7LBMmJWVSlwUY60BuTO0m7smHycapYQZ2MQfi0gGH6yZ0AwrTPiQoKiq14Lvr5HO5RvoPb9ZVRrNjxJ8m/kmgZUlfCHOzrxbl/GkwIF5Ia1wg4IkIBcDGaA5QZTlDY97imSXQvGBrOIXpwetsYH6poNPio0iwfL970j+gztP/wpE2y0pDQOupEJLr/t3n6sfNBzsOOIZJQqt6P15EwkfOLXjmPgbJsM6DGtzDQz5dHL0jLzcUxS7R//fq5+O15lQje3vFSj5VBHjeQoMLQ/ZZM3QsnakPR+6V7wbtcbIOXpmKU4bZ7QIUtFQ==");
        String keyString = "MIICXgIBAAKBgQCd60otLPAgrRbLQJafncmtxpVZJDumTzo9NzNTh3CZN+voittvXclOjpGonWgYiV1Bc44b0Sl08SY3FSXgPPmNaWTSD9acwohPRkiZf0r8oLXt/zIQFGKsTyyrB5oS0Ig5tTa//ACns8Phi2XE2voVlLLXGRR3TRK7UxK6jIROGwIDAQABAoGBAImM9GM9DTupCH2pmIkXax/OJTAr+zj1MYk4l6BeDJmot/cpW9E5I6slS4JO+OjGXVAxUsqv+Sg/FEZOSVt0QPipEzmItLpg//XWfn+JFHcvDuXxRvDe1LL9jA3MzurIaBvmzh6VohyZ6zDZPC99ZPM1F5k5PgUAVRFjneq9455BAkEA0U9WtiBI0sbuj4cUcy7GBUXsDTycx9QX61Jlz6olsp3PAZUVnNhiWecfqq+3DjLfezT1U2fma5IdNWuurQhoswJBAMElRiCWAcCsoNCjwCymjbyK6SvcOCFIRUVzgPe7cKmpURKNiEWS7E3l9x4FLWHqrEl7hYdb9VNnB5dL4/FbqPkCQQCb0hm2ikBMkgZFWYVy3k5S3KQHkqkQ5qdL69KZj4xy9aKHBgrJ8hj93t8nPZrkeiD/YwSDrCTQzjzqaRjn/gubAkAKBS0GM8IThOEy8cvehSWFSo+ePuaKk31EhSGDuK23n5kdJUqUkXLA99VCMXKRSeN2THo484Ret1Glf8Jrmx3BAkEAxeoGBpOtF+1EMezk9W2hyV5U9td0/g7S9w2WIN56/IK1vOcFdWtS3uxsaG6b1N4YXCIAuiixbvdqDbCjt5xNUg==";
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(keyString));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privKey);
        System.out.println(buffers.length);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (int i=0;i<buffers.length/128; i++){
            os.write(cipher.doFinal(Arrays.copyOfRange(buffers, i*128, (i+1)*128)));
        }
        String value = new String(os.toByteArray(), "UTF-8");
        System.out.println(value);
        System.out.println(URLDecoder.decode(value, "UTF-8"));

    }

}
