package me.noreason.platform.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * Created on 2014-11-11
 * <p/>
 * author: MSK
 * description:
 */
public class PlatformSignUtil {

    private String secretKey;

    private PlatformSignUtil(String secretKey){
        this.secretKey = secretKey;
    }

    public static PlatformSignUtil getInstance(String secretKey){
        return new PlatformSignUtil(secretKey);
    }

    public String sign(String url, Map<String, String> params) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        for (String key: keys){
            if ("signature".equals(key) | "sign_method".equals(key)) continue;
            sb.append(String.format("%s=%s&", key,  URLEncoder.encode(params.get(key), "UTF-8")));
        }
        sb.deleteCharAt(sb.length()-1);
        String sortedUrl = sb.toString();
        sortedUrl = sortedUrl.replaceAll("\\+", "%20");
        sortedUrl = sortedUrl.replaceAll("\\*", "%2A");
        String urlStr = URLEncoder.encode(sortedUrl, "UTF-8");
        SecretKeySpec key = new SecretKeySpec(this.secretKey.getBytes("UTF-8"), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(key);
        byte[] bytes = mac.doFinal(urlStr.getBytes("UTF-8"));
        return new String(Base64.encodeBase64(bytes)).replace("\r\n", "");
    }
}
