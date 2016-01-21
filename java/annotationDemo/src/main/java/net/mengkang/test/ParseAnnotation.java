package net.mengkang.test;

/**
 * Created by zhoumengkang on 17/1/16.
 */
public class ParseAnnotation {
    public static void main(String[] args) {

        String sql = "select `id`, `name`, `register_ts` from user where id=?";

        MysqlSelect<UserLite> mysqlSelect = new MysqlSelect<UserLite>();

        mysqlSelect.get(sql,new UserLite(),1);
    }

}
