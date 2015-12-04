package yar;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by zhoumengkang on 3/12/15.
 */
final class YarClientInvocationHandler implements InvocationHandler {

    private YarClient yarClient;

    YarClientInvocationHandler(YarClient yarClient) {
        this.yarClient = yarClient;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return yarClient.invoke(method.getName(), args);
    }
}
