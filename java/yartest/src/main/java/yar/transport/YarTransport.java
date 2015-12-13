package yar.transport;


import yar.protocol.YarRequest;
import yar.protocol.YarResponse;

import java.io.IOException;

/**
 * Created by zhoumengkang on 12/12/15.
 */
public interface YarTransport {

    void open(String url);
    void send();
    YarResponse exec(YarRequest yarRequest) throws IOException;
    void calldata();
    void close();
}
