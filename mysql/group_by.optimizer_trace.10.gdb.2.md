# 背景

在 https://mengkang.net/1328.html 实验中，我们通过`optimizer_trace`发现`group by`会使用`intermediate_tmp_table`，而且里面的的`row_length`是20，抱着"打破砂锅问到底"的求学精神，所以想通过 gdb 调试源码的方式看这个`row_length`为什么是20.

通过`row_length`关键字，我定位到了mysql 5.7 源码里面的`sql/sql_tmp_table.cc`文件

![image.png](https://static.mengkang.net/upload/image/2019/0211/1549863245763990.png)

# 实际操作

## 查找 mysql pid
```bash
[root@localhost ~]# ps -ef|grep mysql
root      3739     1  0 09:36 ?        00:00:00 /bin/sh /usr/local/mysql/bin/mysqld_safe --datadir=/var/lib/mysql --pid-file=/var/lib/mysql/localhost.localdomain.pid
mysql     3894  3739  0 09:36 ?        00:00:01 /usr/local/mysql/bin/mysqld --basedir=/usr/local/mysql --datadir=/var/lib/mysql --plugin-dir=/usr/local/mysql/lib/plugin --user=mysql --log-error=/var/log/mariadb/mariadb.log --pid-file=/var/lib/mysql/localhost.localdomain.pid --socket=/var/lib/mysql/mysql.sock
root      3956  3940  0 09:48 pts/1    00:00:00 mysql -uroot -px xxxx
root      4002  3985  0 10:11 pts/2    00:00:00 grep --color=auto mysql
```
## 启动 gdb 
```bash
[root@localhost ~]# gdb
GNU gdb (GDB) Red Hat Enterprise Linux 7.6.1-114.el7
Copyright (C) 2013 Free Software Foundation, Inc.
License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.  Type "show copying"
and "show warranty" for details.
This GDB was configured as "x86_64-redhat-linux-gnu".
For bug reporting instructions, please see:
<http://www.gnu.org/software/gdb/bugs/>.
```
## attach pid 
```bash
(gdb) attach 3894
```

## 设置断点 

```bash
(gdb) b trace_tmp_table
Breakpoint 1 at 0x15e1eeb: file /root/newdb/mysql-server/sql/sql_tmp_table.cc, line 2300.
```
或者
```bash
(gdb) b /root/newdb/mysql-server/sql/sql_tmp_table.cc:2306
Breakpoint 1 at 0x15e1f8e: file /root/newdb/mysql-server/sql/sql_tmp_table.cc, line 2306.
```
## 客户端连接
```bash
[root@localhost ~]# mysql -uroot -p123456 test
mysql: [Warning] Using a password on the command line interface can be insecure.
```
阻塞中

## 打印 backtrace

```bash
(gdb) bt
#0  0x00007effcf4ec20d in poll () from /lib64/libc.so.6
#1  0x0000000001667759 in Mysqld_socket_listener::listen_for_connection_event (this=0x42e6360)
    at /root/newdb/mysql-server/sql/conn_handler/socket_connection.cc:852
#2  0x0000000000eb14fc in Connection_acceptor<Mysqld_socket_listener>::connection_event_loop (this=0x42ea0a0)
    at /root/newdb/mysql-server/sql/conn_handler/connection_acceptor.h:66
#3  0x0000000000ea8f7a in mysqld_main (argc=12, argv=0x41a60e8) at /root/newdb/mysql-server/sql/mysqld.cc:5149
#4  0x0000000000ea00ed in main (argc=8, argv=0x7ffece17c7e8) at /root/newdb/mysql-server/sql/main.cc:25
```

因为主进程在 poll 一直在等待客户端连接请求。

## 执行 continue

```bash
(gdb) c
Continuing.
```
此时，mysql 客户端则进入 mysql 命令行界面
```bash
[root@localhost ~]# mysql -uroot -p123456 test
mysql: [Warning] Using a password on the command line interface can be insecure.
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 3
Server version: 5.7.25-debug Source distribution

Copyright (c) 2000, 2019, Oracle and/or its affiliates. All rights reserved.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql>
```

## 客户端执行查询

```sql
SET optimizer_trace='enabled=on';
select aid,sum(pv) as num from article_rank force index(idx_day_aid_pv) where day>20181223 group by aid order by num desc LIMIT 10;
```

## 查看断点处信息

```bash
[Switching to Thread 0x7f7f20145700 (LWP 4392)]

Breakpoint 1, trace_tmp_table (trace=0x7eff94003088, table=0x7eff94937200) at /root/newdb/mysql-server/sql/sql_tmp_table.cc:2306
warning: Source file is more recent than executable.
2306	  trace_tmp.add("row_length",table->s->reclength).
(gdb) p table->s->reclength
$1 = 20
(gdb) p table->s->fields
$2 = 2
(gdb) p (*(table->field+0))->field_name
$3 = 0x7eff94010b0c "aid"
(gdb) p (*(table->field+1))->field_name
$4 = 0x7eff94007518 "num"
(gdb) p (*(table->field+0))->row_pack_length()
$5 = 4
(gdb) p (*(table->field+1))->row_pack_length()
$6 = 15
(gdb) p (*(table->field+0))->type()
$7 = MYSQL_TYPE_LONG
(gdb) p (*(table->field+1))->type()
$8 = MYSQL_TYPE_NEWDECIMAL
(gdb)
```
# 总结

原来和 `group by`没关系，只因为我 sql 种使用了`sum`行数，使得`num`字段类型是`MYSQL_TYPE_NEWDECIMAL`

> The SUM() and AVG() functions return a DECIMAL value for exact-value arguments (integer or DECIMAL), and a DOUBLE value for approximate-value arguments (FLOAT or DOUBLE). (Before MySQL 5.0.3, SUM() and AVG() return DOUBLE for all numeric arguments.)  

但是通过我们上面打印信息可以看到两个字段的长度加起来是`19`，而`reclength`是20。通过其他实验也发现`table->s->reclength`的长度就是`table->field`数组里面所有字段的字段长度和再加1。

# 附录
网站的 gdb 调试信息如下
```bash
[root@localhost ~]# gdb
GNU gdb (GDB) Red Hat Enterprise Linux 7.6.1-114.el7
Copyright (C) 2013 Free Software Foundation, Inc.
License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.  Type "show copying"
and "show warranty" for details.
This GDB was configured as "x86_64-redhat-linux-gnu".
For bug reporting instructions, please see:
<http://www.gnu.org/software/gdb/bugs/>.
(gdb) attach 3894
Attaching to process 3894
Reading symbols from /usr/local/mysql/bin/mysqld...done.
Reading symbols from /lib64/libpthread.so.0...(no debugging symbols found)...done.
[New LWP 4392]
[New LWP 4368]
[New LWP 4367]
[New LWP 4366]
[New LWP 4365]
[New LWP 4364]
[New LWP 4363]
[New LWP 4362]
[New LWP 4361]
[New LWP 4360]
[New LWP 4359]
[New LWP 4358]
[New LWP 4357]
[New LWP 4356]
[New LWP 4355]
[New LWP 4353]
[New LWP 4352]
[New LWP 4351]
[New LWP 4350]
[New LWP 4349]
[New LWP 4348]
[New LWP 4347]
[New LWP 4346]
[New LWP 4345]
[New LWP 4344]
[New LWP 4343]
[New LWP 4342]
[Thread debugging using libthread_db enabled]
Using host libthread_db library "/lib64/libthread_db.so.1".
Loaded symbols for /lib64/libpthread.so.0
Reading symbols from /lib64/libcrypt.so.1...(no debugging symbols found)...done.
Loaded symbols for /lib64/libcrypt.so.1
Reading symbols from /lib64/libdl.so.2...(no debugging symbols found)...done.
Loaded symbols for /lib64/libdl.so.2
Reading symbols from /lib64/librt.so.1...(no debugging symbols found)...done.
Loaded symbols for /lib64/librt.so.1
Reading symbols from /lib64/libstdc++.so.6...(no debugging symbols found)...done.
Loaded symbols for /lib64/libstdc++.so.6
Reading symbols from /lib64/libm.so.6...(no debugging symbols found)...done.
Loaded symbols for /lib64/libm.so.6
Reading symbols from /lib64/libgcc_s.so.1...(no debugging symbols found)...done.
Loaded symbols for /lib64/libgcc_s.so.1
Reading symbols from /lib64/libc.so.6...(no debugging symbols found)...done.
Loaded symbols for /lib64/libc.so.6
Reading symbols from /lib64/ld-linux-x86-64.so.2...(no debugging symbols found)...done.
Loaded symbols for /lib64/ld-linux-x86-64.so.2
Reading symbols from /lib64/libfreebl3.so...Reading symbols from /lib64/libfreebl3.so...(no debugging symbols found)...done.
(no debugging symbols found)...done.
Loaded symbols for /lib64/libfreebl3.so
Reading symbols from /lib64/libnss_files.so.2...(no debugging symbols found)...done.
Loaded symbols for /lib64/libnss_files.so.2
0x00007f7f2c32620d in poll () from /lib64/libc.so.6
Missing separate debuginfos, use: debuginfo-install glibc-2.17-260.el7.x86_64 libgcc-4.8.5-36.el7.x86_64 libstdc++-4.8.5-36.el7.x86_64 nss-softokn-freebl-3.36.0-5.el7_5.x86_64
(gdb) b /root/newdb/mysql-server/sql/sql_tmp_table.cc:2306
Breakpoint 1 at 0x15e1f8e: file /root/newdb/mysql-server/sql/sql_tmp_table.cc, line 2306.
(gdb) bt
#0  0x00007f7f2c32620d in poll () from /lib64/libc.so.6
#1  0x0000000001667759 in Mysqld_socket_listener::listen_for_connection_event (this=0x3841360)
    at /root/newdb/mysql-server/sql/conn_handler/socket_connection.cc:852
#2  0x0000000000eb14fc in Connection_acceptor<Mysqld_socket_listener>::connection_event_loop (this=0x38450a0)
    at /root/newdb/mysql-server/sql/conn_handler/connection_acceptor.h:66
#3  0x0000000000ea8f7a in mysqld_main (argc=12, argv=0x37010e8) at /root/newdb/mysql-server/sql/mysqld.cc:5149
#4  0x0000000000ea00ed in main (argc=8, argv=0x7ffdaf5bc2e8) at /root/newdb/mysql-server/sql/main.cc:25
(gdb) c
Continuing.
[Switching to Thread 0x7f7f20145700 (LWP 4392)]

Breakpoint 1, trace_tmp_table (trace=0x7f7ef8016b18, table=0x7f7ef8937380) at /root/newdb/mysql-server/sql/sql_tmp_table.cc:2306
warning: Source file is more recent than executable.
2306	  trace_tmp.add("row_length",table->s->reclength).
(gdb) p table->s->reclength
$1 = 20
(gdb) p table->s->fields
$2 = 2
(gdb) p (*(table->field+0))->field_name
$3 = 0x7eff94010b0c "aid"
(gdb) p (*(table->field+1))->field_name
$4 = 0x7eff94007518 "num"
(gdb) p (*(table->field+0))->row_pack_length()
$5 = 4
(gdb) p (*(table->field+1))->row_pack_length()
$6 = 15
(gdb) p (*(table->field+0))->type()
$7 = MYSQL_TYPE_LONG
(gdb) p (*(table->field+1))->type()
$8 = MYSQL_TYPE_NEWDECIMAL
(gdb)
```