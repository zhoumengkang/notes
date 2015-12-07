package yar.protocol;

/**
 * Created by zhoumengkang on 7/12/15.
 */
public class YarRequest {
    private YarHeader yarHeader;
    private String packagerName;
    private YarRequestBody yarRequestBody;

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

    public YarRequestBody getYarRequestBody() {
        return yarRequestBody;
    }

    public void setYarRequestBody(YarRequestBody yarRequestBody) {
        this.yarRequestBody = yarRequestBody;
    }
}
