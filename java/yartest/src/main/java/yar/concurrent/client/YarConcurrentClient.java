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
    private static List<YarConcurrentCallStack> yarConcurrentCallStacks;
    private static int YAR_PROTOCOL_PERSISTENT = 0;

    static{
        init();
    }

    private static void init(){
        yarConcurrentCallStacks = new ArrayList<>();
        executorService = Executors.newCachedThreadPool();
    }

    public static void call(YarConcurrentCallStack yarConcurrentCallStack) {
        yarConcurrentCallStack.setId(yarConcurrentCallStacks.size() + 1);
        yarConcurrentCallStacks.add(yarConcurrentCallStack);
    }

    public static boolean loop(YarConcurrentCallback callback) {

        List<Future<Object>> result =new ArrayList<>();

        try{
            for (YarConcurrentCallStack callStack : yarConcurrentCallStacks){
                Future<Object> future = executorService.submit(new Handle(callStack));
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
        yarConcurrentCallStacks = null;
        yarConcurrentCallStacks = new ArrayList<>();
    }

    public static class Handle implements Callable<Object> {

        private YarConcurrentCallStack yarConcurrentCallStack;

        public Handle(YarConcurrentCallStack yarConcurrentCallStack) {
            this.yarConcurrentCallStack = yarConcurrentCallStack;
        }

        public Object call() throws Exception {

            logger.debug(String.format("%d: call api '%s' at (%c)'%s' with '%s' parameters",
                    yarConcurrentCallStack.getId(), yarConcurrentCallStack.getMethod(), (YarConcurrentClient.YAR_PROTOCOL_PERSISTENT > 0) ? 'p' : 'r', yarConcurrentCallStack.getUri(),yarConcurrentCallStack.getParamsString()));

            YarResponse yarResponse = null;

            YarRequest yarRequest = new YarRequest();
            yarRequest.setId(yarConcurrentCallStack.getId());
            yarRequest.setMethod(yarConcurrentCallStack.getMethod());
            yarRequest.setParameters(yarConcurrentCallStack.getParams());
            yarRequest.setPackagerName(yarConcurrentCallStack.getPackagerName());

            YarTransport yarTransport = YarTransportFactory.get(yarConcurrentCallStack.getTransport());
            yarTransport.open(yarConcurrentCallStack.getUri());

            try {
                yarResponse = yarTransport.exec(yarRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (yarConcurrentCallStack.getCallback() != null){
                assert yarResponse != null;
                return yarConcurrentCallStack.getCallback().setRetValue(yarResponse.getRetVal()).call();
            }
            return null;
        }
    }

}
