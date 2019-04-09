<?php

include dirname(__DIR__) . "/static/server.php";

class DynamicWebServer extends WebServer
{

    protected $cgiRoot;
    protected $cgiExtension = "cgi";


    public function __construct($ip, $port, $webRoot, $cgiRoot, $cgiExtension)
    {
        parent::__construct($ip, $port, $webRoot);
        $this->cgiRoot = $cgiRoot;
        $this->cgiExtension = $cgiExtension;
    }


    /**
     * @param $requestData
     *
     * @return string
     */
    protected function requestHandler($requestData)
    {

        // 静态 GET /1.html HTTP/1.1 ...
        // 动态 GET /user.cgi?id=1 HTTP/1.1 ...

        $array = explode(" ", $requestData);
        if (count($array) < 2) {
            return "";
        }

        $uri = $array[1];

        if ($uri == "/favicon.ico") {
            return "";
        }

        $queryString = "";

        if (strpos($uri, "?")) {
            $uriArr = explode("?", $uri);
            $uri = $uriArr[0];
            $queryString = isset($uriArr[1]) ? $uriArr[1] : "";
        }

        $filename = $this->webRoot . $uri;
        echo "request:" . $filename . "\n";

        if ($this->cgiCheck($uri)) {

            $this->setEnv($queryString);

            $handle = popen($this->webRoot . $uri, "r");
            $read = stream_get_contents($handle);
            pclose($handle);

            return $this->addHeader($read);
        } elseif (file_exists($filename)) {
            return $this->addHeader(file_get_contents($filename));
        } else {
            return $this->notFound();
        }
    }

    /**
     * 设置环境变量 给 cgi 程序使用
     *
     * @param $queryString
     *
     * @return bool
     */
    private function setEnv($queryString)
    {

        if (!$queryString) {
            return false;
        }

        if (strpos($queryString, "=")) {
            putenv("QUERY_STRING=" . $queryString);
        }

        return true;
    }

    /**
     * 判断请求的 uri 是否是合法的 cgi 资源
     *
     * @param $uri
     *
     * @return bool
     */
    private function cgiCheck($uri)
    {

        $info = pathinfo($uri);

        $extension = isset($info["extension"]) ? $info["extension"] : null;

        if ($extension == $this->cgiExtension) {
            return true;
        }

        return false;
    }

}
