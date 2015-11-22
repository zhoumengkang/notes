#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <string.h>
#include <netdb.h>
  
#define SERV_PORT 8031
#define BUFSIZE	100
void process(FILE * fp, int sockfd);
char * getMessage(char * sendline,int len,FILE * fp);
 
int main(int argc,char const * argv[])
{
    int fd;
    struct hostent * he;
    struct sockaddr_in server;

    if (argc != 2)
    {
    	printf("usage %s <IP Adrress>", argv[0]);
    	exit(1);
    }

    if ((he = gethostbyname(argv[1])) == NULL)
    {
    	perror("gethostbyname error");
    	exit(1);
    }

    if((fd = socket(AF_INET,SOCK_STREAM,0)) == -1){
        perror("create socket failed");
        exit(1);
    }
    
    memset(&server, 0, sizeof(server));
    server.sin_family = AF_INET;
    server.sin_port = htons(SERV_PORT);
    server.sin_addr = *((struct in_addr *) he->h_addr);

    if(connect(fd, (struct sockaddr *)&server, sizeof(server)) == -1)
    {
        perror("connect error");
        exit(1);
    }

    process(stdin,fd);

    close(fd);

    return 0;
}

void process(FILE * fp, int sockfd)
{
	char sendline[BUFSIZE], recvline[BUFSIZE];
	int numbytes;

	printf("connect to server\ninput name :");

	if (fgets(sendline,BUFSIZE,fp) == NULL)
	{
		printf("\nexit.\n");
		return;
	}

	send(sockfd,sendline,strlen(sendline),0);

	while(getMessage(sendline,BUFSIZE,fp) != NULL)
	{
		send(sockfd,sendline,strlen(sendline),0);

		if (numbytes = recv(sockfd,recvline,BUFSIZE,0) == 0)
		{
			printf("server terminated\n");
			return;
		}

		recvline[numbytes] = '\0';
		printf("server message%s\n",recvline);
	}

	printf("\nexit.\n");
}


char * getMessage(char * sendline,int len,FILE * fp)
{
	printf("input string to server\n");
	return (fgets(sendline,len,fp));
}
