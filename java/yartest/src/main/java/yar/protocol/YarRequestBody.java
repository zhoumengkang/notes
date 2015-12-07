package yar.protocol;


/**
 * Created by zhoumengkang on 3/12/15.
 */


public class YarRequestBody {

    private long id;
    private String method;
    private Object[] parameters;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
