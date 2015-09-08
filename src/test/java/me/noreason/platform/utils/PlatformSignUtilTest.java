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
public class PlatformSignUtilTest {

    @Test
    public void sign() throws Exception {
        PlatformSignUtil util = PlatformSignUtil.getInstance("1111111111111111111");
        Map<String, String> params = new HashMap<String, String>();
        params.put("a", "b");
        params.put("b", "中文");
        params.put("c", " ");
        System.out.println(util.sign("http://127.0.0.1:10000/platform/api", params));

    }
}
