package net.mengkang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhoumengkang on 14/12/15.
 */
public class Api {
    protected final static Logger logger = LoggerFactory.getLogger(Api.class);

    public static void main(String[] args) {
        logger.info("111");
        logger.debug("222");
        logger.error("3333");
    }
}
