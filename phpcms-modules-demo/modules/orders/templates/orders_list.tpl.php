<?php 
defined('IN_ADMIN') or exit('No permission resources.');
include $this->admin_tpl('header', 'admin');
?>
<div class="pad-lr-10">
<style type="text/css">
    a:hover{
        cursor: pointer;
    }
    .status_green{
        color: rgb(109, 207, 109);
    }
    .status_red{
        color: red;
    }
    .operation{
        color: #000000;
    }
    .explain-col{
        margin-bottom: 10px;
    }
</style>
<form name="myform" action="?m=orders&c=orders&a=check" method="post">
<div class="table-list">
    <table width="100%" cellspacing="0">
        <thead>
        <div class="explain-col">
            <a class="button" href="?m=orders&c=orders&a=init">显示全部合作订单</a>
            <a class="button" href="?m=orders&c=orders&a=init&status=0">显示未处理的合作订单</a>
        </div>
            <tr>
                <th width="35" align="center">
                    <input type="checkbox" value="" id="check_box" onclick="selectall('id[]');">
                </th>
                <th width="100" align="center">姓名</th>
                <th width='200' align="center">公司</th>
                <th width='120' align="center">电话</th>
                <th width="180" align="center">邮箱</th>
                <th  align="center">申请时间</th>
                <th  align="center">是否处理</th>
                <th  align="center">操作</th>
            </tr>
        </thead>
    <tbody>

 <?php 
if(is_array($data)){
	foreach($data as $v){
?>
    <tr <?php if($v['status']==1){echo 'class="status_green"';}else{echo 'class="status_red"';}?>>
        <td align="center">
        <input type="checkbox" name="id[]" value="<?php echo $v['id']?>">
        </td>
        <td align="center"><?php echo $v['username']?></td>
        <td align="center"><?php echo $v['company']?></td>
        <td align="center"><?php echo $v['tel']?></td>
        <td align="center"><?php echo $v['email']?></td>
        <td align="center"><?php echo date('Y-m-d H:i:s', $v['addtime'])?></td>
        <td align="center"><?php if($v['status']){echo '已经处理';}else{echo '未处理';} ?></td>
        <td align="center" class="operation">
            <input type="submit" class="button" onclick="view_infomation(<?php echo $v['id'];?>);return false;" value="查看详情"/>
            <input name="submit" type="submit" class="button" value="处理" onClick="document.myform.action='?m=orders&c=orders&a=check&id=<?php echo $v['id'];?>&status=0';">
            <input name="submit" type="submit" class="button" value="删除" onClick="document.myform.action='?m=orders&c=orders&a=check&id=<?php echo $v['id'];?>&status=2';return confirm('确认删除吗?')">
        </td>
	</tr>
<?php 
	}
}
?>
</tbody>
    </table>
  
    <div class="btn">
        <label for="check_box">
            <?php echo L('selected_all')?>/<?php echo L('cancel')?>
        </label>
		<input name="submit" type="submit" class="button" value="删除所有" onClick="document.myform.action='?m=orders&c=orders&a=check&status=2';return confirm('确认删除吗?')">&nbsp;&nbsp;
        <input name="submit" type="submit" class="button" value="处理所有" onClick="document.myform.action='?m=orders&c=orders&a=check&status=1';">&nbsp;&nbsp;
    </div>
    <div id="pages">
        <?php echo $this->db->pages;?>
    </div>
</form>
</div>
</body>
</html>
<script type="text/javascript">
function view_infomation(userid) {
    window.top.art.dialog({
        id:'view_infomation'
    }).close();
    window.top.art.dialog({
        title:'需求详情',
        id:'view_infomation',
        iframe:'?m=orders&c=orders&a=view&id='+userid,width:'700',height:'500'
    }, function(){
        window.top.art.dialog({id:'view_infomation'}).close()
    });
}
</script>