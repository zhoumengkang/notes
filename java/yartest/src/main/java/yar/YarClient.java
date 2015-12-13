package yar;

import yar.protocol.YarRequest;
import yar.protocol.YarResponse;
import yar.transport.YarTransport;
import yar.transport.YarTransportFactory;

import java.io.*;
import java.lang.reflect.Proxy;

/**
 * Created by zhoumengkang on 2/12/15.
 */

public class YarClient {
    private String uri;
    private String packager;

    public YarClient(String uri){
        this.uri = uri;
        this.packager = YarConfig.getString("yar.packager");
    }

    public final Object useService(Class type) {
        YarClientInvocationHandler handler = new YarClientInvocationHandler(this);
        if (type.isInterface()) {
            return Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
        } else {
            return Proxy.newProxyInstance(type.getClassLoader(), type.getInterfaces(), handler);
        }
    }

    public Object invoke(String method,Object[] args){

        YarResponse yarResponse = null;

        YarRequest yarRequest = new YarRequest();
        yarRequest.setId(123456789);
        yarRequest.setMethod(method);
        yarRequest.setParameters(args);
        yarRequest.setPackagerName(this.packager);

        YarTransport yarTransport = YarTransportFactory.get(YarConfig.getString("yar.transport"));
        yarTransport.open(this.uri);

        try {
            yarResponse = yarTransport.exec(yarRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert yarResponse != null;
        return yarResponse.getRetVal();

    }

    /**
     * 调试用
     * @param params
     */
    public static void debug(Object... params) {
        if (YarConfig.getBoolean("yar.debug")){

            StackTraceElement[] steArray = Thread.currentThread().getStackTrace();

            StringBuilder sb = new StringBuilder("YAR_DEBUG ");

            sb.append(steArray[2].getClassName()).append(" line: ").append(steArray[2].getLineNumber());

            for (int i = 0,len = params.length; i < len; i++) {
                sb.append(" : ").append(params[i].toString());
            }

            sb.append("\n");

            System.out.println(sb.toString());
        }
    }

}
