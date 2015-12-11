package yar.packager;

import org.json.JSONObject;
import yar.Base;
import yar.protocol.YarRequest;
import yar.protocol.YarResponse;
import yar.protocol.YarResponseBody;

import java.util.Map;

/**
 * Created by zhoumengkang on 4/12/15.
 */
public class JsonPackager extends YarPackager {
    @Override
    public byte[] pack(YarRequest yarRequest) {
        Map<String,Object> request = requestFormat(yarRequest);
        JSONObject jsonObject = new JSONObject(request);
        String string = jsonObject.toString();
        byte[] bytes = new byte[string.length()];
        return string.getBytes();
    }

    @Override
    public YarResponseBody unpack(YarResponse yarResponse) {

        JSONObject jsonObject = new JSONObject(new String(yarResponse.getYarResponseBody()));
        Base.debugPrint(jsonObject);

        YarResponseBody yarResponseBody = new YarResponseBody();
        yarResponseBody.setId(jsonObject.getLong("i"));
        yarResponseBody.setStatus(jsonObject.getInt("s"));
        yarResponseBody.setOut(jsonObject.getString("o"));
        yarResponseBody.setRetVal(jsonObject.get("r"));

        return yarResponseBody;
    }
}
