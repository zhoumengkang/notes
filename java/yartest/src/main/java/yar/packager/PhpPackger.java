package yar.packager;

import yar.protocol.YarRequest;
import yar.protocol.YarResponse;
import yar.protocol.YarResponseBody;

/**
 * Created by zhoumengkang on 11/12/15.
 */
public class PhpPackger extends YarPackager {
    @Override
    public byte[] pack(YarRequest yarRequest) {
        return new byte[0];
    }

    @Override
    public YarResponseBody unpack(YarResponse yarResponse) {
        return null;
    }
}
