package yar.protocol;

import java.util.Arrays;

/**
 * typedef struct _yar_header {
 *      unsigned int   id;
 *      unsigned short version;
 *      unsigned int   magic_num;
 *      unsigned int   reserved;
 *      unsigned char  provider[32];
 *      unsigned char  token[32];
 *      unsigned int   body_len;
 *      }
 */

public class YarHeader {

    private int id;
    private short version;
    private int magic_num;
    private int reserved;
    private char[] provider = new char[32];
    private char[] token = new char[32];
    private int body_len;

    public YarHeader() {
        this.magic_num = YarProtocol.YAR_PROTOCOL_MAGIC_NUM;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public int getMagic_num() {
        return magic_num;
    }

    public void setMagic_num(int magic_num) {
        this.magic_num = magic_num;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    public char[] getProvider() {
        return provider;
    }

    public void setProvider(char[] provider) {
        this.provider = provider;
    }

    public char[] getToken() {
        return token;
    }

    public void setToken(char[] token) {
        this.token = token;
    }

    public int getBody_len() {
        return body_len;
    }

    public void setBody_len(int body_len) {
        this.body_len = body_len;
    }

    @Override
    public String toString() {
        return "YarHeader{" +
                "id=" + id +
                ", version=" + version +
                ", magic_num=" + magic_num +
                ", reserved=" + reserved +
                ", provider=" + Arrays.toString(provider) +
                ", token=" + Arrays.toString(token) +
                ", body_len=" + body_len +
                '}';
    }
}
