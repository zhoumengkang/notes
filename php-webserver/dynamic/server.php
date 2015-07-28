<?php

class server {

    private $ip;
    private $port;

    const WEB_ROOT = "/Users/zhoumengkang/Documents/html";

    // 系统支持的 cgi 程序的文件扩展名
    private $cgi_extension = array("cgi");

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
            $new_sock = null;

            try {
                $new_sock = socket_accept($sock);
            } catch (Exception $e) {
                print $e->getMessage();
                echo "ACCEPT FAILED:" . socket_strerror(socket_last_error()) . "\n";
            }

            try {
                $buf = socket_read($new_sock, 1024);

                $msg = $this->output($buf);

                socket_write($new_sock, $msg, strlen($msg));
                socket_close($new_sock);

            } catch (Exception $e) {
                print $e->getMessage();
                echo "READ FAILED:" . socket_strerror(socket_last_error()) . "\n";
            }

        } while (TRUE);
    }

    /**
     * 404 返回
     * @return string
     */
    private function not_found(){
        return "HTTP/1.1 404 File Not Found\r\nContent-Type: text/html\r\nContent-Length: 23\r\n\r\n<h1>File Not Found</h1>";
    }

    /**
     * @param $request_string
     * @return string
     */
    private function output($request_string){

        // 静态 GET /1.html HTTP/1.1 ...
        // 动态 GET /user.cgi?id=1 HTTP/1.1 ...

        $tmp_data = explode(" ",$request_string);
        $uri = $tmp_data[1];

        $query_string = null;

        if ($uri == "/favicon.ico") {
            return "";
        }

        if (strpos($uri,"?")) {
            $uriArr = explode("?", $uri);
            $uri = $uriArr[0];
            $query_string = isset($uriArr[1]) ? $uriArr[1] : null;
        }

        $filename = self::WEB_ROOT . $uri;

        echo "客户端请求了:".$filename."\n";

        if ($this->cgi_check($uri)) {
            
            $this->set_env($query_string);

            $handle = popen(self::WEB_ROOT.$uri, "r");
            $read = stream_get_contents($handle);
            pclose($handle);

            return $read;
        }

        // 静态文件的处理

        if (file_exists($filename)) {
            return file_get_contents($filename);
        } else {
            return $this->not_found();
        }
    }

    /**
     * 设置环境变量 给 cgi 程序使用
     * @param $query_string
     * @return bool
     */
    private function set_env($query_string){

        if($query_string == null){
            return false;
        }

        if (strpos($query_string, "=")) {
            putenv("QUERY_STRING=".$query_string);
        }
    }

    /**
     * 判断请求的 uri 是否是合法的 cgi 资源
     * @param $uri
     * @return bool
     */
    private function cgi_check($uri){

        $info = pathinfo($uri);

        $extension = isset($info["extension"]) ? $info["extension"] : null;

        if( $extension && in_array($extension,$this->cgi_extension)){
            return true;
        }

        return false;
    }

}
