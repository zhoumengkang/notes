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

    protected HashMap<String,String> options;

    public YarClient(String uri){
        this.uri = uri;
        this.packager = YarPackager.YAR_PACKAGER_JSON;
    }


    public final Object useService(Class type) {
        YarClientInvocationHandler handler = new YarClientInvocationHandler(this);
        if (type.isInterface()) {
            return Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
        } else {
            return Proxy.newProxyInstance(type.getClassLoader(), type.getInterfaces(), handler);
        }
    }

    public String invoke(String method,Object[] args){

        YarRequest yarRequest = new YarRequest();
        yarRequest.setId(1234);
        yarRequest.setMethod(method);
        yarRequest.setParameters(args);

        byte[] send = YarPackager.pack(yarRequest,this.packager);
        byte[] res = sendPost(this.uri, send);
        YarResponse yarResponse = YarPackager.unpack(res);

        return yarResponse.getOut();
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
