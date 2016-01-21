package net.mengkang.demo.models;

/**
 * Created by zhoumengkang on 21/1/16.
 */
public class User {
    private int id;
    private String name;
    private int sex;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                '}';
    }

    public User(String name, int sex) {
        this.name = name;
        this.sex = sex;
    }

    public User() {
    }

}
