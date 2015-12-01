<?php
defined('IN_PHPCMS') or exit('No permission resources.');
pc_base::load_app_class('admin','admin',0);

class orders extends admin {

	private $db;
    public $username;
	public function __construct() {
		parent::__construct();

		//$this->username = param::get_cookie('admin_username');
		$this->db = pc_base::load_model('orders_model');
	}
	
	public function init() {
		//列表
		$sql = '';

        if($_GET['status']){
            $sql = '`status`=\''.intval($_GET['status']).'\'';
        }else{
            $sql = '`status` < 2';
        }

		$page = max(intval($_GET['page']), 1);
		$data = $this->db->listinfo($sql, '`id` DESC', $page,10);

		include $this->admin_tpl('orders_list');
	}

    /**
     * 查看详情
     */
    public function view(){
        $where = array('id' => intval($_GET['id']));
        $info = $this->db->get_one($where);
        //var_dump($info);
        include $this->admin_tpl('orders_view');
    }
	

	/**
	 * 批量修改状态
	 */
	public function check(){
		if((!isset($_REQUEST['id']) || empty($_REQUEST['id']))) {
			showmessage(L('illegal_operation'));
		} else {
            if(is_array($_REQUEST['id'])){
                /*$ids = explode(',',array_map('intval',$_REQUEST['id']));
                $res = $this->db->update();*/
                foreach($_REQUEST['id'] as $v){
                    $res[] = $this->db->update(array('status' => intval($_GET['status'])), array('id' => intval($v)));
                }
                if(array_product($res)){
                    showmessage('处理成功', HTTP_REFERER);
                }else{
                    showmessage('部分成功', HTTP_REFERER);
                }
            }else{
                $id = intval($_REQUEST['id']);
                $res = $this->db->update(array('status' => intval($_GET['status'])), array('id' => intval($id)));
                $res && showmessage('处理成功', HTTP_REFERER);
            }
		}
    }
}
?>