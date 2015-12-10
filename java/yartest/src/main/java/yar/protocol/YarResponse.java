package yar.protocol;

/**
 * Created by zhoumengkang on 7/12/15.
 */
public class YarResponse {
    private YarHeader yarHeader;
    private String packagerName;
    private byte[] yarResponseBody;

    public YarHeader getYarHeader() {
        return yarHeader;
    }

    public void setYarHeader(YarHeader yarHeader) {
        this.yarHeader = yarHeader;
    }

    public String getPackagerName() {
        return packagerName;
    }

    public void setPackagerName(String packagerName) {
        this.packagerName = packagerName;
    }

    public byte[] getYarResponseBody() {
        return yarResponseBody;
    }

    public void setYarResponseBody(byte[] yarResponseBody) {
        this.yarResponseBody = yarResponseBody;
    }
}
