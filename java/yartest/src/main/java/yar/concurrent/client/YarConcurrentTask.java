package yar.concurrent.client;

import yar.YarClientException;
import yar.YarConstants;

/**
 * Created by zhoumengkang on 16/12/15.
 */
public class YarConcurrentTask {
    private int id;
    private String uri;
    private String method;
    private Object[] params;
    private int protocol;
    private String packagerName;
    private YarConcurrentCallback callback;

    public YarConcurrentTask(String uri, String method, Object[] params, String packagerName, YarConcurrentCallback callback) {
        this.uri = uri;
        this.method = method;
        this.params = params;
        this.packagerName = packagerName;
        this.callback = callback;

        if (uri.startsWith("http://") | uri.startsWith("https://")) {
            this.protocol = YarConstants.YAR_CLIENT_PROTOCOL_HTTP;
        } else if (uri.startsWith("tcp://")) {
            this.protocol = YarConstants.YAR_CLIENT_PROTOCOL_TCP;
        } else if (uri.startsWith("udp://")) {
            this.protocol = YarConstants.YAR_CLIENT_PROTOCOL_UDP;
        } else if (uri.startsWith("unix://")) {
            this.protocol = YarConstants.YAR_CLIENT_PROTOCOL_UNIX;
        } else {
            throw new YarClientException(String.format(YarClientException.unsupportedProtocolAddress,uri));
        }
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public String getPackagerName() {
        return packagerName;
    }

    public void setPackagerName(String packagerName) {
        this.packagerName = packagerName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getParams() {
        return params;
    }

    // debug
    public String getParamsString(){
        StringBuffer sb = new StringBuffer("[");
        int paramsLength = params.length;
        for (int i = 0; i < paramsLength; i++) {
            sb.append(params[i].toString());
            if (i < paramsLength - 1){
                sb.append(",");
            }else{
                sb.append("]");
            }
        }
        return sb.toString();
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public YarConcurrentCallback getCallback() {
        return callback;
    }

    public void setCallback(YarConcurrentCallback callback) {
        this.callback = callback;
    }
}
