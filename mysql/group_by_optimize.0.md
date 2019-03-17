# 一次不完整 group by order by 背后的性能问题分析

最近通过一个日志表做排行的时候发现特别卡，最后问题得到了解决，梳理一些索引和MySQL执行过程的经验，但是最后还是有**5个谜题没解开**，希望大家帮忙解答下

**主要包含如下知识点**

- 用数据说话证明慢日志的扫描行数到底是如何统计出来的
- 从 group by 执行原理找出优化方案 
- 排序的实现细节
- gdb 源码调试

## 背景
需要分别统计本月、本周被访问的文章的 TOP10。日志表如下

```sql
CREATE TABLE `article_rank` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `aid` int(11) unsigned NOT NULL,
  `pv` int(11) unsigned NOT NULL DEFAULT '1',
  `day` int(11) NOT NULL COMMENT '日期 例如 20171016',
  PRIMARY KEY (`id`),
  KEY `idx_day_aid_pv` (`day`,`aid`,`pv`),
  KEY `idx_aid_day_pv` (`aid`,`day`,`pv`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

## 准备工作
 
为了能够清晰的验证自己的一些猜想，在虚拟机里安装了一个 debug 版的 mysql，然后开启了慢日志收集，用于统计扫描行数

### 安装

- 下载源码
- 编译安装
- 创建 mysql 用户
- 初始化数据库
- 初始化 mysql 配置文件
- 修改密码

> 如果你兴趣，具体可以参考我的博客，一步步安装 https://mengkang.net/1335.html

### 开启慢日志

编辑配置文件，在`[mysqld]`块下添加
```bash
slow_query_log=1
slow_query_log_file=xxx
long_query_time=0
log_queries_not_using_indexes=1
```

## 性能分析

### 发现问题

假如我需要查询`2018-12-20` ~ `2018-12-24`这5天浏览量最大的10篇文章的 sql 如下，首先使用`explain`看下分析结果
```sql
mysql> explain select aid,sum(pv) as num from article_rank where day>=20181220 and day<=20181224 group by aid order by num desc limit 10;
+----+-------------+--------------+------------+-------+-------------------------------+----------------+---------+------+--------+----------+-----------------------------------------------------------+
| id | select_type | table        | partitions | type  | possible_keys                 | key            | key_len | ref  | rows   | filtered | Extra                                                     |
+----+-------------+--------------+------------+-------+-------------------------------+----------------+---------+------+--------+----------+-----------------------------------------------------------+
|  1 | SIMPLE      | article_rank | NULL       | range | idx_day_aid_pv,idx_aid_day_pv | idx_day_aid_pv | 4       | NULL | 404607 |   100.00 | Using where; Using index; Using temporary; Using filesort |
+----+-------------+--------------+------------+-------+-------------------------------+----------------+---------+------+--------+----------+-----------------------------------------------------------+
```

系统默认会走的索引是`idx_day_aid_pv`，根据`Extra`信息我们可以看到，使用`idx_day_aid_pv`索引的时候，会走覆盖索引，但是会使用临时表，会有排序。

实际执行 SQL1
```sql
# Time: 2019-03-17T03:02:27.984091Z
# User@Host: root[root] @ localhost []  Id:     6
# Query_time: 56.959484  Lock_time: 0.000195 Rows_sent: 10  Rows_examined: 1337315
SET timestamp=1552791747;
select aid,sum(pv) as num from article_rank where day>=20181220 and day<=20181224 group by aid order by num desc limit 10;
```

我们换另一个索引试试，后面称之为 SQL2
```sql
# Time: 2019-03-17T03:03:24.918073Z
# User@Host: root[root] @ localhost []  Id:     6
# Query_time: 4.406927  Lock_time: 0.000200 Rows_sent: 10  Rows_examined: 1337315
SET timestamp=1552791804;
select aid,sum(pv) as num from article_rank force index(idx_aid_day_pv) where day>=20181220 and day<=20181224 group by aid order by num desc limit 10;
```

扫描行数都是`1337315`，为什么`SQL1`执行消耗的时间上是`SQL2`的`13`倍呢？

## 为什么扫描行数是 1337315

我们查询两个数据，一个是满足条件的行数，一个是 group by 统计之后的行数。

```sql
mysql> select count(*) from article_rank where day>=20181220 and day<=20181224;
+----------+
| count(*) |
+----------+
|   785102 |
+----------+

mysql> select count(distinct aid) from article_rank where day>=20181220 and day<=20181224;
+---------------------+
| count(distinct aid) |
+---------------------+
|              552203 |
+---------------------+
```

我们发现`满足条件的总行数（785102）`+`group by 之后的总行数（552203）`+`limit 的值` = `慢日志里统计的 Rows_examined`。


要解答这个问题，就必须搞清楚上面两个 sql 到底分别都是如何运行的。

### SQL1 执行流程分析

为了便于理解，我按照索引的规则先模拟`idx_day_aid_pv`索引的一小部分数据

day      | aid      | pv      | id
-------- | -------- | ------- | -------  
20181220 | 1        | 23      | 1234  
20181220 | 3        | 2       | 1231
20181220 | 4        | 1       | 1212  
20181220 | 7        | 2       | 1221 
20181221 | 1        | 5       | 1257
20181221 | 10       | 1       | 1251 
20181221 | 11       | 8       | 1258 

因为索引`idx_day_aid_pv`最左列是`day`，所以当我们需要查找`20181220`~`20181224`之间的文章的pv总和的时候，我们需要遍历`20181220`~`20181224`这段数据的索引。


```sql
# 开启optimizer_trace
set optimizer_trace='enabled=on';
# 执行 sql 
select aid,sum(pv) as num from article_rank where day>=20181220 and day<=20181224 group by aid order by num desc limit 10;
# 查看 trace 信息
select trace from `information_schema`.`optimizer_trace`\G;
```
摘取里面最后的执行结果如下
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
          "examined_rows": 552203,
          "number_of_tmp_files": 0,
          "sort_buffer_size": 488,
          "sort_mode": "<sort_key, additional_fields>"
        }
      }
    ]
  }
}
```

通过`gdb`调试确认临时表上的字段是`aid`和`num`
```bash
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

#### 执行流程如下

1. 尝试在堆上使用`memory`的内存临时表来存放`group by`的数据，发现内存不够；
2. 创建一张临时表，临时表上有两个字段，`aid`和`num`字段（`sum(pv) as num`）；
3. 从索引`idx_day_aid_pv`中取出1行，插入临时表。插入规则是如果`aid`不存在则直接插入，如果存在，则把`pv`的值累加在`num`上；
4. 循环遍历索引`idx_day_aid_pv`上`20181220`~`20181224`之间的所有行，执行步骤3；
5. 对临时表根据`num`的值做优先队列排序；
6. 取出最后留在堆（优先队列的堆）里面的10行数据，作为结果集直接返回，不需要再回表；

> **补充说明优先队列排序执行步骤分析：**

1. 在临时表（未排序）中取出前 10 行，把其中的`num`和`aid`作为10个元素构成一个小顶堆，也就是最小的 num 在堆顶。
2. 取下一行，根据 num 的值和堆顶值作比较，如果该字大于堆顶的值，则替换掉。然后将新的堆做堆排序。
3. 重复步骤2直到第 552203 行比较完成。

### 使用 SQL_BIG_RESULT 优化

```sql
# Time: 2019-03-17T06:06:44.304555Z
# User@Host: root[root] @ localhost []  Id:     6
# Query_time: 6.144315  Lock_time: 0.000183 Rows_sent: 10  Rows_examined: 2122417
SET timestamp=1552802804;
select SQL_BIG_RESULT aid,sum(pv) as num from article_rank where day>=20181220 and day<=20181224 group by aid order by num desc limit 10;
```

扫描行数是 `2`x`满足条件的总行数（785102）`+`group by 之后的总行数（552203）`+`limit 的值`。

### SQL2 执行流程分析


为了便于理解，同样我也按照索引的规则先模拟`idx_aid_day_pv`索引的一小部分数据

aid      | day      | pv      | id
-------- | -------- | ------- | -------  
1        | 20181220 | 23      | 1234
1        | 20181221 | 5       | 1257
3        | 20181220 | 2       | 1231
3        | 20181222 | 22      | 1331
3        | 20181224 | 13      | 1431
4        | 20181220 | 1       | 1212  
7        | 20181220 | 2       | 1221 
10       | 20181221 | 1       | 1251 
11       | 20181221 | 8       | 1258 

为什么性能上比 SQL1 高了，很多呢，原因之一是`idx_aid_day_pv`索引上`aid`是确定有序的，那么执行`group by`的时候，则不会创建临时表，排序的时候才需要临时表。如果印证这一点呢，我们通过下面的执行计划就能看到

使用`idx_day_aid_pv`索引的效果：

```sql
mysql> explain select aid,sum(pv) as num from article_rank force index(idx_day_aid_pv) where day>=20181220 and day<=20181224 group by aid order by null limit 10;
+----+-------------+--------------+------------+-------+-------------------------------+----------------+---------+------+--------+----------+-------------------------------------------+
| id | select_type | table        | partitions | type  | possible_keys                 | key            | key_len | ref  | rows   | filtered | Extra                                     |
+----+-------------+--------------+------------+-------+-------------------------------+----------------+---------+------+--------+----------+-------------------------------------------+
|  1 | SIMPLE      | article_rank | NULL       | range | idx_day_aid_pv,idx_aid_day_pv | idx_day_aid_pv | 4       | NULL | 404607 |   100.00 | Using where; Using index; Using temporary |
+----+-------------+--------------+------------+-------+-------------------------------+----------------+---------+------+--------+----------+-------------------------------------------+
```
注意我上面使用了`order by null`表示强制对`group by`的结果不做排序。如果不加`order by null`，上面的 sql 则会出现`Using filesort`

使用`idx_aid_day_pv`索引的效果：

```sql
mysql> explain select aid,sum(pv) as num from article_rank force index(idx_aid_day_pv) where day>=20181220 and day<=20181224 group by aid order by null limit 10;
+----+-------------+--------------+------------+-------+-------------------------------+----------------+---------+------+------+----------+--------------------------+
| id | select_type | table        | partitions | type  | possible_keys                 | key            | key_len | ref  | rows | filtered | Extra                    |
+----+-------------+--------------+------------+-------+-------------------------------+----------------+---------+------+------+----------+--------------------------+
|  1 | SIMPLE      | article_rank | NULL       | index | idx_day_aid_pv,idx_aid_day_pv | idx_aid_day_pv | 12      | NULL |   10 |    11.11 | Using where; Using index |
+----+-------------+--------------+------------+-------+-------------------------------+----------------+---------+------+------+----------+--------------------------+
```

```sql
# 开启optimizer_trace
set optimizer_trace='enabled=on';
# 执行 sql 
select aid,sum(pv) as num from article_rank force index(idx_aid_day_pv) where day>=20181220 and day<=20181224 group by aid order by num desc limit 10;
# 查看 trace 信息
select trace from `information_schema`.`optimizer_trace`\G;
```
摘取里面最后的执行结果如下
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
          "rows_estimate": 552213,
          "row_size": 24,
          "memory_available": 262144,
          "chosen": true
        },
        "filesort_execution": [
        ],
        "filesort_summary": {
          "rows": 11,
          "examined_rows": 552203,
          "number_of_tmp_files": 0,
          "sort_buffer_size": 352,
          "sort_mode": "<sort_key, rowid>"
        }
      }
    ]
  }
}
```

#### 执行流程如下

1. 创建一张临时表，临时表上有两个字段，`aid`和`num`字段（`sum(pv) as num`）；
2. 读取索引`idx_aid_day_pv`中的一行，然后查看是否满足条件，如果`day`字段不在条件范围内（`20181220`~`20181224`之间），则读取下一行；如果`day`字段在条件范围内，则把`pv`值累加（不是在临时表中操作）； 
3. 读取索引`idx_aid_day_pv`中的下一行，如果`aid`与步骤1中一致且满足条件，则`pv`值累加（不是在临时表中操作）。如果`aid`与步骤1中不一致，则把之前的结果集写入临时表；
4. 循环执行步骤2、3，直到扫描完整个`idx_aid_day_pv`索引；
5. 对临时表根据`num`的值做优先队列排序；
6. 根据查询到的前10条的`rowid`回表（临时表）返回结果集。


> **补充说明优先队列排序执行步骤分析：**

1. 在临时表（未排序）中取出前 10 行，把其中的`num`和`rowid`作为10个元素构成一个小顶堆，也就是最小的 num 在堆顶。
2. 取下一行，根据 num 的值和堆顶值作比较，如果该字大于堆顶的值，则替换掉。然后将新的堆做堆排序。
3. 重复步骤2直到第 552203 行比较完成。

## 总结与疑问

1. SQL1 执行过程中，使用的是全字段排序最后不需要回表为什么总扫描行数还要加上10才对得上？
2. SQL1 与 SQL2 `group by`之后得到的行数都是`552203`，为什么会出现 SQL1 内存不够，里面还有哪些细节呢？
3. trace 信息里的`creating_tmp_table.tmp_table_info.row_limit_estimate`都是`838860`；计算由来是临时表的内存限制大小`16MB`，而一行需要占的空间是20字节，那么最多只能容纳`floor(16777216/20) = 838860`行，而实际我们需要放入临时表的行数是`785102`。为什么呢？
4. SQL1 使用`SQL_BIG_RESULT`优化之后，原始表需要扫描的行数会乘以2，背后逻辑是什么呢？为什么仅仅是不再尝试往内存临时表里写入这一步会相差10多倍的性能？
5. 通过源码看到 trace 信息里面很多扫描行数都不是实际的行数，既然是实际执行，为什么 trace 信息里不输出真实的扫描行数和容量等呢，比如`filesort_priority_queue_optimization.rows_estimate`在SQL1中的扫描行数我通过gdb看到计算规则如附录图 1
6. 有没有工具能够统计 SQL 执行过程中的 I/O 次数？

## 附录

![图1](https://mengkang.net/upload/image/2019/0220/1550634648559520.jpeg)




