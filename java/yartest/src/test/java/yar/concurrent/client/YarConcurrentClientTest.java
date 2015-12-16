package yar.concurrent.client;

import junit.framework.TestCase;

/**
 * Created by zhoumengkang on 16/12/15.
 */
public class YarConcurrentClientTest extends TestCase {

    public void testLoop() throws Exception {
        YarConcurrentClient.call(new YarConcurrentTask(1,"support",new Object[]{1,2}));
        YarConcurrentClient.call(new YarConcurrentTask(2,"support",new Object[]{3,3}));
        YarConcurrentClient.call(new YarConcurrentTask(3,"support",new Object[]{2,4}));
        YarConcurrentClient.call(new YarConcurrentTask(4,"support",new Object[]{2,10}));
        YarConcurrentClient.loop();
        YarConcurrentClient.reset();
        YarConcurrentClient.call(new YarConcurrentTask(5, "support", new Object[]{10, 10}));
        YarConcurrentClient.loop();
    }
}