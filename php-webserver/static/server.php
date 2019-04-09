<?php

class WebServer
{

    private $ip;
    private $port;

    private $webRoot;

    public function __construct($ip, $port,$webRoot)
    {
        $this->ip = $ip;
        $this->port = $port;
        $this->webRoot = $webRoot;
    }

    public function start()
    {
        $fd = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);

        if ($fd < 0) {
            echo "Error:" . socket_strerror(socket_last_error()) . "\n";
            exit;
        }

        if (socket_bind($fd, $this->ip, $this->port) < 0) {
            echo "BIND FAILED:" . socket_strerror(socket_last_error()) . "\n";
            exit;
        }

        if (socket_listen($fd) < 0) {
            echo "LISTEN FAILED:" . socket_strerror(socket_last_error()) . "\n";
            exit;
        }

        echo $this->ip.":".$this->port."\tserver start\n";

        do {
            $clientFd = null;

            try {
                $clientFd = socket_accept($fd);
            } catch (Exception $e) {
                echo $e->getMessage();
                echo "ACCEPT FAILED:" . socket_strerror(socket_last_error()) . "\n";
            }

            try {
                $request_string = socket_read($clientFd, 1024);

                $response = $this->output($request_string);

                socket_write($clientFd, $response);
                socket_close($clientFd);

            } catch (Exception $e) {
                echo $e->getMessage();
                echo "READ FAILED:" . socket_strerror(socket_last_error()) . "\n";
            }

        } while (true);
    }

    /**
     * @param $request_string
     *
     * @return string
     */
    private function output($request_string)
    {

        echo $request_string;

        // 静态 GET /1.html HTTP/1.1 ...

        $request_array = explode(" ", $request_string);

        if (count($request_array) < 2) {
            return $this->not_found();
        }

        $uri = $request_array[1];

        $filename = $this->webRoot . $uri;

        echo "request:" . $filename . "\n";

        // 静态文件的处理

        if (file_exists($filename)) {
            return $this->add_header(file_get_contents($filename));
        } else {
            return $this->not_found();
        }
    }

    /**
     * 404 返回
     * @return string
     */
    private function not_found()
    {
        $content = "<h1>File Not Found </h1>";

        return "HTTP/1.1 404 File Not Found\r\nContent-Type: text/html\r\nContent-Length: " . strlen($content) . "\r\n\r\n" . $content;
    }

    /**
     * 加上头信息
     *
     * @param $string
     *
     * @return string
     */
    private function add_header($string)
    {
        return "HTTP/1.1 200 OK\r\nContent-Length: " . strlen($string) . "\r\nServer: mengkang\r\n\r\n" . $string;
    }
}

$server = new WebServer("127.0.0.1","9001",__DIR__);
$server->start();
