<?php

class server {

    private $ip;
    private $port;

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
                echo $e->getMessage();
                echo "ACCEPT FAILED:" . socket_strerror(socket_last_error()) . "\n";
            }

            try {
                $request_string = socket_read($new_sock, 1024);

                $response = $this->output($request_string);

                socket_write($new_sock, $response);
                socket_close($new_sock);

            } catch (Exception $e) {
                echo $e->getMessage();
                echo "READ FAILED:" . socket_strerror(socket_last_error()) . "\n";
            }

        } while (TRUE);
    }

    /**
     * @param $request_string
     * @return string
     */
    private function output($request_string){

        // 静态 GET /1.html HTTP/1.1 ...
        // 动态 GET /user.cgi?id=1 HTTP/1.1 ...

        $request_array = explode(" ",$request_string);
        if(count($request_array) < 2){
            return "";
        }

        $uri = $request_array[1];

        echo "request:".web_config::WEB_ROOT . $uri."\n";

        $query_string = null;

        if ($uri == "/favicon.ico") {
            return "";
        }

        if (strpos($uri,"?")) {
            $uriArr = explode("?", $uri);
            $uri = $uriArr[0];
            $query_string = isset($uriArr[1]) ? $uriArr[1] : null;
        }

        $filename = web_config::WEB_ROOT . $uri;

        if ($this->cgi_check($uri)) {
            
            $this->set_env($query_string);

            $handle = popen(web_config::WEB_ROOT.$uri, "r");
            $read = stream_get_contents($handle);
            pclose($handle);

            return $this->add_header($read);
        }

        // 静态文件的处理

        if (file_exists($filename)) {
            return $this->add_header(file_get_contents($filename));
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

        if( $extension && in_array($extension,explode(",",web_config::CGI_EXTENSION))){
            return true;
        }

        return false;
    }

    /**
     * 404 返回
     * @return string
     */
    private function not_found(){
        $content = "<h1>File Not Found </h1>";

        return "HTTP/1.1 404 File Not Found\r\nContent-Type: text/html\r\nContent-Length: ".strlen($content)."\r\n\r\n".$content;
    }

    /**
     * 加上头信息
     * @param $string
     * @return string
     */
    private function add_header($string){
        return "HTTP/1.1 200 OK\r\nContent-Length: ".strlen($string)."\r\nServer: mengkang\r\n\r\n".$string;
    }

}
