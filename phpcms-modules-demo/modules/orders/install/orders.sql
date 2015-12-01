DROP TABLE IF EXISTS `phpcms_orders`;
CREATE TABLE IF NOT EXISTS `phpcms_orders` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL COMMENT '姓名',
  `company` varchar(300) NOT NULL COMMENT '公司名字',
  `tel` varchar(20) NOT NULL COMMENT '手机/座机',
  `email` varchar(200) NOT NULL COMMENT '电子邮件',
  `requirement` text NOT NULL COMMENT '用户需求',
  `addtime` int(11) NOT NULL COMMENT '数据填写的时间',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否处理',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;