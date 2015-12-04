package demo;

import yar.YarClient;

/**
 * Created by zhoumengkang on 3/12/15.
 */
public class SupportService {

    public interface RewardScoreService{
        String support(int uid,int feedId);
    }

    public static String add(int uid, int feedId){
        YarClient yarClient = new YarClient("http://10.211.55.4/yar/server/RewardScoreService.class.php");
        RewardScoreService rewardScoreService = (RewardScoreService) yarClient.useService(RewardScoreService.class);
        return rewardScoreService.support(uid, feedId);
    }

    public static void main(String[] args) {
        String res = add(1, 2);
        System.out.println(res);
    }
}
