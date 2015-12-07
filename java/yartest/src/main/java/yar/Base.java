package yar;

/**
 * Created by zhoumengkang on 7/12/15.
 */
public class Base {

    public static boolean debug = true;

    public static void debugPrint(Object content) {
        if (debug){
            System.out.println("debug >>>\n" + content.toString() + "\n<<< debug\n");
        }
    }

}
