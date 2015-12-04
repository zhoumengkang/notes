package yar.protocol;

/**
 * Created by zhoumengkang on 3/12/15.
 */

public class YarHeader {

    private int id;
    private short version;
    private int magic_num;
    private int reserved;
    private char[] provider = new char[32];
    private char[] token = new char[32];
    private char body_len;

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

    public char getBody_len() {
        return body_len;
    }

    public void setBody_len(char body_len) {
        this.body_len = body_len;
    }
}
