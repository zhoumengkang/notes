package net.mengkang;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhoumengkang on 14/12/15.
 */
public class inviteCode {

    public static void main(String[] args) {
        System.out.println((int) Math.pow(24, 6));
        System.out.println(createCode(1));
        System.out.println(codeRecover(createCode(1)));
        System.out.println(createCode(123));
        System.out.println(codeRecover(createCode(123)));
        System.out.println(createCode(123456789));
        System.out.println(codeRecover(createCode(123456789)));
    }

    public static final int offset = 24;
    public static final String stopChar = "Z";
    public static final int minCodeLen = 4;

    public static final Map<Integer,String> codeMap = new HashMap<Integer, String>(){{
        put(0,"A");
        put(1,"B");
        put(2,"C");
        put(3,"D");
        put(4,"E");
        put(5,"F");
        put(6,"G");
        put(7,"H");
        put(8,"I");
        put(9,"J");
        put(10,"K");
        put(11,"L");
        put(12,"M");
        put(13,"N");
        put(14,"P");
        put(15,"Q");
        put(16,"R");
        put(17,"S");
        put(18,"T");
        put(19,"U");
        put(20,"V");
        put(21,"W");
        put(22,"X");
        put(23,"Y");
    }};

    public static final Map<String,Integer> intMap = new HashMap<String,Integer>(){{
        put("A",0);
        put("B",1);
        put("C",2);
        put("D",3);
        put("E",4);
        put("F",5);
        put("G",6);
        put("H",7);
        put("I",8);
        put("J",9);
        put("K",10);
        put("L",11);
        put("M",12);
        put("N",13);
        put("P",14);
        put("Q",15);
        put("R",16);
        put("S",17);
        put("T",18);
        put("U",19);
        put("V",20);
        put("W",21);
        put("X",22);
        put("Y",23);
    }};

    /**
     * 根据 id 生成邀请码 6 位
     * 如果是 6 位的邀请码只能支持 191102976 1亿9千万用户
     * 我们自己的产品我想应该是够用了
     *
     * @param id
     * @return
     */
    public static String createCode(int id){
        String code = int2chars(id);
        if (code.length() < (minCodeLen - 1)){
            code = code + stopChar + codeTail(code);
        } else if (code.length() < minCodeLen){
            code = code + stopChar;
        }
        return code;
    }

    /**
     * 从邀请获取用户 id
     * @param code
     * @return
     */
    public static int codeRecover(String code){
        int len = code.indexOf(stopChar);
        if (len > 0) {
            code = code.substring(0,len);
        }
        return chars2int(code);
    }

    public static String codeTail(String code){
        String res = "";
        String lastChar = code.substring(code.length()-1,code.length());// 原code的尾数
        for (int i = 0; i < (minCodeLen - 1 - code.length()); i++) {
            res += lastChar;
        }
        return res;
    }

    /**
     * 用户 id
     * @param id
     * @return
     */
    public static String int2chars(int id){
        int div = id/offset;
        int remainder = id%offset;

        if (div == 0){
            return codeMap.get(id);
        } else if (div < offset) {
            return codeMap.get(div) + codeMap.get(remainder);
        } else {
            return int2chars(div) + codeMap.get(remainder);
        }
    }

    public static int chars2int(String chats){
        int res = 0;
        int codeLen = chats.length();
        for (int i = 0; i < codeLen; i++) {
            String a = chats.substring(i,i+1);
            if (intMap.containsKey(a)){
                res += intMap.get(a)*(Math.pow(offset,(codeLen-i-1)));
            }else{
                res = 0;
                break;
            }
        }
        return res;
    }
}
