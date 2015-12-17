package yar.concurrent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    protected final static Logger logger = LoggerFactory.getLogger(YarConcurrentClient.class);

    private static ExecutorService executorService;
    private static List<YarConcurrentTask> yarConcurrentTasks;

    static{
        init();
    }

    private static void init(){
        yarConcurrentTasks = new ArrayList<>();
        executorService = Executors.newCachedThreadPool();
    }

    public static void call(YarConcurrentTask yarConcurrentTask) {
        yarConcurrentTask.setId(yarConcurrentTasks.size() + 1);
        yarConcurrentTasks.add(yarConcurrentTask);
    }

    public static boolean loop(YarConcurrentCallback callback) {

        List<Future<Object>> result =new ArrayList<>();

        try{
            for (YarConcurrentTask task : yarConcurrentTasks){
                Future<Object> future = executorService.submit(new YarClientCallable(task));
                result.add(future);
            }

            try {
                callback.call();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }


        for(Future<Object> future:result){
            try {
                if (future.get() != null){
                    logger.info(future.get().toString());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public static void reset(){
        yarConcurrentTasks = null;
        yarConcurrentTasks = new ArrayList<>();
    }

    public static class YarClientCallable implements Callable<Object> {

        private YarConcurrentTask yarConcurrentTask;

        public YarClientCallable(YarConcurrentTask yarConcurrentTask) {
            this.yarConcurrentTask = yarConcurrentTask;
        }

        public Object call() throws Exception {

            logger.debug("开始处理任务" + yarConcurrentTask.getId());

            YarResponse yarResponse = null;

            YarRequest yarRequest = new YarRequest();
            yarRequest.setId(yarConcurrentTask.getId());
            yarRequest.setMethod(yarConcurrentTask.getMethod());
            yarRequest.setParameters(yarConcurrentTask.getParams());
            yarRequest.setPackagerName(yarConcurrentTask.getPackagerName());

            YarTransport yarTransport = YarTransportFactory.get(yarConcurrentTask.getTransport());
            yarTransport.open(yarConcurrentTask.getUri());

            try {
                yarResponse = yarTransport.exec(yarRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (yarConcurrentTask.getCallback() != null){
                assert yarResponse != null;
                return yarConcurrentTask.getCallback().setRetValue(yarResponse.getRetVal()).call();
            }
            return null;
        }
    }

}
