package yar.concurrent.client;

/**
 * Created by zhoumengkang on 16/12/15.
 */
public class YarConcurrentTask {
    private int id;
    private String method;
    private Object[] params;

    public YarConcurrentTask(int id, String method, Object[] params) {
        this.id = id;
        this.method = method;
        this.params = params;
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
}
