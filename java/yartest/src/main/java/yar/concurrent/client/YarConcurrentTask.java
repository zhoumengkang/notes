package yar.concurrent.client;

/**
 * Created by zhoumengkang on 16/12/15.
 */
public class YarConcurrentTask {
    private int id;
    private String uri;
    private String method;
    private Object[] params;
    private String transport;
    private String packagerName;
    private YarConcurrentCallback callback;

    public YarConcurrentTask(String uri, String method, Object[] params,String transport, String packagerName, YarConcurrentCallback callback) {
        this.uri = uri;
        this.method = method;
        this.params = params;
        this.transport = transport;
        this.packagerName = packagerName;
        this.callback = callback;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
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
