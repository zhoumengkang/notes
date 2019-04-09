<?php
/**
 * Created by PhpStorm.
 * User: mengkang
 * Date: 2019/4/9
 * Time: 下午4:45
 */

include "./server.php";

$server = new WebServer("127.0.0.1", "9001", __DIR__);
$server->start();