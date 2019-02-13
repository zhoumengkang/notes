本篇文章关键字：优先队列排序算法、小顶堆、大顶堆

接着 https://mengkang.net/1328.html 的案例，我们继续磕。

回顾下实验3中的例子
```sql
select `aid`,sum(`pv`) as num from article_rank force index(idx_aid_day_pv) where `day`>'20190115' group by aid order by num desc limit 10;
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
```

> 关于这里的 filesort_priority_queue_optimization 算法可以参考 [https://blog.csdn.net/qian520ao/article/details/80531150](https://blog.csdn.net/qian520ao/article/details/80531150)

在该案例中根据该结果可知，临时表使用的堆上的 memory 表。根据 https://mengkang.net/1336.html 实验中 gdb 调试打印可知道，临时表存的两个字段是`aid`和`num`。

前面我们已经分析过对于 InnoDB 表来说 `additional_fields` 对比 `rowid` 来说，减少了回表，也就减少了磁盘访问，会被优先选择。但是要注意这是对于 InnoDB 来说的。而实验3是内存表，使用的是 memory 引擎。回表过程只是根据数据行的位置，直接访问内存得到数据，不会有磁盘访问（可以简单的理解为一个内存中的数组下标去找对应的元素），排序的列越少越好占的内存就越小，所以就选择了 rowid 排序。

还有一个原因就是我们这里使用了`limit 10`这样堆的成员个数比较小，所以占用的内存不会太大。不要忘了这里选择优先队列排序算法依然受到`sort_buffer_size`的限制。

优先队列排序执行步骤分析：

1. 在临时表（未排序）中取出前 10 行，把其中的`num`（来源于`sum(pv)`）和`rowid`作为10个元素构成一个小顶堆，也就是最小的 num 在堆顶。
2. 取下一行，根据 num 的值和堆顶值作比较，如果该字大于堆顶的值，则替换掉。然后将新的堆做堆排序。
3. 重复步骤2直到第 649091 行比较完成。
4. 然后对最后的10行做一次回表查询其 aid,num。

所以至此，rows_estimate 就是 649091 + 10 = 649101。实验3的结果与之相吻合，但是其他的都是 1057 行，是怎么算出来的呢？





