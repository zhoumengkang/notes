package yar.concurrent.client;

import junit.framework.TestCase;
import yar.YarConfig;

/**
 * Created by zhoumengkang on 16/12/15.
 */
public class YarConcurrentClientTest extends TestCase {

    /**
     * rpc api 地址
     */
    static String RewardScoreServiceUri = "http://mengkang.net/demo/yar-server/RewardScoreService.php";

    public class callback extends YarConcurrentCallback {

        public void async() {
            System.out.println("现在, 所有的请求都发出去了, 还没有任何请求返回\n");
        }

        public Object success() {
            return retValue;
        }

    }

    public void testLoop() throws Exception {

        String packagerName = YarConfig.getString("yar.packager");

        for (int i = 0; i < 20; i++) {
            YarConcurrentClient.call(new YarConcurrentTask(RewardScoreServiceUri, "support", new Object[]{1, 2}, packagerName, new callback()));
        }
        for (int i = 0; i < 20; i++) {
            YarConcurrentClient.call(new YarConcurrentTask(RewardScoreServiceUri,"post",new Object[]{1,2},packagerName,new callback()));
        }

        YarConcurrentClient.loop(new callback());
        YarConcurrentClient.reset();
    }
}