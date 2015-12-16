package yar.concurrent.client;

import yar.YarConfig;
import yar.protocol.YarRequest;
import yar.protocol.YarResponse;
import yar.transport.YarTransport;
import yar.transport.YarTransportFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by zhoumengkang on 2/12/15.
 */
public class YarConcurrentClient {

    private static ExecutorService executorService;
    private static List<YarConcurrentTask> yarConcurrentTasks;

    static{
        init();
    }

    private static void init(){
        yarConcurrentTasks = new ArrayList<YarConcurrentTask>();
        executorService = Executors.newCachedThreadPool();
    }

    public static void call(YarConcurrentTask yarConcurrentTask) {
        yarConcurrentTasks.add(yarConcurrentTask);
    }

    public static void loop() {

        List<Future<Object>> result =new ArrayList<Future<Object>>();

        try{
            for (YarConcurrentTask task : yarConcurrentTasks){
                Future<Object> future = executorService.submit(new YarClientCallable(task));
                result.add(future);
            }

        }catch(Exception e){

        }


        for(Future<Object> future:result){
            try {
                System.out.println(System.currentTimeMillis() + " 返回值: " + future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void reset(){
        yarConcurrentTasks = null;
        yarConcurrentTasks = new ArrayList<YarConcurrentTask>();
    }

    public static class YarClientCallable implements Callable<Object> {

        private YarConcurrentTask yarConcurrentTask;

        public YarClientCallable(YarConcurrentTask yarConcurrentTask) {
            this.yarConcurrentTask = yarConcurrentTask;
        }

        public Object call() throws Exception {
            System.out.println(System.currentTimeMillis() + " : " + Thread.currentThread().getName());

            YarResponse yarResponse = null;

            YarRequest yarRequest = new YarRequest();
            yarRequest.setId(yarConcurrentTask.getId());
            yarRequest.setMethod(yarConcurrentTask.getMethod());
            yarRequest.setParameters(yarConcurrentTask.getParams());
            yarRequest.setPackagerName(YarConfig.getString("yar.packager"));

            YarTransport yarTransport = YarTransportFactory.get(YarConfig.getString("yar.transport"));
            yarTransport.open("http://10.211.55.4/yar/server/RewardScoreService.class.php");

            try {
                yarResponse = yarTransport.exec(yarRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert yarResponse != null;

            return yarResponse.getRetVal();
        }
    }

}
