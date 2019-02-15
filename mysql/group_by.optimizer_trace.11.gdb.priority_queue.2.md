## 背景

接着 https://mengkang.net/1328.html 的案例，我们继续磕。
上一篇 [GDB 调试 Mysql 实战（三）优先队列排序算法探究（上）](https://mengkang.net/1337.html) 分析了实验3中的`row_size`为什么是24。其他实验的`row_size`都是36，扫描行数也不符合预期。这篇就来探究下。

以实验1为例来分析
```sql
select `aid`,sum(`pv`) as num from article_rank force index(idx_day_aid_pv)  where `day`>20190115 group by aid order by num desc LIMIT 10;
```

`optimizer_trace.join_execution.steps`的结果如下

```json
{
  "join_execution": {
    "select#": 1,
    "steps": [
      {
        "creating_tmp_table": {
          "tmp_table_info": {
            "table": "intermediate_tmp_table",
            "row_length": 20,
            "key_length": 4,
            "unique_constraint": false,
            "location": "memory (heap)",
            "row_limit_estimate": 838860
          }
        }
      },
      {
        "converting_tmp_table_to_ondisk": {
          "cause": "memory_table_size_exceeded",
          "tmp_table_info": {
            "table": "intermediate_tmp_table",
            "row_length": 20,
            "key_length": 4,
            "unique_constraint": false,
            "location": "disk (InnoDB)",
            "record_format": "fixed"
          }
        }
      },
      {
        "filesort_information": [
          {
            "direction": "desc",
            "table": "intermediate_tmp_table",
            "field": "num"
          }
        ],
        "filesort_priority_queue_optimization": {
          "limit": 10,
          "rows_estimate": 1057,
          "row_size": 36,
          "memory_available": 262144,
          "chosen": true
        },
        "filesort_execution": [
        ],
        "filesort_summary": {
          "rows": 11,
          "examined_rows": 649091,
          "number_of_tmp_files": 0,
          "sort_buffer_size": 488,
          "sort_mode": "<sort_key, additional_fields>"
        }
      }
    ]
  }
}
```
##  row_size 为什么是 36

```bash
(gdb) b Sort_param::init_for_filesort
Breakpoint 1 at 0xf1a89f: file /root/newdb/mysql-server/sql/filesort.cc, line 107.
```

![image.png](https://static.mengkang.net/upload/image/2019/0215/1550228905770831.png)
 
```bash
(gdb) b Filesort::get_addon_fields
Breakpoint 2 at 0xf21231: file /root/newdb/mysql-server/sql/filesort.cc, line 2459.
(gdb) b /root/newdb/mysql-server/sql/filesort.cc:2496
Breakpoint 3 at 0xf212f9: file /root/newdb/mysql-server/sql/filesort.cc, line 2496.
(gdb) b /root/newdb/mysql-server/sql/filesort.cc:2523
Breakpoint 4 at 0xf2145f: file /root/newdb/mysql-server/sql/filesort.cc, line 2523.
```

![image.png](https://static.mengkang.net/upload/image/2019/0215/1550229618668980.png)

排序字段还是实验3一样是16字节，后面20字节则是两个字段相加20字节+ `(null_fields + 7) / 8` 一个可为空的字段，所以最后是36了。

## rows_estimate 为什么是 1057

```bash
(gdb) b /root/newdb/mysql-server/sql/filesort.cc:320
Breakpoint 5 at 0xf1b1d9: file /root/newdb/mysql-server/sql/filesort.cc, line 320.
...
Breakpoint 5, filesort (thd=0x7f0214000d80, filesort=0x7f021401f668, sort_positions=false, examined_rows=0x7f022804d050,
    found_rows=0x7f022804d048, returned_rows=0x7f022804d040) at /root/newdb/mysql-server/sql/filesort.cc:320
320	  num_rows= table->file->estimate_rows_upper_bound();
(gdb) s
ha_innobase::estimate_rows_upper_bound (this=0x7f0214022b50)
    at /root/newdb/mysql-server/storage/innobase/handler/ha_innodb.cc:13655
ha_innobase::estimate_rows_upper_bound (this=0x7f0214022b50)
    at /root/newdb/mysql-server/storage/innobase/handler/ha_innodb.cc:13655
warning: Source file is more recent than executable.
13655		DBUG_ENTER("estimate_rows_upper_bound");
(gdb) n
13661		update_thd(ha_thd());
(gdb) n
13663		TrxInInnoDB	trx_in_innodb(m_prebuilt->trx);
(gdb) n
13665		m_prebuilt->trx->op_info = "calculating upper bound for table rows";
(gdb) n
13667		index = dict_table_get_first_index(m_prebuilt->table);
(gdb) n
13669		ulint	stat_n_leaf_pages = index->stat_n_leaf_pages;
(gdb) p stat_n_leaf_pages
$19 = 139646902217632
(gdb) n
13671		ut_a(stat_n_leaf_pages > 0);
(gdb) p UNIV_PAGE_SIZE
No symbol "UNIV_PAGE_SIZE" in current context.
(gdb) n
13674			((ulonglong) stat_n_leaf_pages) * UNIV_PAGE_SIZE;
(gdb) n
13681		estimate = 2 * local_data_file_length
(gdb) p local_data_file_length
$20 = 16384
(gdb) p stat_n_leaf_pages
$21 = 1
(gdb) n
13682			/ dict_index_calc_min_rec_len(index);
(gdb) n
13684		m_prebuilt->trx->op_info = "";
(gdb) p estimate
$22 = 1057
(gdb) p dict_index_calc_min_rec_len(index)
$23 = 31
```

![image.png](https://static.mengkang.net/upload/image/2019/0215/1550231035164905.png)

也就是说`local_data_file_length`是16字节，为当前系统一个内存页大小。
`dict_index_calc_min_rec_len`注释中写道`Calculates the minimum record length in an index.`
`dict_index_calc_min_rec_len(index)`的值为31，太复杂了先不看了，懵逼了。