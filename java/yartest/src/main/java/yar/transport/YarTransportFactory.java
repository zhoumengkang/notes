package yar.transport;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhoumengkang on 13/12/15.
 */
public class YarTransportFactory {

    public static final String YAR_TRANSPORT_HTTP      = "http";
    public static final String YAR_TRANSPORT_SOCKET    = "socket";

    public static Map<String,YarTransport> yarTransportMap;

    static{
        register();
    }

    public static void register(){
        yarTransportMap = new HashMap<>();
        yarTransportMap.put(YAR_TRANSPORT_HTTP.toLowerCase(),new HttpTransport());
        yarTransportMap.put(YAR_TRANSPORT_SOCKET.toLowerCase(),new SocketTransport());
    }

    public static YarTransport get(String yarTransportName) {
        YarTransport yarTransport = yarTransportMap.get(yarTransportName.toLowerCase());
        if (yarTransport == null) {
            String exception  = String.format("unsupported protocol %s", yarTransportName);
            throw new IllegalArgumentException(exception);
        } else {
            return yarTransport;
        }
    }
}
