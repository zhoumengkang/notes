package yar;

import yar.protocol.YarHeader;
import yar.protocol.YarRequest;
import yar.protocol.YarResponse;
import yar.protocol.YarResponseBody;

import java.io.*;

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

            yarHeader.setBodyLen(in.readInt());

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

        YarResponse yarResponse = new YarResponse();

        byte[] header = new byte[YAR_HEADER_LENGTH];
        for (int i = 0; i < YAR_HEADER_LENGTH; i++) {
            header[i] = responseByte[i];
        }

        YarHeader yarHeader = YarProtocol.parse(header);
        yarResponse.setYarHeader(yarHeader);

        byte[] packager = new byte[YAR_PACKAGER_NAME_LENGTH];
        int packagerLength = 0;
        for (int i = 0; i < YAR_PACKAGER_NAME_LENGTH; i++) {
            packager[i] = responseByte[YAR_HEADER_LENGTH + i];
            // 在这个8字节中，当是 php 或者是 json 的时候，后面的三个或者四个字节可能之前已经被占用，需要截取下
            if (packager[i] == 0){
                packagerLength = i;
                break;
            }
        }

        String packagerName = new String(packager);
        Base.debugPrint(packagerName);
        yarResponse.setPackagerName(packagerName.substring(0,packagerLength));


        int off = YAR_HEADER_LENGTH + YAR_PACKAGER_NAME_LENGTH;
        int len = responseByte.length;

        byte[] yarResponseBody = new byte[len];
        for (int i = off; i < len; i++) {
            yarResponseBody[i - off] = responseByte[i];
        }

        yarResponse.setYarResponseBody(yarResponseBody);

        return yarResponse;
    }

    public static byte[] requestCreate(YarRequest yarRequest) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteArrayOutputStream);

        YarHeader yarHeader = yarRequest.getYarHeader();

        try {
            out.writeInt(yarHeader.getId());
            out.writeShort(yarHeader.getVersion());
            out.writeInt(yarHeader.getMagicNum());
            out.writeInt(yarHeader.getReserved());

            for (char aProvider : yarHeader.getProvider()) {
                out.writeChar(aProvider);
            }

            for (char aToken : yarHeader.getToken()) {
                out.writeChar(aToken);
            }

            out.writeInt(yarHeader.getBodyLen());
            return byteArrayOutputStream.toByteArray();
        } finally {
            byteArrayOutputStream.close();
            out.close();
        }
    }

}
