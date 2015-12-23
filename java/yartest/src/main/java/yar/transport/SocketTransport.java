package yar.transport;

import yar.client.YarClientOptions;
import yar.protocol.YarRequest;
import yar.protocol.YarResponse;

import java.io.IOException;

/**
 * Created by zhoumengkang on 12/12/15.
 */
public class SocketTransport implements YarTransport {
    @Override
    public void open(String url,YarClientOptions yarClientOptions) {

    }

    @Override
    public void send() {

    }

    @Override
    public YarResponse exec(YarRequest yarRequest) throws IOException {
        return null;
    }

    @Override
    public void calldata() {

    }

    @Override
    public void close() {

    }
}
