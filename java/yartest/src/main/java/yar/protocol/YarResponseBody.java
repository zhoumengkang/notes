package yar.protocol;

/**
 * typedef struct _yar_response {
 *      long id;
 *      int  status;
 *      zend_string *out;
 *      zval err;
 *      zval retval;
 * } yar_response_t;
 */
public class YarResponseBody {
    private long id;
    private int status;
    private String out;
    private String err;
    private String retval;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public String getRetval() {
        return retval;
    }

    public void setRetval(String retval) {
        this.retval = retval;
    }
}
