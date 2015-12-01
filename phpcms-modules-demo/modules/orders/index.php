<?php 
defined('IN_PHPCMS') or exit('No permission resources.');
class index {

    function __construct() {
        $this->db = pc_base::load_model('orders_model');
    }

	public function init() {
        include template('orders', 'show');
	}

    public function insert(){
        $data = array_map('htmlspecialchars',$_POST);
        $data['addtime'] = time();
        $data['status'] = 0;
        if($this->db->insert($data)){
            showmessage(L('add_success'),WEB_PATH);
        }else{
            showmessage(L('operation_failure'),HTTP_REFERER);
        }
    }
}
?>
