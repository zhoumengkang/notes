package yar;

import yar.packager.YarPackager;
import yar.protocol.YarRequest;
import yar.protocol.YarResponse;

import java.io.*;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/**
 * Created by zhoumengkang on 2/12/15.
 */

public class YarClient {
    private String uri;
    private String packager;

    public static final char YAR_OPT_PACKAGER           = 1;
    public static final char YAR_OPT_PERSISTENT         = 2;
    public static final char YAR_OPT_TIMEOUT            = 4;
    public static final char YAR_OPT_CONNECT_TIMEOUT    = 8;

    protected HashMap<String,String> options;

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

        YarRequest yarRequest = new YarRequest();
        yarRequest.setId(123456789);
        yarRequest.setMethod(method);
        yarRequest.setParameters(args);
        yarRequest.setPackagerName(this.packager);

        byte[] res = null;
        try {
            res = sendPost(this.uri, YarProtocol.requestCreate(yarRequest));
        } catch (IOException e) {
            e.printStackTrace();
        }

        YarResponse yarResponse = YarProtocol.responseFetch(res);
        assert yarResponse != null;
        return yarResponse.getRetVal();
    }



    public static byte[] sendPost(String url, byte[] content) {
        OutputStream out = null;
        InputStream in = null;
        byte[] b = null;
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            out = conn.getOutputStream();
            out.write(content);
            out.flush();
            out.close();

            in = conn.getInputStream();
            b = new byte[in.available()];
            in.read(b);
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


        return b;
    }

}
