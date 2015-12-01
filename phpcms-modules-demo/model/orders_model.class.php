<?php
defined('IN_PHPCMS') or exit('No permission resources.');
pc_base::load_sys_class('model', '', 0);
//修改这个类名为"order_model"
class orders_model extends model {

    public $table_name;
    public function __construct() {
        $this->db_config = pc_base::load_config('database');
        $this->db_setting = 'default';
        $this->table_name = 'orders';//仅仅需要修改这里的对应的表名,不带前缀
        parent::__construct();
    }
}
?>