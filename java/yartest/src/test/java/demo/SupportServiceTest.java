package demo;

import junit.framework.TestCase;

/**
 * Created by zhoumengkang on 3/12/15.
 */
public class SupportServiceTest extends TestCase {
    public void testName() throws Exception {
        byte[] packager = new byte[8];
        packager[0] = 'j';
        packager[1] = 's';
        packager[2] = 'o';
        packager[3] = 'n';
        String packagerName = new String(packager);
        System.out.println(packagerName);
        System.out.println(packagerName.length());

        if (packagerName.equals("json")){
            System.out.println(1);
        }else{
            System.out.println(2);
        }

        packagerName = packagerName.substring(0,4);

        System.out.println(packagerName);
        System.out.println(packagerName.length());

        if (packagerName.equals("json")){
            System.out.println(1);
        }else{
            System.out.println(2);
        }

    }
}