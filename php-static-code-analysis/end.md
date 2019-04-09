很多时候，最大的优势在某些情况下就会变成最大的劣势。PHP 语法非常灵活，也不用编译。但是在项目比较复杂的时候，可能会导致一些意想不到的 bug。

# 背景分析

不知道你的项目是否有遇到过类似的线上故障呢？比如

## 继承类语法错误导致的故障

文件1
```php
class Animal
{
    public $hasLeg = false;
}
```
文件2
```php
include "Animal.php";

class Dog extends Animal
{
    protected $hasLeg = false;
}

$dog = new Dog();
```
执行脚本
```bash
php Dog.php

Fatal error: Access level to Dog::$hasLeg must be public (as in class Animal) in /Users/mengkang/vagrant-develop/project/untitled1/Dog.php on line 5
```  
如图
![image.png](https://static.mengkang.net/upload/image/2019/0304/1551685668526070.png)

（注意 IDE 并没有提示有预发错误的哟，我专门截图）

今天在看代码的时候看到一个变量一直重复查询，就是用户是否是管理员的身份。我想既然这样，不然在第一次用的地方就放入到成员变量里，免得后面都重复查询。

结果发现我在父类定义的变量名`$isAdmin`，之前的代码已经在某一个子类里面单独定义过了。父类里是`public`属性，而子类里是`private`导致了这个故障。

如果是 java 这种错误，无法编译通过。但是 php 不需要编译，只要测试没有覆盖到刚刚修改的文件就不会发现这个问题，既是优势也是弱势。


## 参数不符合预期

![image.png](https://static.mengkang.net/upload/image/2019/0304/1551687172732098.png)

有时候`a.php`,`b.php`,`c.php`三个文件都引用`d.php`的的一个函数，但是修改了`d.php`里面的一个函数的参数个数，如果前面使用的3个文件里面的没有改全，只改了`a.php`，而测试的时候又没有覆盖到`b.php`和`c.php`，那么上线了，就会触发`bug`和错误了。

### 错把数组当对象

你可能认为这种错误太低级了，不可能发生在自己身上，但是根据我的经验的确会发生，高强度的需求之下，很容易复制粘贴一些东西，只复制一半。而且恰巧因为某些逻辑判断，自己在日常环境开发的时候，出现问题的地方没有被执行到。
比如下面这段代码：

```php
$article = $this->getParam('article');

// 假设下面这段代码是复制的
$isPowerEditer = "xxxxx 演示代码";

if(!$isPowerEditer){
    if ($article->getUserId() != $uid)
    {
        ...
    }
}
```  

因为复制的来源处，`$article`是一个对象，所以调用了`getUserId`的方法。但是上面的`$article`是一个从客户端获取的参数，不是对象。

> Call to a member function getUserId() on a non-object

而自己测试的时候，因为`if(!$isPowerEditer)`的判断导致没有执行到里面去。直到上线之后才发现问题。

## 错把对象当数组
![image.png](https://static.mengkang.net/upload/image/2018/0725/1532510390438304.png)

```
Cannot use object of type DataObject\Article as array
```  

不禁反思，如果这个项目是 java 的，肯定不会出现上面两个问题了，因为在项目构建的时候就已经没法通过了。

## 不存在的数组

![image.png](https://static.mengkang.net/upload/image/2018/1116/1542364029645985.png)
这也不飘红？多写了个`s`呢，可能因为外面包了一个`empty`所以IDE没有标记为错误吧。所以我们不能太相信IDE。

# 思考与改进

## 自造轮子实验

进一步思考，我们是否能够做一个工具来自己模拟编译呢？写了一个小 demo ,依赖`nikic/php-parser`
> https://github.com/nikic/PHP-Parser

PHP-Parser 可以把PHP代码解析为AST，方便我们做语法分析。比如上面的例子
文件1
```php
class Animal
{
    public $hasLeg = false;
}
```
文件2(Dog.php)
```php
include "Animal.php";

class Dog extends Animal
{
    protected $hasLeg = false;
}

$dog = new Dog();
```
我们利用 PHP-Parser 做了语法解析检测，代码如下：
```php
include dirname(__DIR__)."/vendor/autoload.php";

use PhpParser\Error;
use PhpParser\Node\Stmt\Property;
use PhpParser\ParserFactory;
use PhpParser\Node\Stmt\Class_;

$code = file_get_contents("Dog.php");

$parser = (new ParserFactory)->create(ParserFactory::PREFER_PHP5);

try {
    $ast = $parser->parse($code);
} catch (Error $error) {
    echo "Parse error: {$error->getMessage()}\n";
    return;
}

$classCheck = new ClassCheck($ast);
$classCheck->extendsCheck();


class ClassCheck{

    /**
     * @var Class_[]|null
     */
    private $classTable;

    public function __construct($nodes)
    {
        foreach ($nodes as $node){
            if ($node instanceof Class_){
                $name = $node->name;
                if (!isset($this->classTable[$name])) {
                    $this->classTable[$name] = $node;
                }else{
                    // 报错哪里类重复了
                    echo $node->getLine();
                }
            }
        }
    }

    public function extendsCheck(){

        foreach ($this->classTable as $node){
            if (!$node->extends){
                continue;
            }

            $parentClassName = $node->extends->getFirst();

            if (!isset($this->classTable[$parentClassName])) {
                exit($parentClassName."不存在");
            }

            $parentNode = $this->classTable[$parentClassName];

            foreach ($node->stmts as $stmt){
                if ($stmt instanceof Property){
                    // 查看该属性是否存在于父类中
                    $this->propertyCheck($stmt,$parentNode);
                }
            }
        }
    }

    /**
     * @param Property $property
     * @param Class_ $parentNode
     */
    private function propertyCheck($property,$parentNode){
        foreach ($parentNode->stmts as $stmt){
            if ($stmt instanceof Property){
                if ($stmt->props[0]->name != $property->props[0]->name){
                    continue;
                }

                if ($stmt->isProtected() && $property->isPrivate()) {
                    echo $stmt->getLine()."\n";
                    echo $property->getLine()."\n";
                }
            }
        }
    }
}
```  
原理能就是对解析出来的AST继续做分析，但是前人栽树后人乘凉，这样的完整工具已经有大神帮我们做好了。

## 使用现有工具

> https://github.com/phan/phan

可以说它与上面介绍的`nikic/php-parser`师出同门，依赖`nikic/php-ast`PHP扩展 

### 先安装`php-ast`扩展
大概描述安装步骤

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
### 安装 composer
大概描述安装步骤

```bash
curl -sS https://getcomposer.org/installer | php
```
安装`plan`
```bash
mkdir test
cd test
~/composer.phar require --dev "phan/phan:1.x"
```
### 实验

#### 实验1

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

##### 执行结果

案例中的错误
1. 类不存在
2. 参数类型错误
3. 语法运算类型推断

![image.png](https://static.mengkang.net/upload/image/2019/0322/1553251500297376.png)

#### 实验2

新增一个`src/b.php`

```php
<?php
class B{

}
```

##### 执行结果

能过自动查找到`class B`了，不用我们做自动加载规则的指定

![image.png](https://static.mengkang.net/upload/image/2019/0322/1553251910215320.png)

#### 实验3

刚刚两个都是测试的单独的脚本，没有测试项目，其实`Plan`已经支持了。假如我有一个项目如下

![image.png](https://static.mengkang.net/upload/image/2019/0324/1553407052319004.png)

我在composer.json里面指定自动加载规则
```json
{
  "require-dev": {
    "phan/phan": "1.x"
  },
  "autoload": {
    "psr-4": {
      "Mk\\": "src"
    }
  }
}
```
然后在项目根目录执行
```bash
./vendor/bin/phan --init --init-level=3
```
然后就会生成默认的配置文件在`.phan`目录里，最后就可以执行静态检测命令了
```bash
./vendor/bin/phan --progress-bar
```

![image.png](https://static.mengkang.net/upload/image/2019/0324/1553407357509360.png)

如图所示呢，说明根据项目的自动加载规则`A`,`B`,`C`三个类呢都被扫描到了。

看到这里，是不是有想把自己项目上线流程里面加上静态语法检测呢？心动不如行动。