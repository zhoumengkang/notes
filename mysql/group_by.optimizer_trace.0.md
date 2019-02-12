# 背景

Mysql 版本 ：5.7
业务需求：需要统最近一个月阅读量最大的10篇文章
为了对比后面实验效果，我加了3个索引


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
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```
# 实验原理

> Optimizer Trace 是MySQL 5.6.3里新加的一个特性，可以把MySQL Optimizer的决策和执行过程输出成文本，结果为JSON格式，兼顾了程序分析和阅读的便利。

利用`performance_schema`库里面的`session_status`来统计`innodb`读取行数
利用`performance_schema`库里面的`optimizer_trace`来查看语句执行的详细信息

下面的实验都使用如下步骤来执行

```sql
#0. 如果前面有开启 optimizer_trace 则先关闭
SET optimizer_trace="enabled=off";

#1. 开启 optimizer_trace
SET optimizer_trace='enabled=on';

#2. 记录现在执行目标 sql 之前已经读取的行数
select VARIABLE_VALUE into @a from performance_schema.session_status where variable_name = 'Innodb_rows_read';

#3. 执行我们需要执行的 sql
todo

#4. 查询 optimizer_trace 详情
select trace from `information_schema`.`optimizer_trace`\G;

#5. 记录现在执行目标 sql 之后读取的行数
select VARIABLE_VALUE into @b from performance_schema.session_status where variable_name = 'Innodb_rows_read';
```
> 官方文档 https://dev.mysql.com/doc/internals/en/optimizer-tracing.html

# 实验

我做了四次实验，具体执行的第三步的 sql 如下

实验   | sql
------| ------
实验1  | select `aid`,sum(`pv`) as num from article_rank force index(idx_day_aid_pv)  where `day`>20190115 group by aid order by num desc LIMIT 10;
实验2  | select `aid`,sum(`pv`) as num from article_rank force index(idx_day)         where `day`>20190115 group by aid order by num desc LIMIT 10;
实验3  | select `aid`,sum(`pv`) as num from article_rank force index(idx_aid_day_pv)  where `day`>20190115 group by aid order by num desc LIMIT 10;
实验4  | select `aid`,sum(`pv`) as num from article_rank force index(PRI)             where `day`>20190115 group by aid order by num desc LIMIT 10;

## 实验1

```sql
mysql> select `aid`,sum(`pv`) as num from article_rank force index(idx_day_aid_pv) where `day`>'20190115' group by aid order by num desc LIMIT 10;
# 结果省略
10 rows in set (25.05 sec)
```

```json
{
  "steps": [
    {
      "join_preparation": "略"
    },
    {
      "join_optimization": "略"
    },
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
  ]
}
```
```sql
mysql> select VARIABLE_VALUE into @b from performance_schema.session_status where variable_name = 'Innodb_rows_read';
Query OK, 1 row affected (0.00 sec)

mysql> select @b-@a;
+---------+
| @b-@a   |
+---------+
| 6417027 |
+---------+
1 row in set (0.01 sec)
```

## 实验2

```sql
mysql> select `aid`,sum(`pv`) as num from article_rank force index(idx_day) where `day`>'20190115' group by aid order by num desc LIMIT 10;
# 结果省略
10 rows in set (42.06 sec)
```
```json
{
  "steps": [
    {
      "join_preparation": "略"
    },
    {
      "join_optimization": "略"
    },
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
  ]
}
```
```sql
mysql> select @b-@a;
+---------+
| @b-@a   |
+---------+
| 9625540 |
+---------+
1 row in set (0.00 sec)
```  

## 实验3

```sql
mysql> select `aid`,sum(`pv`) as num from article_rank force index(idx_aid_day_pv) where `day`>'20190115' group by aid order by num desc LIMIT 10;
# 省略结果
10 rows in set (5.38 sec)
```
```json
{
  "steps": [
    {
      "join_preparation": "略"
    },
    {
      "join_optimization": "略"
    },
    {
      "join_execution": {
        "select#": 1,
        "steps": [
          {
            "creating_tmp_table": {
              "tmp_table_info": {
                "table": "intermediate_tmp_table",
                "row_length": 20,
                "key_length": 0,
                "unique_constraint": false,
                "location": "memory (heap)",
                "row_limit_estimate": 838860
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
              "rows_estimate": 649101,
              "row_size": 24,
              "memory_available": 262144,
              "chosen": true
            },
            "filesort_execution": [
            ],
            "filesort_summary": {
              "rows": 11,
              "examined_rows": 649091,
              "number_of_tmp_files": 0,
              "sort_buffer_size": 352,
              "sort_mode": "<sort_key, rowid>"
            }
          }
        ]
      }
    }
  ]
}
```
```sql
mysql> select VARIABLE_VALUE into @b from performance_schema.session_status where variable_name = 'Innodb_rows_read';
Query OK, 1 row affected (0.00 sec)

mysql> select @b-@a;
+----------+
| @b-@a    |
+----------+
| 14146056 |
+----------+
1 row in set (0.00 sec)
```  

## 实验4
```sql
mysql> select `aid`,sum(`pv`) as num from article_rank force index(PRI) where `day`>'20190115' group by aid order by num desc LIMIT 10;# 省略查询结果
10 rows in set (21.90 sec)
```
```json
{
  "steps": [
    {
      "join_preparation": "略"
    },
    {
      "join_optimization": "略"
    },
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
  ]
}
```
```sql
mysql> select VARIABLE_VALUE into @b from performance_schema.session_status where variable_name = 'Innodb_rows_read';
Query OK, 1 row affected (0.00 sec)

mysql> select @b-@a;
+----------+
| @b-@a    |
+----------+
| 17354569 |
+----------+
1 row in set (0.00 sec)
```

## 实验1的原理详解

看下本案例中的 sql 去掉强制索引之后的语句
```sql
select `aid`,sum(`pv`) as num from article_rank where `day`>20190115 group by aid order by num desc LIMIT 10;
```
### 第一步
因为该 sql 中使用了 `group by`，所以我们看到`optimizer_trace`在执行时（`join_execution`）都会先创建一张临时表`creating_tmp_table`）来存放`group by`子句之后的结果。

> 存放的字段是`aid`和`num`两个字段。该临时表是如何存储的? row_length 为什么是 20? 另开三篇博客写了这个问题 
> https://mengkang.net/1334.html
> https://mengkang.net/1335.html
> https://mengkang.net/1336.html

### 第二步
因为`memory_table_size_exceeded`的原因，需要把临时表`intermediate_tmp_table`以`InnoDB`引擎存在磁盘。
```sql
mysql> show global variables like '%table_size';
+---------------------+----------+
| Variable_name       | Value    |
+---------------------+----------+
| max_heap_table_size | 16777216 |
| tmp_table_size      | 16777216 |
+---------------------+----------+
```

> https://dev.mysql.com/doc/refman/5.7/en/server-system-variables.html#sysvar_max_heap_table_size
> https://dev.mysql.com/doc/refman/5.7/en/server-system-variables.html#sysvar_tmp_table_size

> max_heap_table_size
> This variable sets the maximum size to which user-created MEMORY tables are permitted to grow. The value of the variable is used to calculate MEMORY table MAX_ROWS values. Setting this variable has no effect on any existing MEMORY table, unless the table is re-created with a statement such as CREATE TABLE or altered with ALTER TABLE or TRUNCATE TABLE. A server restart also sets the maximum size of existing MEMORY tables to the global max_heap_table_size value.

> tmp_table_size 
> The maximum size of internal in-memory temporary tables. This variable does not apply to user-created MEMORY tables.
> The actual limit is determined from whichever of the values of tmp_table_size and max_heap_table_size is smaller. If an in-memory temporary table exceeds the limit, MySQL automatically converts it to an on-disk temporary table. The internal_tmp_disk_storage_engine option defines the storage engine used for on-disk temporary tables.


也就是说这里临时表的限制是`16M`，而一行需要占的空间是20字节，那么最多只能容纳`floor(16777216/20) = 838860`行，所以`row_limit_estimate`是`838860`。

我们统计下`group by`之后的总行数。

```sql
mysql> select count(distinct aid) from article_rank where `day`>'20190115';
+---------------------+
| count(distinct aid) |
+---------------------+
|              649091 |
+---------------------+
```

```bash
649091 < 838860
```
> 问题：为什么会触发`memory_table_size_exceeded`呢？

数据写入临时表的过程如下：

在磁盘上创建临时表，表里有两个字段，`aid`和`num`，因为是 `group by aid`，所以`aid`是临时表的主键。
实验1中是扫描索引`idx_day_aid_pv`，依次取出叶子节点的`aid`和`pv`的值。
如果临时表种没有对应的 aid就插入，如果已经存在的 aid，则把需要插入行的 pv 累加在原来的行上。

### 第三步
对`intermediate_tmp_table`里面的`num`字段做`desc`排序


#### filesort_summary.examined_rows

排序扫描行数统计，我们统计下`group by`之后的总行数。（前面算过是649091）

所以每个实验的结果中`filesort_summary.examined_rows` 的值都是`649091`。
`filesort_summary.number_of_tmp_files`的值为0，表示没有使用临时文件来排序。

#### filesort_summary.sort_mode

MySQL 会给每个线程分配一块内存用于排序，称为`sort_buffer`。`sort_buffer`的大小由`sort_buffer_size`来确定。

```sql
mysql> show global variables like 'sort_buffer_size';
+------------------+--------+
| Variable_name    | Value  |
+------------------+--------+
| sort_buffer_size | 262144 |
+------------------+--------+
1 row in set (0.01 sec)
```  
也就说是`sort_buffer_size`默认值是`256KB`

> https://dev.mysql.com/doc/refman/5.6/en/server-system-variables.html#sysvar_sort_buffer_size
> Default Value (Other, 64-bit platforms, >= 5.6.4)	262144 

排序的方式也是有多种的

- <sort_key, rowid>
- <sort_key, additional_fields>
- <sort_key, packed_additional_fields>

##### additional_fields

1. 初始化`sort_buffer`，确定放入字段，因为我们这里是根据`num`来排序，所以`sort_key`就是`num`，`additional_fields`就是`aid`；
2. 把`group by` 子句之后生成的临时表（`intermediate_tmp_table`）里的数据（`aid`,`num`）存入`sort_buffer`。我们通过`number_of_tmp_files`值为0，知道内存是足够用的，并没有使用外部文件进行归并排序；
3. 对`sort_buffer`中的数据按`num`做快速排序；
4. 按照排序结果取前10行返回给客户端；

##### rowid

1. 根据索引或者全表扫描，按照过滤条件获得需要查询的排序字段值和row ID；
2. 将要排序字段值和row ID组成键值对，存入sort buffer中；
3. 如果sort buffer内存大于这些键值对的内存，就不需要创建临时文件了。否则，每次sort buffer填满以后，需要在内存中排好序（快排），并写到临时文件中；
4. 重复上述步骤，直到所有的行数据都正常读取了完成；
5. 用到了临时文件的，需要利用磁盘外部排序，将row id写入到结果文件中；
6. 根据结果文件中的row ID按序读取用户需要返回的数据。由于row ID不是顺序的，导致回表时是随机IO，为了进一步优化性能（变成顺序IO），MySQL会读一批row ID，并将读到的数据按排序字段顺序插入缓存区中(内存大小read_rnd_buffer_size)。

##### filesort_priority_queue_optimization

优先队列排序算法


# 实验结果分析

在看了附录中的实验结果之后，我汇总了一些比较重要的数据对比信息


指标   | index | query_time | filesort_summary.examined_rows | filesort_summary.sort_mode | filesort_priority_queue_optimization.rows_estimate | converting_tmp_table_to_ondisk | Innodb_rows_read  
------- | ------- | ------- | ------- | ------- | ------- | -------  | ------- 
实验1  |idx_day_aid_pv    | 25.05 | 649091 | additional_fields | 1057   | true  | 6417027  
实验2  |idx_day           | 42.06 | 649091 | additional_fields | 1057   | true  | 9625540  
实验3  |idx_aid_day_pv    | 5.38  | 649091 | rowid             | 649101 | false | 14146056 
实验4  |PRI               | 21.90 | 649091 | additional_fields | 1057   | true  | 17354569 

## filesort_summary.examined_rows

实验1案例中已经分析过。
```sql
mysql> select count(distinct aid) from article_rank where `day`>'20190115';
+---------------------+
| count(distinct aid) |
+---------------------+
|              649091 |
+---------------------+
```

## filesort_summary.sort_mode

同样的字段，同样的行数，为什么有的是`additional_fields`排序，有的是`rowid`排序

## filesort_priority_queue_optimization.rows_estimate

## converting_tmp_table_to_ondisk

是否创建临时表

## Innodb_rows_read

上面实验中每次在统计`@b-@a`的过程中，我们查询了`OPTIMIZER_TRACE`这张表，需要用到临时表，而 `internal_tmp_disk_storage_engine` 的默认值是 `InnoDB`。如果使用的是 `InnoDB` 引擎的话，把数据从临时表取出来的时候，会让 `Innodb_rows_read` 的值加 1。

我们先查询下面两个数据，下面需要使用到

```sql
mysql> select count(*) from article_rank;
+----------+
| count(*) |
+----------+
| 14146055 |
+----------+

mysql> select count(*) from article_rank where `day`>'20190115';
+----------+
| count(*) |
+----------+
|  3208513 |
+----------+
```

### 实验1
因为满足条件的总行数是`3208513`，因为使用的是`idx_day_aid_pv`索引，而查询的值是`aid`和`pv`，所以是覆盖索引，不需要进行回表。
但是可以看到在创建临时表（`creating_tmp_table`）之后，因为超过临时表内存限制（`memory_table_size_exceeded`），所以这`3208513`行数据的临时表会写入磁盘，使用的依然是`InnoDB`引擎。
所以实验1最后结果是 `3208513*2 + 1 = 6417027`；
### 实验2
相比实验1，实验2中不仅需要对临时表存盘，同时因为索引是`idx_day`，不能使用覆盖索引，还需要每行都回表，所以最后结果是 `3208513*3 + 1 = 9625540`；
### 实验3
实验3中因为最左列是`aid`，无法对`day>20190115`表达式进行过滤筛选，所以需要遍历整个索引（覆盖所有行的数据）。
但是本次过程中创建的临时表（memory 引擎）没有写入磁盘，都是在内存中操作，所以最后结果是`14146055 + 1 = 14146056`；
耗时也是最短的。

> 为什么实验3使用的是 rowid 排序而不是 additional_fields 排序？

我们说 additional_fields 对比 rowid 来说，减少了回表，也就减少了磁盘访问，会被优先选择。但是要注意这是对于 InnoDB 来说的。而实验3是内存表，使用的是 memory 引擎。回表过程只是根据数据行的位置，直接访问内存得到数据，不会有磁盘访问。排序的列越少越好占的内存就越小，所以就选择了 rowid 排序。

> 同样是写入 649091 到内存临时表，为什么其他三种方式都会出现内存不够用的情况呢？莫非其他三种情况是先把所有的行写入到临时表，再遍历合并？

### 实验4
实验4首先遍历主表，需要扫描`14146055`行，然后把符合条件的`3208513`行放入临时表 ，所以最后是`14146055 + 3208513 + 1 = 17354569`。


# 参考

> 《[MySQL实战45讲](https://time.geekbang.org/column/intro/139?code=PWgK7lHzTZ5V0oHA26aaVfiJUFaAwCk32BZwieZTUDg%3D)》
> [https://time.geekbang.org/column/article/73479](https://time.geekbang.org/column/intro/139?code=PWgK7lHzTZ5V0oHA26aaVfiJUFaAwCk32BZwieZTUDg%3D)
> [https://time.geekbang.org/column/article/73795](https://time.geekbang.org/column/intro/139?code=PWgK7lHzTZ5V0oHA26aaVfiJUFaAwCk32BZwieZTUDg%3D)
> https://dev.mysql.com/doc/refman/5.7/en/order-by-optimization.html
> https://juejin.im/entry/59019b428d6d810058b8488e