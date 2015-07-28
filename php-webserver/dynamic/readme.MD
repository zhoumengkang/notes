简单介绍
===
如果要运行该 demo 需要做如下操作 

1.修改 `config.php` 里的项目根目录 `web_root` 

2.编译`cgi-demo\user.c`，编译命令`gcc -o user.cgi user.c`，然后将 `user.cgi` 文件放入你配置的项目根目录下面 

3.在终端执行 `php start.php`  该 web 服务器就启动了 

4.通过[http://localhost:9003/user.cgi?id=1][1]就可以访问看到如下效果了

  [1]: http://localhost:9003/user.cgi?id=1
