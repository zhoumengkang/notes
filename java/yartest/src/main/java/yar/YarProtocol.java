package yar;

import yar.protocol.YarHeader;
import yar.protocol.YarResponse;
import yar.protocol.YarResponseBody;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by zhoumengkang on 5/12/15.
 */
public class YarProtocol {

    public static final int YAR_PROTOCOL_MAGIC_NUM = 0x80DFEC60;
    public static final int YAR_HEADER_LENGTH = 82;
    public static final int YAR_PACKAGER_NAME_LENGTH = 8;

    public static byte[] render(){
        return null;
    }

    public static YarHeader parse(byte[] content){
        YarHeader yarHeader = new YarHeader();

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(content));
        try {
            yarHeader.setId(in.readInt());
            yarHeader.setVersion(in.readShort());
            if (in.readInt() != YAR_PROTOCOL_MAGIC_NUM) {
                return null;
            }
            yarHeader.setReserved(in.readInt());

            char[] provider = new char[16];
            for (int i = 0; i < provider.length; i++) {
                provider[i] = in.readChar();
            }
            yarHeader.setProvider(provider);

            char[] token = new char[16];
            for (int i = 0; i < token.length; i++) {
                token[i] = in.readChar();
            }
            yarHeader.setToken(token);

            yarHeader.setBody_len(in.readInt());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return yarHeader;
    }

    public static YarResponse responseFetch(byte[] responseByte){
        YarResponseBody yarResponseBody = new YarResponseBody();
        YarResponse yarResponse = new YarResponse();

        byte[] header = new byte[YAR_HEADER_LENGTH];
        for (int i = 0; i < YAR_HEADER_LENGTH; i++) {
            header[i] = responseByte[i];
        }

        YarHeader yarHeader = YarProtocol.parse(header);
        Base.debugPrint(yarHeader);
        yarResponse.setYarHeader(yarHeader);

        byte[] packager = new byte[YAR_PACKAGER_NAME_LENGTH];
        for (int i = 0; i < YAR_PACKAGER_NAME_LENGTH; i++) {
            packager[i] = responseByte[YAR_HEADER_LENGTH + i];
            // 在这个8字节中后面的内容可能之前已经被占用，需要截取下
            if (packager[i] == 0){
                break;
            }
        }

        String packagerName = new String(packager);
        Base.debugPrint(packagerName);
        yarResponse.setPackagerName(packagerName);


        int off = YAR_HEADER_LENGTH + YAR_PACKAGER_NAME_LENGTH;
        int len = responseByte.length;

        byte[] retval = new byte[len];
        for (int i = off; i < len; i++) {
            retval[i - off] = responseByte[i];
        }

        yarResponseBody.setRetval(new String(retval));
        yarResponse.setYarResponseBody(yarResponseBody);

        return yarResponse;
    }


}
