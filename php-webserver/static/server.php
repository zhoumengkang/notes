<?php

class WebServer
{

    protected $ip;
    protected $port;
    protected $webRoot;

    public function __construct($ip, $port, $webRoot)
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

        echo $this->ip . ":" . $this->port . "\tserver start\n";

        do {
            $clientFd = null;

            try {
                $clientFd = socket_accept($fd);
            } catch (Exception $e) {
                echo $e->getMessage();
                echo "ACCEPT FAILED:" . socket_strerror(socket_last_error()) . "\n";
            }

            try {
                $requestData = socket_read($clientFd, 1024);

                $response = $this->requestHandler($requestData);

                socket_write($clientFd, $response);
                socket_close($clientFd);

            } catch (Exception $e) {
                echo $e->getMessage();
                echo "READ FAILED:" . socket_strerror(socket_last_error()) . "\n";
            }

        } while (true);
    }

    /**
     * @param $requestData
     *
     * @return string
     */
    protected function requestHandler($requestData)
    {

        echo $requestData;

        // 静态 GET /1.html HTTP/1.1 ...

        $array = explode(" ", $requestData);
        if (count($array) < 2) {
            return "";
        }

        $uri = $array[1];

        if ($uri == "/favicon.ico") {
            return "";
        }

        $filename = $this->webRoot . $uri;
        echo "request:" . $filename . "\n";

        // 静态文件的处理

        if (file_exists($filename)) {
            return $this->addHeader(file_get_contents($filename));
        } else {
            return $this->notFound();
        }
    }

    /**
     * 404 返回
     *
     * @return string
     */
    protected function notFound()
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
    protected function addHeader($string)
    {
        return "HTTP/1.1 200 OK\r\nContent-Length: " . strlen($string) . "\r\nServer: mengkang\r\n\r\n" . $string;
    }
}
