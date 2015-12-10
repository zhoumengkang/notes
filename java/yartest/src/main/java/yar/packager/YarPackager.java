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
abstract public class YarPackager {

    public static final String YAR_PACKAGER_PHP      = "PHP";
    public static final String 	YAR_PACKAGER_JSON    =  "JSON";
    public static final String 	YAR_PACKAGER_MSGPACK = "MSGPACK";


    public abstract byte[] pack(YarRequestBody yarRequestBody);

    public abstract YarResponseBody unpack(YarResponse yarResponse);

    public String nameCheck(String packagerName){
        packagerName = packagerName.toLowerCase();

        if (packagerName.equals(YAR_PACKAGER_PHP.toLowerCase())
                ||packagerName.equals(YAR_PACKAGER_JSON.toLowerCase())
                ||packagerName.equals(YAR_PACKAGER_MSGPACK.toLowerCase())
                ){
            return packagerName;
        }else{
            System.out.printf("unsupported packager %s",packagerName);
            return null;
        }
    }

}
