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
void process_cli(int connectfd,struct sockaddr_in client);

int main(void)
{
    int lfd, cfd;
    struct sockaddr_in serv_addr,clin_addr;
    socklen_t clin_len;
    pid_t pid;
    char buf[1024];
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
        
    if(listen(lfd, 128) == -1)
    {
        perror("listen error");
        exit(1);
    }
   
    clin_len = sizeof(clin_addr);
  
    signal(SIGCLD,SIG_IGN);
  
    while(1)
    {
          
        if((cfd = accept(lfd, (struct sockaddr *)&clin_addr, &clin_len)) == -1)
        {
            perror("accept error");
            exit(1);
        }
  
        pid = fork();
  
        if (pid > 0)
        {
            // 在父进程中关闭连接的套接字描述符，只是把 cfd 的引用数减少1，在子进程中还在使用 cfd
            close(cfd);
        }
        else if (pid == 0)
        {
            // 子进程关闭 lfd 处理任务，使其回到 TIME_WAIT 状态值
            close(lfd);
            process_cli(cfd,clin_addr);
            exit(0);
        }
        else
        {
            perror("fork error");
            exit(1);
        }

    }
   
    close(lfd);
       
    return 0;
}

void process_cli(int connectfd,struct sockaddr_in client)
{
    int num,len;
    char recvbuf[BUFSIZE],sendbuf[BUFSIZE],client_name[BUFSIZE],client_port[5];

    sprintf(client_name,"%s:%d",inet_ntoa(client.sin_addr),client.sin_port);

    printf("you get a connection from %s\n", client_name);

    while(num = recv(connectfd,recvbuf,BUFSIZE,0))
    {
        recvbuf[num-1] = '\0';
        printf("receive client (%s) message: %s\n", client_name,recvbuf);

        
        len = num-1;
        for (int i = 0; i < len; ++i)
        {
            sendbuf[i] = recvbuf[num -i -2];
        }
        sendbuf[len] = '\0';// 这样就不用执行 memset(sendbuf,0,sizeof(sendbuf));
        send(connectfd,sendbuf,len,0);
    }

    printf("client (%s) exit\n",client_name);

    close(connectfd);
}
