package demo;

import yar.YarClient;
import yar.YarConfig;
import yar.concurrent.client.YarConcurrentCallback;
import yar.concurrent.client.YarConcurrentClient;
import yar.concurrent.client.YarConcurrentCallStack;

/**
 * Created by zhoumengkang on 3/12/15.
 */
public class SupportService {

    /**
     * 定义 rpc 接口
     */
    public interface RewardScoreService{
        String support(int uid,int fid);
        String post(int uid,int fid);
    }

    /**
     * rpc api 地址
     */
    private static String RewardScoreServiceUri = "h1ttp://10.211.55.4/yar/server/RewardScoreService.class.php";

    public static String add(int uid, int fid){
        YarClient yarClient  = new YarClient(RewardScoreServiceUri);
        RewardScoreService rewardScoreService = (RewardScoreService) yarClient.useService(RewardScoreService.class);
        return rewardScoreService.support(uid, fid);
    }

    public static String post(int uid, int fid){
        YarClient yarClient  = new YarClient(RewardScoreServiceUri);
        RewardScoreService rewardScoreService = (RewardScoreService) yarClient.useService(RewardScoreService.class);
        return rewardScoreService.post(uid, fid);
    }

    public class callback extends YarConcurrentCallback {

        public void async() {
            System.out.println("现在, 所有的请求都发出去了, 还没有任何请求返回\n");
        }

        public Object success() {
            return retValue;
        }

    }

    public void testLoop() throws Exception {
        String transport = YarConfig.getString("yar.transport");
        String packagerName = YarConfig.getString("yar.packager");

        YarConcurrentClient.call(new YarConcurrentCallStack(RewardScoreServiceUri, "support", new Object[]{1, 2}, transport, packagerName, new callback()));
        YarConcurrentClient.call(new YarConcurrentCallStack(RewardScoreServiceUri,"post",new Object[]{1,2},transport,packagerName,new callback()));
        YarConcurrentClient.loop(new callback());
        YarConcurrentClient.reset();
    }

}
