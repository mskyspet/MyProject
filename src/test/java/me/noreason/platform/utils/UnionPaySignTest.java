package me.noreason.platform.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2015-03-14
 * <p/>
 * author: MSK
 * description:
 */
public class UnionPaySignTest {

    @Test
    public void testMd5_sign() throws Exception {

        String secretkey = "jdlfjadjfljafjaf";
        Map<String, String> params = new HashMap<String, String>();
        params.put("a", "b");
        params.put("b", "中文");
        params.put("c", " ");
        System.out.println(UnionPaySign.md5_sign(secretkey, params));
    }
}
