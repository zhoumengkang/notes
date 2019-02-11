## 下载源码
```bash
git clone https://github.com/mysql/mysql-server.git
cd mysql-server
git checkout 5.7
```

## 编译安装

安装依赖
```bash
yum install -y cmake make gcc gcc-c++ ncurses-devel bison gdb
```
需要注意的一点，需要指定 boost 路径，会 cmake 的时候自动下载
```bash
cd BUILD; 
cmake .. -DDOWNLOAD_BOOST=1 -DWITH_BOOST=<directory> -DWITH_DEBUG=1 -DWITH_UNIT_TESTS=off
make 
make install
```

最后程序安装到了`/usr/local/mysql`目录

## 创建专用用户
```bash
groupadd mysql
useradd -s /sbin/nologin -M -g mysql mysql
```


初始化数据库
```bash
cd /usr/local/mysql/
bin/mysqld --defaults-file=/etc/my.cnf --initialize --user=mysql

2019-02-01T07:45:58.147032Z 1 [Note] A temporary password is generated for root@localhost: jss<swtX.8og
```
连接数据库
```bash
[root@bogon bin]# ./mysql -h localhost -uroot
ERROR 2002 (HY000): Can't connect to local MySQL server through socket '/tmp/mysql.sock' (2)
```
原来是因为配置文件里面没有置顶客户端的 socket 文件
```bash
cat /etc/my.cnf
[mysqld]
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock
# Disabling symbolic-links is recommended to prevent assorted security risks
symbolic-links=0
# Settings user and group are ignored when systemd is used.
# If you need to run mysqld under a different user or group,
# customize your systemd unit file for mariadb according to the
# instructions in http://fedoraproject.org/wiki/Systemd

[mysqld_safe]
log-error=/var/log/mariadb/mariadb.log
pid-file=/var/run/mariadb/mariadb.pid

#
# include all files from the config directory
#
!includedir /etc/my.cnf.d
```
增加
```bash
[client]
default-character-set=utf8
socket=/var/lib/mysql/mysql.sock
[mysql]
default-character-set=utf8
socket=/var/lib/mysql/mysql.sock
```
再次连接就 ok 了。

## 修改默认密码
```bash
SET PASSWORD = PASSWORD('123456');
ALTER USER 'root'@'localhost' PASSWORD EXPIRE NEVER;
flush privileges;
```

## 导入测试数据
```bash
/usr/local/mysql/bin/mysql -uroot -p123456 test < article_rank.sql
```

> https://www.linuxidc.com/Linux/2018-05/152246.htm
> https://www.cnblogs.com/debmzhang/p/5013540.html

