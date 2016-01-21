package net.mengkang.test;

import java.lang.reflect.Field;

/**
 * Created by zhoumengkang on 17/1/16.
 */
public class MysqlSelect<A> {
    public A get(String sql, A a, Object... params) {
        String[] selectFields = parseSelectFields(sql);

        Class   c      = a.getClass();
        Field[] fields = c.getDeclaredFields();

        for (int i = 0; i < selectFields.length; i++) {
            for (Field field : fields) {
                if (field.getName().equals(selectFields[i]) ||
                        (field.isAnnotationPresent(DbFiled.class) &&
                                field.getAnnotation(DbFiled.class).value().equals(selectFields[i]))
                        ) {

                    Class fieldClass = field.getType();
                    if (fieldClass == String.class) {
                        System.out.println(1);
                    } else if (fieldClass == int.class) {
                        System.out.println(2);
                    }
                }
            }
        }

        return null;
    }

    private void parseResultSet(Field field) {

    }

    private String[] parseSelectFields(String sql) {
        sql = sql.toLowerCase();

        String[] fieldArray = sql.substring(sql.indexOf("select") + 6, sql.indexOf("from")).split(",");
        int      length     = fieldArray.length;
        String[] fields     = new String[length];

        for (int i = 0; i < length; i++) {
            fields[i] = fieldArray[i].trim().replace("`", "");
        }

        return fields;
    }
}
