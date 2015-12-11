package yar;

import yar.packager.YarPackager;
import yar.protocol.YarRequest;
import yar.protocol.YarResponse;
import yar.protocol.YarResponseBody;

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
        this.packager = YarPackager.YAR_PACKAGER_JSON;
    }

    public static void setOpt(int opt,int value){

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
        yarRequest.setId(1234);
        yarRequest.setMethod(method);
        yarRequest.setParameters(args);
        yarRequest.setPackagerName(this.packager);

        byte[] res = new byte[0];
        try {
            res = sendPost(this.uri, YarProtocol.requestCreate(yarRequest));
        } catch (IOException e) {
            e.printStackTrace();
        }

        YarResponse yarResponse = YarProtocol.responseFetch(res);

        try {
            YarResponseBody yarResponseBody = YarPackager.get(yarResponse.getPackagerName()).unpack(yarResponse);
            return yarResponseBody.getRetVal();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
