<?php

include __DIR__ . "/server.php";

$server = new DynamicWebServer("127.0.0.1","9002", __DIR__,__DIR__."/cgi-demo","cgi");

$server->start();
