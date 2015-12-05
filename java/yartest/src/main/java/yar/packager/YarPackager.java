package yar.packager;

import yar.protocol.YarHeader;
import yar.protocol.YarProtocol;
import yar.protocol.YarRequest;
import yar.protocol.YarResponse;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


/**
 * Created by zhoumengkang on 5/12/15.
 */
public class YarPackager {

    public static final String YAR_PACKAGER_PHP      = "PHP";
    public static final String 	YAR_PACKAGER_JSON    =  "JSON";
    public static final String 	YAR_PACKAGER_MSGPACK = "MSGPACK";

    public static final int HEADER_LENGTH = 82;
    public static final int PACKAGER_LENGTH = 8;

    public static byte[] pack(YarRequest yarRequest, String yarPackageType){
        return new byte[123];
    }

    public static YarResponse unpack(byte[] content){
        YarResponse yarResponse = new YarResponse();

        byte[] header = new byte[HEADER_LENGTH];
        for (int i = 0; i < HEADER_LENGTH; i++) {
            header[i] = content[i];
        }

        YarHeader yarHeader = YarProtocol.parse(header);
        if (yarHeader != null){
            System.out.println(yarHeader.toString());
        }

        byte[] packager = new byte[PACKAGER_LENGTH];
        for (int i = 0; i < PACKAGER_LENGTH; i++) {
            packager[i] = content[HEADER_LENGTH + i];
        }

        String packagerName = new String(packager);
        System.out.println(packagerName);




        int off = HEADER_LENGTH + PACKAGER_LENGTH;
        int len = content.length;

        byte[] retval = new byte[len];
        for (int i = off; i < len; i++) {
            retval[i - off] = content[i];
        }

        yarResponse.setRetval(new String(retval));

        return yarResponse;
    }

}
