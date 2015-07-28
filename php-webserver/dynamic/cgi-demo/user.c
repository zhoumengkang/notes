#include <stdio.h>
#include <stdlib.h>
// 通过获取的 id 查询用户的信息
int main(void){

	// 演示数据
	typedef struct 
	{
		char *username;
		int  age;
	} user;

	user users[] = {
		{
			"周梦康",
			27
		},
		{
			"mengkang.zhou",
			18
		}
	};


	char *query_string;
	int id;

	query_string = getenv("QUERY_STRING");
	
	if (query_string == NULL)
	{
		printf("没有输入数据");
	} else if (sscanf(query_string,"id=%d",&id) != 1)
	{
		printf("没有输入id");
	} else
	{
		printf("用户信息查询\n学号: %d\n姓名: %s\n年龄: %d",id,users[id].username,users[id].age);
	}
	
	return 0;
}
