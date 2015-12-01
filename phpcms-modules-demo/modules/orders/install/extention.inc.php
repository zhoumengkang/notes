<?php
defined('IN_PHPCMS') or exit('Access Denied');
defined('INSTALL') or exit('Access Denied');

$parentid = $menu_db->insert(array('name'=>'orders', 'parentid'=>0, 'm'=>'orders', 'c'=>'orders', 'a'=>'init', 'data'=>'', 'listorder'=>10, 'display'=>'1'), true);
$parentid = $menu_db->insert(array('name'=>'orders', 'parentid'=>$parentid, 'm'=>'orders', 'c'=>'orders', 'a'=>'init', 'data'=>'', 'listorder'=>0, 'display'=>'1'), true);
$menu_db->insert(array('name'=>'list_orders', 'parentid'=>$parentid, 'm'=>'orders', 'c'=>'orders', 'a'=>'init', 'data'=>'', 'listorder'=>0, 'display'=>'1'));
$menu_db->insert(array('name'=>'list_views', 'parentid'=>$parentid, 'm'=>'orders', 'c'=>'orders', 'a'=>'view', 'data'=>'', 'listorder'=>0, 'display'=>'1'));
$menu_db->insert(array('name'=>'list_check', 'parentid'=>$parentid, 'm'=>'orders', 'c'=>'orders', 'a'=>'check', 'data'=>'', 'listorder'=>0, 'display'=>'1'));
$language = array('orders'=>'订单管理','list_orders'=>'订单列表','list_views'=>'订单浏览','list_check'=>'订单处理');
?>
