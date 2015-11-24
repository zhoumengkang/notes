#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <signal.h>
#include <string.h>
    
#define SERV_PORT 8031
#define BUFSIZE 1024
#define BACKLOG 20

static void handle_fork(int lfd);

int main(void)
{
    int lfd, cfd;
    struct sockaddr_in serv_addr;
    
    pid_t pid[BACKLOG];
    char buf[BUFSIZE];
    int len;
    
    if((lfd = socket(AF_INET,SOCK_STREAM,0)) == -1){
        perror("create socket failed");
        exit(1);
    }
  
    int opt = 1;
    setsockopt(lfd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
      
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(SERV_PORT);
       
    if(bind(lfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1)
    {
        perror("bind error");
        exit(1);
    }
        
    if(listen(lfd, BACKLOG) == -1)
    {
        perror("listen error");
        exit(1);
    }
  
    signal(SIGCLD,SIG_IGN);
  
    for (int i = 0; i < BACKLOG; ++i)
    {
        pid[i] = fork();
        if (pid[i] == 0)
        {
            handle_fork(lfd);
        }
    }
   
    close(lfd);
       
    return 0;
}

static void handle_fork(int lfd)
{
    int cfd;
    struct sockaddr_in clin_addr;
    socklen_t clin_len;
    socklen_t num,len;
    char recvbuf[BUFSIZE],sendbuf[BUFSIZE],client_name[100];

    clin_len = sizeof(clin_addr);
    
    while(1)
    {
        if((cfd = accept(lfd, (struct sockaddr *)&clin_addr, &clin_len)) == -1)
        {
            perror("accept error");
            exit(1);
        }
        else
        {
            snprintf(client_name,100,"%s:%d",inet_ntoa(clin_addr.sin_addr),ntohs(clin_addr.sin_port));
            printf("you get a connection from %s\n", client_name);
        }

        while (num = recv(cfd,recvbuf,BUFSIZE,0))
        {
            recvbuf[num-1] = '\0';// 把客户端发送过来的最后的一个空格去掉
            printf("receive client (%s) message: %s\n", client_name,recvbuf);
            
            len = num-1;
            for (int i = 0; i < len; ++i)
            {
                sendbuf[i] = recvbuf[num -i -2];
            }
            sendbuf[len] = '\0';// 这样就不用执行 memset(sendbuf,0,sizeof(sendbuf));
            send(cfd,sendbuf,len,0);
        }

        printf("client (%s) exit\n",client_name);

        close(cfd);
    }

}
