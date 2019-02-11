有这样一张表（索引比较多，是我测试用的）
```sql
CREATE TABLE `article_rank` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `aid` int(11) unsigned NOT NULL,
  `pv` int(11) unsigned NOT NULL DEFAULT '1',
  `day` int(11) NOT NULL COMMENT '日期 例如 20171016',
  PRIMARY KEY (`id`),
  KEY `idx_day` (`day`),
  KEY `idx_day_aid_pv` (`day`,`aid`,`pv`),
  KEY `idx_aid_day_pv` (`aid`,`day`,`pv`)
) ENGINE=InnoDB AUTO_INCREMENT=240776593 DEFAULT CHARSET=utf8
```
执行下面 sql
```sql
select `aid`,sum(`pv`) as num from article_rank force index(idx_day_aid_pv)  where `day`>20190115 group by aid order by num desc LIMIT 10;
```
根据 optimizer_trace 的结果
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
问题就是`intermediate_tmp_table`里面的 row_length 是怎么计算的，背后每行数据是如何存储的呢？

硬着头皮下载了 mysql 的源码，根据 trace 结果定位`sql_tmp_table.cc`里找到如下代码
![image.png](https://static.mengkang.net/upload/image/2019/0131/1548922453530740.png)

然后就是源码安装 mysql 开启 debug 模式
> https://mengkang.net/1335.html
> https://mengkang.net/1336.html
