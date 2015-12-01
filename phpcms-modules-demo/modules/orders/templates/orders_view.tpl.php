<?php 
defined('IN_ADMIN') or exit('No permission resources.');
?>
<style type="text/css">
    th,td{
        font: 12px/1.5 tahoma,arial,\5b8b\4f53,sans-serif;
        border-bottom: #eee 1px solid;
        padding-top: 5px;
        padding-bottom: 5px;
    }
</style>
<div class="pad-lr-10">
    <table class="table_form" width="100%">
        <tbody>
            <tr>
            <th width="80">姓名：</th>
            <td><?php echo $info['username']?></td>
            </tr>
            <tr>
            <th>公司：</th>
            <td><?php echo $info['company']?></td>
            </tr>
            <tr>
            <th width="80">电话：</th>
            <td><?php echo $info['tel']?></td>
            </tr>
            <tr>
            <th>邮箱：</th>
            <td><?php echo $info['email']?></td>
            </tr>
            <tr>
            <th width="80">需求：</th>
            <td><?php echo $info['requirement']?></td>
            </tr>
        </tbody>
    </table>
</div>
</body>
</html>