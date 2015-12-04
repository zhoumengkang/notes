package yar.packager;

import org.json.JSONObject;
import yar.protocol.YarHeader;
import yar.protocol.YarRequest;
import yar.utils.PackUtils;

/**
 * Created by zhoumengkang on 4/12/15.
 */
public class JsonPackager {

    public static String pack(String method,Object[] args){
        return new StringBuffer().append(createPostYarHeader())
                .append(createJsonProtocol())
                .append(createPostBody(method, args)).toString();
    }

    public static String unpack(){
        return null;
    }

    public static String createPostBody(String method,Object[] args){

        YarRequest yarRequest = new YarRequest();
        yarRequest.setI(123456789);
        yarRequest.setM(method);
        yarRequest.setP(args);

        return new JSONObject(yarRequest).toString();
    }

    public static byte[] createPostYarHeader(){
        YarHeader yarHeader = new YarHeader();
        return PackUtils.pack(yarHeader.toString());
    }

    public static String createJsonProtocol(){
        return null;
    }
}
