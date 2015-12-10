package yar;

import yar.packager.JsonPackager;
import yar.packager.YarPackager;
import yar.protocol.YarRequestBody;
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

        YarRequestBody yarRequestBody = new YarRequestBody();
        yarRequestBody.setId(1234);
        yarRequestBody.setMethod(method);
        yarRequestBody.setParameters(args);
        // TODO
        byte[] send = new JsonPackager().pack(yarRequestBody);
        byte[] res = sendPost(this.uri, send);
        YarResponse yarResponse = YarProtocol.responseFetch(res);
        return null;
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
