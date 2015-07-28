简单介绍
===
在终端执行 `php start.php` 该 web 服务器就启动了

config.php 配置了项目的 web 目录和监听端口

如果要看到动态效果需要先编译`cgi-demo\user.c`，编译命令`gcc -o user.cgi user.c`，然后把 `user.cgi` 放入项目根目录

通过[http://localhost:9003/user.cgi?id=1][1]就可以访问了


  [1]: http://localhost:9003/user.cgi?id=1
