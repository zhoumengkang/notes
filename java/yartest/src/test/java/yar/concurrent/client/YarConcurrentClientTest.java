package yar.concurrent.client;

import junit.framework.TestCase;
import yar.YarConfig;

/**
 * Created by zhoumengkang on 16/12/15.
 */
public class YarConcurrentClientTest extends TestCase {

    public class callback extends YarConcurrentCallback{

        public void async() {
            System.out.println("现在, 所有的请求都发出去了, 还没有任何请求返回\n");
        }

        public Object success() {
            return retValue;
        }

    }

    public void testLoop() throws Exception {
        String transport = YarConfig.getString("yar.transport");
        String uri = "http://10.211.55.4/yar/server/RewardScoreService.class.php";
        String packagerName = YarConfig.getString("yar.packager");

        YarConcurrentClient.call(new YarConcurrentTask(uri,"support",new Object[]{1,2},transport,packagerName,new callback()));
        YarConcurrentClient.call(new YarConcurrentTask(uri,"post",new Object[]{1,2},transport,packagerName,new callback()));
        YarConcurrentClient.loop(new callback());
        YarConcurrentClient.reset();
    }
}