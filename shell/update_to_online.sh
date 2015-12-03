#!/bin/sh

######################## config ########################

# 升级日志
publish_log_file="/update/log/path/publish.log"

# 需要邮件通知的人
master=(
    i@zhoumengkang.com
    mengkang@topit.me
    zhoumengkang@php.net
)

# 更新服务器列表
remote_hosts=(
    192.168.1.14
    192.168.1.10
    192.168.1.17
    192.168.1.13
    192.168.1.14
    192.168.1.13
    192.168.1.26  # 图片上传服务器
    192.168.1.17  # 用于 static.mengkang.net 图片访问
)
######################## config ########################

function_svn_diff(){
    svn_diff_size=`sudo svn diff|wc -c`;
    
    if((svn_diff_size>0));then
        echo -e "\e[10;31m以下文件修改了,但还未提交\e[0m"
        echo -e "\e[10;31m============================================================\e[0m"
        sudo svn diff|grep "Index:"|awk '{print $2}'
        echo -e "\e[10;31m============================================================\e[0m"
        echo -e "\e[10;31m请先进行 svn 代码提交再执行上线操作\e[0m"
        exit;
    fi
}

function_svn_need_add(){

    echo -e "\e[10;31m以下文件还未加入版本库\e[0m"
    echo -e "\e[10;31m============================================================\e[0m"
    sudo svn st|grep "?"|grep "\.php"|grep -v "site2.0/back"|awk '{print $2}'
    echo -e "\e[10;31m============================================================\e[0m"
    echo -e "\e[10;31m忽略这些文件直接上线? \e[0m\e[10;32m yes \e[0m / \e[10;31m no \e[0m"
    
    read comfirm
    
    if [ "$comfirm" != "yes" ]; then
        echo -e "\e[0m\e[10;32m上线取消\e[0m"
        exit
    fi
}


function_puslish_init(){
    for i in "${!remote_hosts[@]}";do
        if(($i == 0)); then
            function_rsync_online ${remote_hosts[$i]} | tee $publish_log_file;

            # 先看有没有更新文件，如果没有更新则退出，没有更新则只有下面四行，如果有更新则会有更新的文件列表
            # sending incremental file list

            # sent 147892 bytes  received 639 bytes  99020.67 bytes/sec
            # total size is 707645226  speedup is 4764.29
            num=`cat $publish_log_file|wc -l`
            if(($num<5)); then
                echo -e "\e[0m\e[10;32m暂无更新\e[0m";
                exit;
            fi
        else
            function_rsync_online ${remote_hosts[$i]}
        fi
    done
}

# 分发
function_rsync_online(){
    if [[ $1 ]]; then
        export RSYNC_PASSWORD=rsync
        /usr/bin/rsync -avz --delete --exclude=".*" /path/of/code/ rsync@$1::topit_online/
    fi
}

function_publish_quit(){
    # online 目录版本
    head_log=`sudo svn log -r head|grep -v "\-\-"|grep -v ^$`
    publish_log=`cat $publish_log_file`
    # 类似于 r175 | xiaofei | 2015-07-08 15:40:24 +0800 (Wed, 08 Jul 2015) | 1 line 活动规则显示 bug 修复
    echo $head_log;

    for x in ${master[@]};do
        echo -e "$head_log\n---------------------------------------\n$publish_log\n---------------------------------------\n上线人: $USER"|mail -s "topit上线通知" $x
        echo -e "已邮件通知\e[0m\e[10;32m $x\e[0m";
    done
}

cd /path/of/code/;

function_svn_diff;
function_svn_need_add;
function_puslish_init;
function_publish_quit;
