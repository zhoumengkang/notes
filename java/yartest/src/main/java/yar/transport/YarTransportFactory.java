package yar.transport;

import yar.YarConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhoumengkang on 13/12/15.
 */
public class YarTransportFactory {

    public static Map<Integer,YarTransport> yarTransportMap;

    static{
        register();
    }

    public static void register(){
        yarTransportMap = new HashMap<>();
        yarTransportMap.put(YarConstants.YAR_CLIENT_PROTOCOL_HTTP,new HttpTransport());
        yarTransportMap.put(YarConstants.YAR_CLIENT_PROTOCOL_TCP,new SocketTransport());
        yarTransportMap.put(YarConstants.YAR_CLIENT_PROTOCOL_UDP,new SocketTransport());
        yarTransportMap.put(YarConstants.YAR_CLIENT_PROTOCOL_UNIX,new SocketTransport());
    }

    public static YarTransport get(int yarTransportType) {
        YarTransport yarTransport = yarTransportMap.get(yarTransportType);
        if (yarTransport == null) {
            String exception  = String.format("unsupported protocol %d", yarTransportType);
            throw new IllegalArgumentException(exception);
        } else {
            return yarTransport;
        }
    }

    public static YarTransport concurrentGet(int yarTransportType) {
        if (!yarTransportMap.containsKey(yarTransportType)) {
            String exception  = String.format("unsupported protocol %d", yarTransportType);
            throw new IllegalArgumentException(exception);
        }

        if (yarTransportType == YarConstants.YAR_CLIENT_PROTOCOL_HTTP) {
            return new HttpTransport();
        }else{
            return new SocketTransport();
        }
    }
}
