#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <fcntl.h>
#include <sys/select.h>
#include <sys/time.h>
#include <string.h>

#define SERV_PORT     8031
#define BUFSIZE       1024
#define FD_SET_SIZE   128

int main(void) {
    int lfd, cfd, maxfd, scokfd, retval;
    struct sockaddr_in serv_addr, clin_addr;

    socklen_t clin_len; // 地址信息结构体大小

    char recvbuf[BUFSIZE];
    int len;

    fd_set read_set, fd_set;

    int client[FD_SET_SIZE];
    int i;
    int maxi = -1;


    if ((lfd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
        perror("套接字描述符创建失败");
        exit(1);
    }

    int opt = 1;
    setsockopt(lfd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(SERV_PORT);

    if (bind(lfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) == -1) {
        perror("绑定失败");
        exit(1);
    }

    if (listen(lfd, FD_SET_SIZE) == -1) {
        perror("监听失败");
        exit(1);
    }

    maxfd = lfd + 1;

    for (i = 0; i < FD_SET_SIZE; ++i) {
        client[i] = -1;
    }

    FD_ZERO(&fd_set);
    FD_SET(lfd, &fd_set);

    while (1) {

        read_set = fd_set;

        for (i = 0; i < FD_SET_SIZE; ++i) {
            if (client[i] > 0) {
                FD_SET(client[i], &read_set);
            }
        }

        retval = select(maxfd, &read_set, NULL, NULL, 0);

        if (retval == -1) {
            perror("select 错误\n");
        } else if (retval == 0) {
            printf("超时\n");
            continue;
        }

        if (FD_ISSET(lfd, &read_set)) {
            clin_len = sizeof(clin_addr);
            if ((cfd = accept(lfd, (struct sockaddr *) &clin_addr, &clin_len)) == -1) {
                perror("接收错误\n");
                continue;
            }

            for (i = 0; i < FD_SET_SIZE; ++i) {
                if (client[i] < 0) {
                    client[i] = cfd;
                    FD_SET(cfd, &read_set);
                    printf("接收client[%d]一个请求来自于: %s:%d\n", i, inet_ntoa(clin_addr.sin_addr), ntohs(clin_addr.sin_port));
                    break;
                }
            }

            maxfd = (cfd > maxfd) ? (cfd + 1) : maxfd;
            maxi = (i > maxi) ? ++i : maxi;
        }

        for (i = 0; i <= maxi; ++i) {
            if (client[i] < 0) {
                continue;
            }

            if (FD_ISSET(client[i], &read_set)) {

                while (len = read(client[i], recvbuf, BUFSIZE)) {
                    //把客户端输入的内容输出在终端
                    write(STDOUT_FILENO, recvbuf, len);
                    // 只有当客户端输入 stop 就停止当前客户端的连接
                    if (strncasecmp(recvbuf, "stop", 4) == 0) {
                        close(client[i]);
                        printf("clinet[%d] 连接关闭\n", i);
                        FD_CLR(client[i], &read_set);
                        client[i] = -1;
                        break;
                    }
                }

                if (--retval <= 0) {
                    break;
                }
            }
        }

    }

    close(lfd);

    return 0;
}
