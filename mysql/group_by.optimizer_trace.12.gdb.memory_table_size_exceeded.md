## 背景

接着 https://mengkang.net/1328.html 的案例，我们继续磕。

```sql
select aid,sum(pv) as num from article_rank force index(xxx) where day>20190115 group by aid order by num desc LIMIT 10;
```
同样是覆盖索引，为什么`idx_aid_day_pv`创建临时表的时候堆内存是足够的，而`idx_day_aid_pv`却出现内存不够的情况呢？

根据之前trace 里面的线索，带上我零基础的C++知识，加120分的自信和勇气，使用全局搜索大法，找到了如下代码

![image.png](https://static.mengkang.net/upload/image/2019/0308/1552027362500981.png)

直接看代码？不可能的。使用`gdb`帮我们看看实际的调用栈情况。

```bash
(gdb) b create_ondisk_from_heap
Breakpoint 1 at 0x15e2847: file /root/newdb/mysql-server/sql/sql_tmp_table.cc, line 2481.
```
执行sql
```sql
select aid,sum(pv) as num from article_rank force index(idx_day_aid_pv) where day>20181219 group by aid order by num desc limit 10;
```
查看调用栈
```bash
[Switching to Thread 0x7f65a02f0700 (LWP 4066)]

Breakpoint 1, create_ondisk_from_heap (thd=0x7f65b0014810, table=0x7f65b000c890, start_recinfo=0x7f65b000d728,
    recinfo=0x7f65b0932130, error=135, ignore_last_dup=false, is_duplicate=0x0)
    at /root/newdb/mysql-server/sql/sql_tmp_table.cc:2481
warning: Source file is more recent than executable.
2481	  TABLE new_table;
(gdb) bt
#0  create_ondisk_from_heap (thd=0x7f65b0014810, table=0x7f65b000c890, start_recinfo=0x7f65b000d728,
    recinfo=0x7f65b0932130, error=135, ignore_last_dup=false, is_duplicate=0x0)
    at /root/newdb/mysql-server/sql/sql_tmp_table.cc:2481
#1  0x00000000014f8483 in end_update (join=0x7f65b0931238, qep_tab=0x7f65b0931e00, end_of_records=false)
    at /root/newdb/mysql-server/sql/sql_executor.cc:3496
#2  0x00000000014fb3a9 in QEP_tmp_table::put_record (this=0x7f65b0932370, end_of_records=false)
    at /root/newdb/mysql-server/sql/sql_executor.cc:4640
#3  0x00000000014fc0ed in QEP_tmp_table::put_record (this=0x7f65b0932370)
    at /root/newdb/mysql-server/sql/sql_executor.h:248
#4  0x00000000014f2933 in sub_select_op (join=0x7f65b0931238, qep_tab=0x7f65b0931e00, end_of_records=false)
    at /root/newdb/mysql-server/sql/sql_executor.cc:1083
#5  0x00000000014f384f in evaluate_join_record (join=0x7f65b0931238, qep_tab=0x7f65b0931c88)
    at /root/newdb/mysql-server/sql/sql_executor.cc:1645
#6  0x00000000014f2c8f in sub_select (join=0x7f65b0931238, qep_tab=0x7f65b0931c88, end_of_records=false)
    at /root/newdb/mysql-server/sql/sql_executor.cc:1297
#7  0x00000000014f2510 in do_select (join=0x7f65b0931238) at /root/newdb/mysql-server/sql/sql_executor.cc:950
#8  0x00000000014f0477 in JOIN::exec (this=0x7f65b0931238) at /root/newdb/mysql-server/sql/sql_executor.cc:199
#9  0x000000000158942a in handle_query (thd=0x7f65b0014810, lex=0x7f65b0016b30, result=0x7f65b0003b78, added_options=0,
    removed_options=0) at /root/newdb/mysql-server/sql/sql_select.cc:184
#10 0x000000000153f251 in execute_sqlcom_select (thd=0x7f65b0014810, all_tables=0x7f65b0930990)
    at /root/newdb/mysql-server/sql/sql_parse.cc:5144
#11 0x0000000001538c7f in mysql_execute_command (thd=0x7f65b0014810, first_level=true)
    at /root/newdb/mysql-server/sql/sql_parse.cc:2816
#12 0x000000000154012b in mysql_parse (thd=0x7f65b0014810, parser_state=0x7f65a02ef690)
    at /root/newdb/mysql-server/sql/sql_parse.cc:5570
#13 0x0000000001535bba in dispatch_command (thd=0x7f65b0014810, com_data=0x7f65a02efdf0, command=COM_QUERY)
    at /root/newdb/mysql-server/sql/sql_parse.cc:1484
#14 0x0000000001534aee in do_command (thd=0x7f65b0014810) at /root/newdb/mysql-server/sql/sql_parse.cc:1025
#15 0x00000000016650ae in handle_connection (arg=0x3d5e550)
    at /root/newdb/mysql-server/sql/conn_handler/connection_handler_per_thread.cc:306
#16 0x000000000191e8f4 in pfs_spawn_thread (arg=0x32b6e80) at /root/newdb/mysql-server/storage/perfschema/pfs.cc:2190
#17 0x00007f65e648fdd5 in start_thread () from /lib64/libpthread.so.0
#18 0x00007f65e5356ead in clone () from /lib64/libc.so.6
(gdb)
```

![image.png](https://static.mengkang.net/upload/image/2019/0309/1552116871930946.png)