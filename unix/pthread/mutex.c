#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <semaphore.h>
#include <time.h>

void * thread_function(void * arg);

pthread_mutex_t work_mutex;

#define WORK_SIZE 1024

char work_area[WORK_SIZE];

int time_to_exit = 0;

int main(int argc, char const *argv[])
{
	pthread_t a_thread;
	void * thread_result;

	if (pthread_mutex_init(&work_mutex,NULL) != 0)
	{
		perror("mutex pthread_mutex_init failed");
		exit(EXIT_FAILURE);
	}

	if (pthread_create(&a_thread,NULL,thread_function,NULL) != 0)
	{
		perror("threae create failed");
		exit(EXIT_FAILURE);
	}

	pthread_mutex_lock(&work_mutex);

	printf("input some text, enter 'end' to finish\n");

	/* time_to_exit 等于 0 ，即表示用户键盘输入尚未结束，其值从线程修改更新 */
	while(!time_to_exit)
	{
		fgets(work_area,WORK_SIZE,stdin);
		pthread_mutex_unlock(&work_mutex);
		while(1)
		{
			pthread_mutex_lock(&work_mutex);
			if (work_area[0] != '\0')
			{
				pthread_mutex_unlock(&work_mutex);
				sleep(1);
			}
			else
			{
				break;
			}
		}
	}

	pthread_mutex_unlock(&work_mutex);

	printf("waiting for thread to finish\n");

	if (pthread_join(a_thread,&thread_result) != 0)
	{
		perror("pthread_join failed\n");
		exit(EXIT_FAILURE);
	}

	printf("thread joined\n");

	pthread_mutex_destroy(&work_mutex);

	exit(EXIT_SUCCESS);

}

void * thread_function(void * arg)
{
	printf("time: %d a_thread start\n", time(0));
	sleep(1);
	pthread_mutex_lock(&work_mutex);
	while(strncmp("end",work_area,3) != 0)
	{
		printf("you input %d characters\n", strlen(work_area)-1);
		work_area[0] = '\0';
		pthread_mutex_unlock(&work_mutex);
		sleep(1);
		pthread_mutex_lock(&work_mutex);
		while(work_area[0] == '\0')
		{
			pthread_mutex_unlock(&work_mutex);
			sleep(1);
			pthread_mutex_lock(&work_mutex);
		}
	}

	time_to_exit = 0;
	work_area[0] = '\0';
	pthread_mutex_unlock(&work_mutex);
	pthread_exit(0);
}
