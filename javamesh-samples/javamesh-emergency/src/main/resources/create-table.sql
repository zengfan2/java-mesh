CREATE TABLE IF NOT EXISTS `emergency_script`
(
    `script_id`     int(11) NOT NULL AUTO_INCREMENT COMMENT '脚本ID',
    `script_name`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '脚本名',
    `is_public`     varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '是否公有，0:私有,1:公有',
    `script_type`   varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '脚本类型 0:shell 1:jython 2:groovy',
    `submit_info`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '提交信息',
    `have_password` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '有无密码 0:无密码,1:有密码',
    `password_mode` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码获取方式 0:本地,1:平台',
    `password`      varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
    `server_user`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务器用户',
    `server_ip`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务器IP',
    `content`       text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '脚本内容',
    `script_user`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '脚本创建人',
    `update_time`   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `param`         varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '参数列表',
    `script_status` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '脚本状态 0:新增,1:待审核,2:已审核,3:被驳回',
    `comment`       varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '审核不通过原因',
    PRIMARY KEY (`script_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;$$$

CREATE TABLE IF NOT EXISTS `emergency_plan`  (
                                   `plan_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `plan_no` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '预案编号',
                                   `plan_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '预案名称',
                                   `create_user` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
                                   `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `is_valid` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '1' COMMENT '是否生效',
                                   `check_user` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '审核人',
                                   `check_time` timestamp NULL DEFAULT NULL COMMENT '审核时间',
                                   `check_remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '审核意见',
                                   `check_result` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '审核结果 0 待审核 1 审核中 2 审核通过 3驳回',
                                   PRIMARY KEY (`plan_id`) USING BTREE,
                                   UNIQUE INDEX `plan_no`(`plan_no`) USING BTREE,
                                   INDEX `is_valid`(`is_valid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;$$$

CREATE TABLE IF NOT EXISTS `emergency_plan_detail`  (
                                          `detail_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                          `plan_id` int(11) NOT NULL COMMENT '预案ID',
                                          `scene_id` int(11) NOT NULL COMMENT '场景ID',
                                          `task_id` int(11) NULL DEFAULT NULL COMMENT '任务ID',
                                          `pre_scene_id` int(11) NULL DEFAULT NULL COMMENT '所依赖的场景ID',
                                          `pre_task_id` int(11) NULL DEFAULT NULL COMMENT '所依赖的任务ID',
                                          `parent_task_id` int(11) NULL DEFAULT NULL COMMENT '父任务ID',
                                          `create_user` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
                                          `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `is_valid` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '1' COMMENT '是否生效',
                                          `sync` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '同步' COMMENT '执行标志位 异步执行async 同步执行sync',
                                          PRIMARY KEY (`detail_id`) USING BTREE,
                                          INDEX `plan_id`(`plan_id`) USING BTREE,
                                          INDEX `scene_id`(`scene_id`) USING BTREE,
                                          INDEX `task_id`(`task_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 251 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;$$$

CREATE TABLE IF NOT EXISTS `emergency_task`  (
                                   `task_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `task_no` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '任务编号',
                                   `task_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '任务名称',
                                   `scene_id` int(11) NULL DEFAULT NULL COMMENT '场景ID',
                                   `script_id` int(11) NULL DEFAULT NULL COMMENT '脚本ID',
                                   `pre_task_id` int(11) NULL DEFAULT NULL COMMENT '所依赖的任务ID',
                                   `create_user` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
                                   `create_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `is_valid` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '1' COMMENT '是否生效',
                                   `channel_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '通道类型',
                                   `script_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '脚本名称',
                                   `submit_info` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '提交信息',
                                   PRIMARY KEY (`task_id`) USING BTREE,
                                   UNIQUE INDEX `task_no`(`task_no`) USING BTREE,
                                   INDEX `task_name`(`task_name`) USING BTREE,
                                   INDEX `is_valid`(`is_valid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;$$$

CREATE TABLE IF NOT EXISTS `emergency_exec`  (
                                   `exec_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `plan_id` int(11) NULL DEFAULT NULL COMMENT '预案ID',
                                   `scene_id` int(11) NULL DEFAULT NULL COMMENT '场景ID',
                                   `task_id` int(11) NULL DEFAULT NULL COMMENT '任务ID',
                                   `task_detail_id` int(11) NULL DEFAULT NULL COMMENT '子任务ID',
                                   `script_id` int(11) NULL DEFAULT NULL COMMENT '脚本ID',
                                   `create_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
                                   `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `start_time` timestamp NULL DEFAULT NULL COMMENT '开始执行时间',
                                   `end_time` timestamp NULL DEFAULT NULL COMMENT '结束执行时间',
                                   PRIMARY KEY (`exec_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;$$$

CREATE TABLE IF NOT EXISTS `emergency_exec_record`  (
                                          `record_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                          `exec_id` int(11) NOT NULL COMMENT '执行ID',
                                          `plan_id` int(11) NOT NULL COMMENT '预案ID',
                                          `scene_id` int(11) NOT NULL COMMENT '场景ID',
                                          `task_id` int(11) NULL DEFAULT NULL COMMENT '任务ID',
                                          `pre_scene_id` int(11) NULL DEFAULT NULL COMMENT '所依赖的场景ID',
                                          `pre_task_id` int(11) NULL DEFAULT NULL COMMENT '所依赖的任务ID',
                                          `parent_task_id` int(11) NULL DEFAULT NULL COMMENT '父任务ID',
                                          `status` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '执行状态（0 待执行 1执行中 2执行成功 3执行失败 4执行取消 5人工确认成功 6人工确认失败）',
                                          `script_id` int(11) NULL DEFAULT NULL COMMENT '脚本ID',
                                          `script_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '脚本名',
                                          `script_content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '脚本内容',
                                          `script_type` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '脚本类型 0 shell 1 jyphon 2 grovy',
                                          `script_params` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '脚本参数',
                                          `server_ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '远程服务器IP',
                                          `server_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务器用户',
                                          `have_password` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '有无密码 0:无密码,1:有密码',
                                          `password_mode` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码获取方式 0:本地,1:平台',
                                          `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
                                          `log` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '运行日志',
                                          `create_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
                                          `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `start_time` timestamp NULL DEFAULT NULL COMMENT '开始执行时间',
                                          `end_time` timestamp NULL DEFAULT NULL COMMENT '结束执行时间',
                                          `ensure_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '确认人',
                                          `is_valid` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '1' COMMENT '有效标志',
                                          `sync` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '执行方式',
                                          PRIMARY KEY (`record_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 480 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;$$$