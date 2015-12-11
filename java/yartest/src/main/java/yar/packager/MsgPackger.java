package yar.packager;

import yar.protocol.YarRequestBody;
import yar.protocol.YarResponse;
import yar.protocol.YarResponseBody;

/**
 * Created by zhoumengkang on 11/12/15.
 */
public class MsgPackger extends YarPackager {
    @Override
    public byte[] pack(YarRequestBody yarRequestBody) {
        return new byte[0];
    }

    @Override
    public YarResponseBody unpack(YarResponse yarResponse) {
        return null;
    }
}
