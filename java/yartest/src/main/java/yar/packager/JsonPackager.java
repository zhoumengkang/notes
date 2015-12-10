package yar.packager;

import org.json.JSONObject;
import yar.protocol.YarRequestBody;
import yar.protocol.YarResponse;
import yar.protocol.YarResponseBody;

/**
 * Created by zhoumengkang on 4/12/15.
 */
public class JsonPackager extends YarPackager {
    @Override
    public byte[] pack(YarRequestBody yarRequestBody) {
        return new byte[0];
    }

    @Override
    public YarResponseBody unpack(YarResponse yarResponse) {
        if (!nameCheck(yarResponse.getPackagerName()).equals(YAR_PACKAGER_JSON)){
            return null;
        }

        JSONObject jsonObject = new JSONObject(new String(yarResponse.getYarResponseBody()));
        System.out.println(jsonObject);

        return null;
    }
}
