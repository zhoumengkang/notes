phpcms-modules-demo
===================

**PHPCMS模块开发的简单演示 结构如下:**
```
orders/
├── index.php				//该模块的前台控制器
├── install
│   ├── config.inc.php			//phpcms会自动调用里面的内容，作为该模块的信息描述
│   ├── extention.inc.php		//该模块在后台的操作菜单，至少得有CURD对应的方法，否则给非admin用户授权会有问题
│   ├── index.html			
│   ├── model.php			//通过该文件找到order.sql并执行里面的SQL
│   ├── module.sql			//该模块插入到module表里面的一条SQL
│   ├── orders.sql
│   └── templates			//里面存放的文件是前台模板文件,安装完后会复制到前台模板目录中去
│       ├── apply.html
│       ├── error404.html
│       └── show.html
├── orders.php				//该模块的后台控制器
├── templates				//该模块的后台模板文件存放目录
│   ├── orders_list.tpl.php
│   └── orders_view.tpl.php
└── uninstall
    ├── extention.inc.php
    ├── index.html
    ├── model.php
    └── orders.sql

```
**详细介绍地址:**
http://mengkang.net/215.html
