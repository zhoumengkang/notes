package yar.protocol;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by zhoumengkang on 5/12/15.
 */
public class YarProtocol {

    public static final int YAR_PROTOCOL_MAGIC_NUM = 0x80DFEC60;

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

}
