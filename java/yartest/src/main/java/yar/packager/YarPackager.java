package yar.packager;

import yar.Base;
import yar.protocol.YarHeader;
import yar.YarProtocol;
import yar.protocol.YarRequestBody;
import yar.protocol.YarResponse;
import yar.protocol.YarResponseBody;



/**
 * Created by zhoumengkang on 5/12/15.
 */
public class YarPackager {

    public static final String YAR_PACKAGER_PHP      = "PHP";
    public static final String 	YAR_PACKAGER_JSON    =  "JSON";
    public static final String 	YAR_PACKAGER_MSGPACK = "MSGPACK";


    public static byte[] pack(YarRequestBody yarRequestBody, String yarPackageType){
        return new byte[123];
    }

    public static YarResponse unpack(byte[] content){
        return null;
    }

}
