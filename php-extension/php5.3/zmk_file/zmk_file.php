<?php

$fp = file_open("./CREDITS","r");
var_dump($fp);
//$res = file_close($fp);
//var_dump($res);
$str = file_read($fp,6);
var_dump($str);


?>
