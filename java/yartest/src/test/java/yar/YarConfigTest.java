package yar;

import junit.framework.TestCase;

/**
 * Created by zhoumengkang on 12/12/15.
 */
public class YarConfigTest extends TestCase {
    public void testConfigRead() throws Exception {
        System.out.println(YarConfig.getString("yar.token"));
        System.out.println(YarConfig.getInt("yar.timeout"));
    }
}