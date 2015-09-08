package me.noreason.platform.utils;

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created on 2014-12-11
 * <p/>
 * author: MSK
 * description:
 *  银联签名算法
 */
public class UnionPaySign {

    public static String md5_sign(String secertKey, Map<String, String> params) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuffer sb = new StringBuffer();
        for (String key: keys){
            if ("signature".equals(key) | "sign_method".equals(key)) continue;
            sb.append(String.format("%s=%s&", key,  URLEncoder.encode(params.get(key), "UTF-8")));
        }
        sb.deleteCharAt(sb.length()-1);
        String sortedParam = sb.toString();
        sortedParam = sortedParam.replaceAll("\\+", "%20");
        sortedParam = sortedParam.replaceAll("\\*", "%2A");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String secertKeyMd5 = Hex.encodeHexString(md5.digest(secertKey.getBytes("UTF-8")));
        String sign =Hex.encodeHexString(md5.digest((sortedParam + "&" + secertKeyMd5).getBytes("UTF-8")));
        return sign;
    }
}
