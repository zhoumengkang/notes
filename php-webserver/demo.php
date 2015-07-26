<?php

class server {
 
    protected $ip;
    protected $port;
    private $webroot = "/Users/zhoumengkang/Documents/html";
    private $socketServer;
 
    public function __construct($ip, $port) {
        $this->ip = $ip;
        $this->port = $port;
        $this->await();
    }
 
    private function await() {
        $sock = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
        if ($sock < 0) {
            echo "Error:" . socket_strerror(socket_last_error()) . "\n";
        }
 
        $ret = socket_bind($sock, $this->ip, $this->port);
        if (!$ret) {
            echo "BIND FAILED:" . socket_strerror(socket_last_error()) . "\n";
            exit;
        }
        echo "OK\n";
 
        $ret = socket_listen($sock);
        if ($ret < 0) {
            echo "LISTEN FAILED:" . socket_strerror(socket_last_error()) . "\n";
        }
 
        do {
            $asock = socket_accept($sock);
            if ($asock < 0) {
                echo "ACCEPT FAILED:" . socket_strerror(socket_last_error()) . "\n";
            }
 
            $buf = socket_read($asock, 1024);
            if (!$buf) {
                echo "READ FAILED:" . socket_strerror(socket_last_error()) . "\n";
                break;
            }

            $msg = $this->get_output($buf);

            socket_write($asock, $msg, strlen($msg));
            socket_close($asock);

        } while (TRUE);
    }

    private function get_output($requestString){
        $tmpData = explode(" ",$requestString); // GET /1.html HTTP/1.1 ...
        $uri = $tmpData[1];
        $filename = $this->webroot . $uri;
        echo $filename."\n";
        if (file_exists($filename)) {
            return file_get_contents($filename);
        } else {
            return $this->not_found();
        }
    }

    private function not_found(){
        return "HTTP/1.1 404 File Not Found\r\nContent-Type: text/html\r\nContent-Length: 23\r\n\r\n<h1>File Not Found</h1>";
    }

}

$server = new server("127.0.0.1", 9003);
