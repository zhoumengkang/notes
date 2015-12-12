package yar.packager;

import org.json.JSONObject;
import yar.YarClient;
import yar.protocol.YarRequest;
import yar.protocol.YarResponse;

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
        return string.getBytes();
    }

    @Override
    public YarResponse unpack(byte[] content) {
        JSONObject jsonObject = new JSONObject(new String(content));
        YarClient.debug(jsonObject);
        YarResponse yarResponse = new YarResponse();
        yarResponse.setId(jsonObject.getLong("i"));
        yarResponse.setStatus(jsonObject.getInt("s"));
        yarResponse.setOut(jsonObject.getString("o"));
        yarResponse.setRetVal(jsonObject.get("r"));

        return yarResponse;
    }
}
