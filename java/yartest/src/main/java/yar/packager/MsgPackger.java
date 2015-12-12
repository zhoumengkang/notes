package yar.packager;

import yar.protocol.YarRequest;
import yar.protocol.YarResponse;

/**
 * Created by zhoumengkang on 11/12/15.
 */
public class MsgPackger extends YarPackager {
    @Override
    public byte[] pack(YarRequest yarRequest) {
        return new byte[0];
    }

    @Override
    public YarResponse unpack(byte[] content) {
        return null;
    }
}
