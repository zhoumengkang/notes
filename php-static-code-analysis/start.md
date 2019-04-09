# PHP 代码静态扫描
## 先找出新增的文件

比较即将发布的`releases`分支和`master`分支的区别

```bash
git diff origin/releases/20190320164433039_r_release_96232_xxx-code master --name-only >> ~/test.txt

cat ~/test.txt
a.php
b.php
c.php
```
## 使用正确的工具

> https://github.com/phan/phan

先安装`php-ast`扩展

```bash
git clone https://github.com/nikic/php-ast
cd php-ast/
phpize
sudo ./configure --enable-ast
sudo make
sudo make install
cd /etc/php.d
# 引入扩展
sudo vim ast.ini
# 就能看到扩展啦
php -m | grep ast
```
安装 composer

```bash
curl -sS https://getcomposer.org/installer | php
```
安装`plan`
```bash
mkdir test
cd test
~/composer.phar require --dev "phan/phan:1.x"
```
# 实验1

新建个项目，随便写个有问题的代码

路径是`src/a.php`

```php
<?php

class A extends B
{
    public function a1()
    {
        return $this->a2(1);
    }

    /**
     * @param array $b
     *
     * @return int
     */
    private function a2($b)
    {
        return $b + 1;
    }
}
```

写个shell脚本
```bash
#!/bin/bash

function log()
{
    echo -e -n "\033[01;35m[YUNQI] \033[01;31m"
    echo $@
    echo -e -n "\033[00m"
}

Color_Text()
{
  echo -e " \e[0;$2m$1\e[0m"
}

Echo_Red()
{
  echo $(Color_Text "$1" "31")
}

Echo_Green()
{
  echo $(Color_Text "$1" "32")
}

Echo_Yellow()
{
  echo $(Color_Text "$1" "33")
}

: > file.list

for file in $(ls src/*)
do
  echo $file >> file.list
done

Echo_Green "file list:\n"
Echo_Green "========================\n"

cat file.list

Echo_Green "========================\n"


Echo_Yellow "Phan run\n"
Echo_Yellow "========================\n"

./vendor/bin/phan -f file.list -o res.out

Echo_Yellow "========================\n"

Echo_Red "error log\n"
Echo_Red "========================\n"

cat res.out

Echo_Red "========================\n"
```

## 执行结果

案例中的错误
1. 类不存在
2. 参数类型错误
3. 语法运算类型推断

![image.png](https://static.mengkang.net/upload/image/2019/0322/1553251500297376.png)

# 实验2

新增一个`src/b.php`

```php
<?php
class B{

}
```

## 执行结果

能过自动查找到`class B`了，不用我们做自动加载规则的指定

![image.png](https://static.mengkang.net/upload/image/2019/0322/1553251910215320.png)

## 实验3

Phan 还可以指定目录检测，我们可以修改下上面的脚本为

```bash
#!/bin/bash

function log()
{
    echo -e -n "\033[01;35m[YUNQI] \033[01;31m"
    echo $@
    echo -e -n "\033[00m"
}

Color_Text()
{
  echo -e " \e[0;$2m$1\e[0m"
}

Echo_Red()
{
  echo $(Color_Text "$1" "31")
}

Echo_Green()
{
  echo $(Color_Text "$1" "32")
}

: > file.list

for file in $(ls src/*)
do
  echo $file >> file.list
done

Echo_Green "Phan run\n"
Echo_Green "========================\n"

WORK_DIR=$(cd `dirname $0`; pwd)

echo ${WORK_DIR}

./vendor/bin/phan --progress-bar -f file.list -o ${WORK_DIR}/res.out

Echo_Green "========================\n"

Echo_Red "error log\n"
Echo_Red "========================\n"

cat ${WORK_DIR}/res.out

Echo_Red "========================\n"
```

然后修改两个文件

```php
class B
{
    protected $a;
}
```

```php
class A extends B
{
    private $a;
}
```

前面的两个实验，ide 都能帮我们检测出来，这种情况 ide 是不会提示有错误的，那么我们执行下


![image.png](https://static.mengkang.net/upload/image/2019/0322/1553253603449043.png)