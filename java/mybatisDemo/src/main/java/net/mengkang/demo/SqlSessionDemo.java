package net.mengkang.demo;

import net.mengkang.demo.models.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class SqlSessionDemo {
    public static void main(String[] args) {
        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        SqlSession session1 = sqlSessionFactory.openSession();
        try {
            User user = new User("mengkang", 1);
            int count = session1.insert("net.mengkang.mappers.user.add", user);
            session1.commit();
            System.out.println(user.getId()); // 这样即可获取刚刚插入的 id，线程安全吗？
        } finally {
            session1.close();
        }

        SqlSession session2 = sqlSessionFactory.openSession();
        try {
            User user = session2.selectOne("net.mengkang.mappers.user.getUserByID", 1);
            if (user != null){
                System.out.println(user.toString());
            }
        } finally {
            session2.close();
        }


    }
}
