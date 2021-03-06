CREATE DATABASE flow_saga;

CREATE TABLE `saga_transaction_log` (
  `id` bigint(20) NOT NULL,
  `shard_routing_key` bigint(20) NOT NULL DEFAULT '0' COMMENT '分库分表路由键',
  `saga_transaction_name` varchar(200) NOT NULL DEFAULT '' COMMENT '业务流程名称',
  `biz_serial_no` varchar(200) NOT NULL DEFAULT '' COMMENT '业务流水号',
  `saga_transaction_class_name` varchar(200) NOT NULL DEFAULT '' COMMENT '流程类名称',
  `saga_transaction_class_method_name` varchar(200) NOT NULL DEFAULT '' COMMENT '流程类方法名称',
  `transaction_status` int(11) NOT NULL DEFAULT '0' COMMENT '流程状态：0-初始态, 1-流程执行成功, 2-流程执行失败, 3-流程回滚成功, 4- 流程回滚失败',
  `param_json` text COMMENT '参数json串',
  `param_type_json` text COMMENT '参数类型json',
  `retry_time` int(11) DEFAULT '0' COMMENT '重试第几次',
  `saga_transaction_type` tinyint(4) DEFAULT NULL COMMENT '流程处理的类型',
  `recover` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否恢复模式',
  `error_msg` varchar(100) NOT NULL DEFAULT '' COMMENT '错误信息',
  `version` int(11) NOT NULL DEFAULT '1' COMMENT '版本号',
  `create_time` bigint(20) NOT NULL COMMENT '创建日期',
  `update_time` bigint(20) NOT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`),
  KEY `inx_biz_serial_no` (`biz_serial_no`),
  KEY `inx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='业务流程记录表';


CREATE TABLE `saga_sub_transaction_log` (
  `id` bigint(20) NOT NULL,
  `shard_routing_key` bigint(20) NOT NULL DEFAULT '0' COMMENT '分库分表路由键',
  `saga_transaction_id` bigint(20) NOT NULL COMMENT '主流程ID',
  `sub_transaction_name` varchar(200) NOT NULL DEFAULT '' COMMENT '业务流程名称',
  `biz_serial_no` varchar(200) NOT NULL DEFAULT '' COMMENT '业务流水号',
  `sub_transaction_class_name` varchar(200) NOT NULL DEFAULT '' COMMENT '流程类名称',
  `sub_transaction_class_method_name` varchar(200) NOT NULL DEFAULT '' COMMENT '流程类方法名称',
  `transaction_status` int(11) NOT NULL DEFAULT '0' COMMENT '流程状态：0-初始态, 1-流程执行成功, 2-流程执行失败, 3-流程回滚成功, 4- 流程回滚失败',
  `param_value_json` text COMMENT '参数json串',
  `param_type_json` text COMMENT '参数类型json',
  `return_value_json` text COMMENT '返回值json',
  `return_type_json` text COMMENT '返回值类型json',
  `error_msg` varchar(100) NOT NULL DEFAULT '' COMMENT '错误信息',
  `version` int(11) NOT NULL DEFAULT '1' COMMENT '版本号',
  `create_time` bigint(20) NOT NULL COMMENT '创建日期',
  `update_time` bigint(20) NOT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`),
  KEY `idx_saga_transaction_id` (`saga_transaction_id`),
  KEY `idx_biz_serial_no` (`biz_serial_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='业务子流程记录表';