package net.mengkang.demo.dao;

import net.mengkang.demo.models.User;
import org.apache.ibatis.annotations.Select;

/**
 * Created by zhoumengkang on 21/1/16.
 */
public interface IUser {

    @Select("select * from user where id= #{id}")
    public User getUserByID(int id);
}
