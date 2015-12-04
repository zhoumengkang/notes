package yar;

import java.io.*;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/**
 * Created by zhoumengkang on 2/12/15.
 */

public class YarClient {
    protected String uri;
    protected HashMap<String,String> options;

    public YarClient(String uri){
        this.uri = uri;
    }

    public void setOpt(String key,String value){

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

        String content = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(new File("./test.log"));
            byte[] b = new byte[fileInputStream.available()];
            fileInputStream.read(b);
            fileInputStream.close();
            content = new String(b);
            System.out.println(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sendPost(uri, content);
    }

    public static String sendPost(String url, String content) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            out = new OutputStreamWriter(conn.getOutputStream());

            out.write(content);
            out.flush();
            out.close();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (result.length() > 0){
                    result += "\n" + line;
                }
                result += line;
            }

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

        return result;
    }

}
