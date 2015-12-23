package demo;

import yar.client.YarClient;
import yar.client.YarClientOptions;

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
    private static String RewardScoreServiceUri = "http://mengkang.net/demo/yar-server/RewardScoreService.php";

    public static String add(int uid, int fid){
        YarClient yarClient  = new YarClient(RewardScoreServiceUri);
        RewardScoreService rewardScoreService = (RewardScoreService) yarClient.useService(RewardScoreService.class);
        return rewardScoreService.support(uid, fid);
    }

    public static String post(int uid, int fid){
        YarClientOptions yarClientOptions = new YarClientOptions();
        YarClient yarClient  = new YarClient(RewardScoreServiceUri,yarClientOptions);
        RewardScoreService rewardScoreService = (RewardScoreService) yarClient.useService(RewardScoreService.class);
        return rewardScoreService.post(uid, fid);
    }

}
