多个客户端并发给服务器端发送消息，服务器端给客户端返回反转的内容。

但是服务器没有对子进程数做限制，一旦并发高就出问题。

客户端终端
```
[zhoumengkang@localhost unix]$ gcc client.c -o client
[zhoumengkang@localhost unix]$ ./client 127.0.0.1
input string to server:zmk
server string return  :kmz
input string to server:haha
server string return  :ahah
input string to server:hei
server string return  :ieh
input string to server:
```
服务器终端
```
[zhoumengkang@localhost unix]$ gcc server.c -std=c99 -o server
[zhoumengkang@localhost unix]$ ./server 
you get a connection from 127.0.0.1:40362
receive client (127.0.0.1:40362) message: zmk
receive client (127.0.0.1:40362) message: haha
receive client (127.0.0.1:40362) message: hei
```
