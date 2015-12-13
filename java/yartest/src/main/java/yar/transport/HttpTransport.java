package yar.transport;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import yar.YarConfig;
import yar.YarProtocol;
import yar.protocol.YarRequest;
import yar.protocol.YarResponse;

import java.io.IOException;

/**
 * Created by zhoumengkang on 12/12/15.
 */
public class HttpTransport implements YarTransport{

    private String url;
    private CloseableHttpClient httpClient;
    private RequestConfig requestConfig;
    private CloseableHttpResponse httpResponse = null;

    @Override
    public void open(String url) {
        this.url = url;
        this.httpClient = HttpClients.createDefault();
        this.requestConfig = RequestConfig.custom()
                .setSocketTimeout(YarConfig.getInt("yar.timeout"))
                .setConnectTimeout(YarConfig.getInt("yar.connect.timeout")).build();


    }

    @Override
    public void send() {

    }

    @Override
    public YarResponse exec(YarRequest yarRequest) throws IOException {
        HttpPost httpPost = new HttpPost(this.url);
        httpPost.setConfig(this.requestConfig);
        httpPost.setEntity(new ByteArrayEntity(YarProtocol.requestCreate(yarRequest), ContentType.APPLICATION_FORM_URLENCODED));
        try {
            this.httpResponse = httpClient.execute(httpPost);
            if(httpResponse.getStatusLine().getStatusCode()==200){
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    return YarProtocol.responseFetch(EntityUtils.toByteArray(entity));
                }
            }
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            try {
                if (httpResponse != null){
                    httpResponse.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void calldata() {

    }

    @Override
    public void close() {

    }
}
