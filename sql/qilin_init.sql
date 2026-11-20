-- =============================================================
--  「栖邻」社区物业工单协同平台  —— 数据库初始化脚本
--  MySQL 8.0+ / InnoDB / utf8mb4
--  v1.0  2026-11-20
--
--  执行方式：
--    mysql -u root -p < qilin_init.sql
--  或在 Navicat / DBeaver 中整体运行
--
--  设计约定：
--    1. 主键统一 BIGINT AUTO_INCREMENT
--    2. 不使用物理外键（互联网项目常见做法），靠索引 + 应用层保证完整性
--    3. 逻辑删除 deleted：0 正常 / 1 已删除
--    4. 时间字段 datetime，应用层时区统一 Asia/Shanghai
--    5. 所有表、字段均带中文注释
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `qilin`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `qilin`;

-- -------------------------------------------------------------
-- 1. 用户表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`      VARCHAR(50)  NOT NULL                COMMENT '登录账号',
  `password`      VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '密码（BCrypt）。初始为空，由后端 DataInitializer 首次启动时写入',
  `real_name`     VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '真实姓名',
  `phone`         VARCHAR(20)  NOT NULL DEFAULT ''     COMMENT '手机号',
  `avatar`        VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '头像地址',
  `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0禁用 1启用',
  `last_login_at` DATETIME     NULL                    COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '最后登录IP',
  `remark`        VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '备注',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -------------------------------------------------------------
-- 2. 角色表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_code`  VARCHAR(50) NOT NULL                COMMENT '角色编码 ADMIN/MANAGER/WORKER/OWNER',
  `role_name`  VARCHAR(50) NOT NULL                COMMENT '角色名称',
  `sort`       INT         NOT NULL DEFAULT 0      COMMENT '排序',
  `status`     TINYINT     NOT NULL DEFAULT 1      COMMENT '状态 0禁用 1启用',
  `remark`     VARCHAR(255) NOT NULL DEFAULT ''    COMMENT '备注',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- -------------------------------------------------------------
-- 3. 用户-角色关联
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL                COMMENT '用户ID',
  `role_id` BIGINT NOT NULL                COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- -------------------------------------------------------------
-- 4. 菜单 / 按钮权限
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id`        BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` BIGINT      NOT NULL DEFAULT 0      COMMENT '父级ID，0为顶级',
  `name`      VARCHAR(50) NOT NULL                COMMENT '名称',
  `path`      VARCHAR(200) NOT NULL DEFAULT ''    COMMENT '路由路径',
  `component` VARCHAR(200) NOT NULL DEFAULT ''    COMMENT '前端组件路径',
  `perms`     VARCHAR(100) NOT NULL DEFAULT ''    COMMENT '权限标识，如 biz:order:dispatch',
  `type`      CHAR(1)     NOT NULL DEFAULT 'C'    COMMENT '类型 M目录 C菜单 F按钮',
  `icon`      VARCHAR(50) NOT NULL DEFAULT ''     COMMENT '图标',
  `sort`      INT         NOT NULL DEFAULT 0      COMMENT '排序',
  `visible`   TINYINT     NOT NULL DEFAULT 1      COMMENT '是否显示 0隐藏 1显示',
  `status`    TINYINT     NOT NULL DEFAULT 1      COMMENT '状态 0禁用 1启用',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- -------------------------------------------------------------
-- 5. 角色-菜单关联
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` BIGINT NOT NULL                COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL                COMMENT '菜单ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- -------------------------------------------------------------
-- 6. 楼栋
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `biz_building`;
CREATE TABLE `biz_building` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`       VARCHAR(50) NOT NULL                COMMENT '楼栋名称，如 1号楼',
  `unit_count` INT         NOT NULL DEFAULT 1      COMMENT '单元数',
  `sort`       INT         NOT NULL DEFAULT 0      COMMENT '排序',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_building_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼栋表';

-- -------------------------------------------------------------
-- 7. 房屋
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `biz_house`;
CREATE TABLE `biz_house` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `building_id` BIGINT       NOT NULL                COMMENT '楼栋ID',
  `unit_no`     VARCHAR(10)  NOT NULL                COMMENT '单元号，如 1',
  `room_no`     VARCHAR(10)  NOT NULL                COMMENT '房号，如 101',
  `area`        DECIMAL(8,2) NOT NULL DEFAULT 0.00   COMMENT '建筑面积（平方米）',
  `status`      TINYINT      NOT NULL DEFAULT 0      COMMENT '状态 0空置 1在住',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_house` (`building_id`, `unit_no`, `room_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房屋表';

-- -------------------------------------------------------------
-- 8. 业主-房屋关系
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `biz_owner_house`;
CREATE TABLE `biz_owner_house` (
  `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT   NOT NULL                COMMENT '用户ID',
  `house_id`      BIGINT   NOT NULL                COMMENT '房屋ID',
  `relation_type` TINYINT  NOT NULL DEFAULT 1      COMMENT '关系 1业主 2租户 3家属',
  `is_primary`    TINYINT  NOT NULL DEFAULT 0      COMMENT '是否主房屋 0否 1是',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_house` (`user_id`, `house_id`),
  KEY `idx_house_id` (`house_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业主房屋关联表';

-- -------------------------------------------------------------
-- 9. 维修工档案
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `biz_repair_worker`;
CREATE TABLE `biz_repair_worker` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`    BIGINT       NOT NULL                COMMENT '对应用户ID',
  `skills`     VARCHAR(200) NOT NULL DEFAULT ''     COMMENT '技能标签，逗号分隔，如 水电,门窗',
  `workload`   INT          NOT NULL DEFAULT 0      COMMENT '当前在手工单数',
  `status`     TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0休息 1可接单',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工档案表';

-- -------------------------------------------------------------
-- 10. 报修工单（核心表）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `biz_repair_order`;
CREATE TABLE `biz_repair_order` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no`         VARCHAR(32)  NOT NULL                COMMENT '工单号 RO+yyyyMMdd+6位序列',
  `house_id`         BIGINT       NOT NULL                COMMENT '房屋ID',
  `owner_id`         BIGINT       NOT NULL                COMMENT '报修人用户ID',
  `worker_id`        BIGINT       NULL                    COMMENT '维修工用户ID，未派单为NULL',
  `type`             TINYINT      NOT NULL DEFAULT 5      COMMENT '类型 1水电 2门窗 3电梯 4公共设施 5其他',
  `urgency`          TINYINT      NOT NULL DEFAULT 0      COMMENT '紧急程度 0普通 1紧急',
  `status`           VARCHAR(20)  NOT NULL DEFAULT 'PENDING_DISPATCH'
                     COMMENT '状态 PENDING_DISPATCH/DISPATCHED/ACCEPTED/PROCESSING/COMPLETED/EVALUATED/CANCELLED/REJECTED',
  `description`      VARCHAR(500) NOT NULL                COMMENT '问题描述',
  `expect_time`      DATETIME     NULL                    COMMENT '期望上门时间',
  `dispatch_time`    DATETIME     NULL                    COMMENT '派单时间',
  `accept_time`      DATETIME     NULL                    COMMENT '接单时间',
  `finish_time`      DATETIME     NULL                    COMMENT '完成时间',
  `handle_result`    VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '处理结果说明',
  `score`            TINYINT      NULL                    COMMENT '评分 1-5',
  `evaluate_content` VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '评价内容',
  `evaluate_time`    DATETIME     NULL                    COMMENT '评价时间',
  `cancel_reason`    VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '取消/驳回原因',
  `version`          INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本号，防并发抢单',
  `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_owner_status` (`owner_id`, `status`),
  KEY `idx_worker_status` (`worker_id`, `status`),
  KEY `idx_status_created` (`status`, `created_at`),
  KEY `idx_house_id` (`house_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修工单表';

-- -------------------------------------------------------------
-- 11. 工单流转日志（可追溯的核心证据）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `biz_repair_order_log`;
CREATE TABLE `biz_repair_order_log` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id`      BIGINT       NOT NULL                COMMENT '工单ID',
  `from_status`   VARCHAR(20)  NOT NULL DEFAULT ''     COMMENT '原状态，空表示创建',
  `to_status`     VARCHAR(20)  NOT NULL                COMMENT '新状态',
  `operator_id`   BIGINT       NOT NULL                COMMENT '操作人ID',
  `operator_name` VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '操作人姓名（冗余，便于展示）',
  `remark`        VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '备注',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转日志表';

-- -------------------------------------------------------------
-- 12. 账单
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `biz_bill`;
CREATE TABLE `biz_bill` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `house_id`   BIGINT        NOT NULL                COMMENT '房屋ID',
  `period`     CHAR(7)       NOT NULL                COMMENT '账期 yyyy-MM',
  `amount`     DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '金额',
  `status`     TINYINT       NOT NULL DEFAULT 0      COMMENT '状态 0未缴 1已缴 2逾期',
  `due_date`   DATE          NOT NULL                COMMENT '应缴截止日',
  `remark`     VARCHAR(255)  NOT NULL DEFAULT ''     COMMENT '备注',
  `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  -- 关键：同一房屋同一账期只能有一条账单，保证定时任务重复执行时幂等
  UNIQUE KEY `uk_house_period` (`house_id`, `period`),
  KEY `idx_status` (`status`),
  KEY `idx_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';

-- -------------------------------------------------------------
-- 13. 缴费记录
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `biz_payment`;
CREATE TABLE `biz_payment` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `bill_id`     BIGINT        NOT NULL                COMMENT '账单ID',
  `amount`      DECIMAL(10,2) NOT NULL                COMMENT '缴费金额',
  `pay_method`  TINYINT       NOT NULL DEFAULT 1      COMMENT '方式 1线下现金 2转账 3其他',
  `pay_time`    DATETIME      NOT NULL                COMMENT '缴费时间',
  `operator_id` BIGINT        NOT NULL                COMMENT '操作人（管理员）',
  `remark`      VARCHAR(255)  NOT NULL DEFAULT ''     COMMENT '备注',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_bill_id` (`bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缴费记录表';

-- -------------------------------------------------------------
-- 14. 公告
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `biz_announcement`;
CREATE TABLE `biz_announcement` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`        VARCHAR(200) NOT NULL                COMMENT '标题',
  `content`      TEXT         NOT NULL                COMMENT '正文',
  `publisher_id` BIGINT       NOT NULL                COMMENT '发布人ID',
  `scope`        TINYINT      NOT NULL DEFAULT 1      COMMENT '可见范围 1全体 2业主 3维修工',
  `status`       TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0草稿 1已发布 2已下线',
  `publish_time` DATETIME     NULL                    COMMENT '发布时间',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_status_publish` (`status`, `publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- -------------------------------------------------------------
-- 15. 公告已读
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `biz_announcement_read`;
CREATE TABLE `biz_announcement_read` (
  `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `announcement_id` BIGINT   NOT NULL                COMMENT '公告ID',
  `user_id`         BIGINT   NOT NULL                COMMENT '用户ID',
  `read_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ann_user` (`announcement_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读表';

-- -------------------------------------------------------------
-- 16. 访客
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `biz_visitor`;
CREATE TABLE `biz_visitor` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `inviter_id`    BIGINT      NOT NULL                COMMENT '邀请人（业主）用户ID',
  `house_id`      BIGINT      NOT NULL                COMMENT '房屋ID',
  `visitor_name`  VARCHAR(50) NOT NULL                COMMENT '访客姓名',
  `visitor_phone` VARCHAR(20) NOT NULL DEFAULT ''     COMMENT '访客手机号',
  `code`          VARCHAR(16) NOT NULL                COMMENT '邀请码',
  `expire_at`     DATETIME    NOT NULL                COMMENT '过期时间',
  `status`        TINYINT     NOT NULL DEFAULT 0      COMMENT '状态 0待核销 1已核销 2已失效',
  `verify_time`   DATETIME    NULL                    COMMENT '核销时间',
  `verify_by`     BIGINT      NULL                    COMMENT '核销人（门岗）用户ID',
  `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_inviter` (`inviter_id`),
  KEY `idx_expire` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客表';

-- -------------------------------------------------------------
-- 17. 附件
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `biz_attachment`;
CREATE TABLE `biz_attachment` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_type`   VARCHAR(50)  NOT NULL                COMMENT '业务类型 REPAIR_ORDER / ANNOUNCEMENT ...',
  `biz_id`     BIGINT       NOT NULL                COMMENT '业务ID',
  `url`        VARCHAR(500) NOT NULL                COMMENT '文件访问地址',
  `file_name`  VARCHAR(200) NOT NULL DEFAULT ''     COMMENT '原始文件名',
  `size`       BIGINT       NOT NULL DEFAULT 0      COMMENT '文件大小（字节）',
  `mime`       VARCHAR(100) NOT NULL DEFAULT ''     COMMENT 'MIME类型',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件表';


-- =============================================================
--  初始化数据
-- =============================================================

-- ---------- 角色 ----------
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `sort`, `remark`) VALUES
(1, 'ADMIN',   '超级管理员', 1, '系统全部权限'),
(2, 'MANAGER', '物业管理员', 2, '派单、账单、公告、基础数据'),
(3, 'WORKER',  '维修工',     3, '接单、处理工单'),
(4, 'OWNER',   '业主',       4, '报修、缴费、看公告');

-- ---------- 用户 ----------
-- 说明：password 初始为空，后端启动时由 DataInitializer 用 BCrypt 写入初始密码 123456
-- 若你想手动生成 BCrypt 哈希，在 SpringBoot 项目里执行：
--   System.out.println(new BCryptPasswordEncoder().encode("123456"));
-- 然后 UPDATE sys_user SET password = '生成的哈希' WHERE username = 'admin';
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `status`, `remark`) VALUES
(1, 'admin',   '', '系统管理员', '13800000001', 1, '超级管理员账号'),
(2, 'manager', '', '王管理',     '13800000002', 1, '物业管理员'),
(3, 'worker1', '', '李师傅',     '13800000003', 1, '水电维修'),
(4, 'worker2', '', '张师傅',     '13800000004', 1, '门窗维修'),
(5, 'owner1',  '', '陈业主',     '13800000005', 1, '1号楼1单元101业主');

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 3),
(5, 4);

-- ---------- 菜单 / 按钮 ----------
-- type: M=目录 C=菜单 F=按钮
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `perms`, `type`, `icon`, `sort`) VALUES
-- 顶级目录
(1, 0, '系统管理', '/system', '', '', 'M', 'setting', 1),
(2, 0, '业务管理', '/biz',    '', '', 'M', 'appstore', 2),
(3, 0, '我的',     '/mine',   '', '', 'M', 'user',     3),

-- 系统管理
(101, 1, '用户管理', '/system/user', 'system/user/index', 'system:user:list', 'C', 'user',      1),
(102, 1, '角色管理', '/system/role', 'system/role/index', 'system:role:list', 'C', 'team',      2),
(103, 1, '菜单管理', '/system/menu', 'system/menu/index', 'system:menu:list', 'C', 'menu',      3),
(104, 1, '操作日志', '/system/log',  'system/log/index',  'system:log:list',  'C', 'file-text', 4),

-- 业务管理
(201, 2, '房屋管理',   '/biz/house',        'biz/house/index',        'biz:house:list',        'C', 'home',           1),
(202, 2, '维修工管理', '/biz/worker',       'biz/worker/index',       'biz:worker:list',       'C', 'tool',           2),
(203, 2, '工单管理',   '/biz/order',        'biz/order/index',        'biz:order:list',        'C', 'profile',        3),
(204, 2, '账单管理',   '/biz/bill',         'biz/bill/index',         'biz:bill:list',         'C', 'account-book',   4),
(205, 2, '公告管理',   '/biz/announcement', 'biz/announcement/index', 'biz:announcement:list', 'C', 'notification',   5),
(206, 2, '访客记录',   '/biz/visitor',      'biz/visitor/index',      'biz:visitor:list',      'C', 'idcard',         6),

-- 我的
(301, 3, '我要报修', '/mine/repair/create', 'mine/repair/create', '', 'C', 'plus-circle',  1),
(302, 3, '我的报修', '/mine/repair',        'mine/repair/index',  '', 'C', 'list',         2),
(303, 3, '我的账单', '/mine/bill',          'mine/bill/index',    '', 'C', 'account-book', 3),
(304, 3, '公告',     '/mine/announcement',  'mine/announcement/index', '', 'C', 'notification', 4),
(305, 3, '访客邀请', '/mine/visitor',       'mine/visitor/index', '', 'C', 'idcard',       5),
(306, 3, '我的工单', '/mine/work',          'mine/work/index',    '', 'C', 'tool',         6),

-- 按钮权限
(401, 203, '派单',     '', '', 'biz:order:dispatch', 'F', '', 1),
(402, 204, '标记缴费', '', '', 'biz:bill:pay',       'F', '', 1),
(403, 204, '导出账单', '', '', 'biz:bill:export',    'F', '', 2),
(404, 302, '评价',     '', '', 'biz:order:evaluate', 'F', '', 1),
(405, 301, '提交报修', '', '', 'biz:order:create',   'F', '', 1),
(406, 306, '接单',     '', '', 'biz:order:accept',   'F', '', 1),
(407, 306, '完成工单', '', '', 'biz:order:complete', 'F', '', 2);

-- ---------- 角色-菜单授权 ----------
-- 超管：全部
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `id` FROM `sys_menu`;

-- 物业管理员（含"我要报修"用于代业主报修）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(2, 2), (2, 201), (2, 202), (2, 203), (2, 204), (2, 205), (2, 206),
(2, 301), (2, 401), (2, 402), (2, 403), (2, 405);

-- 维修工
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(3, 3), (3, 306), (3, 406), (3, 407);

-- 业主
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(4, 3), (4, 301), (4, 302), (4, 303), (4, 304), (4, 305),
(4, 404), (4, 405);

-- ---------- 楼栋 ----------
INSERT INTO `biz_building` (`id`, `name`, `unit_count`, `sort`) VALUES
(1, '1号楼', 2, 1),
(2, '2号楼', 2, 2),
(3, '3号楼', 1, 3);

-- ---------- 房屋 ----------
INSERT INTO `biz_house` (`building_id`, `unit_no`, `room_no`, `area`, `status`) VALUES
(1, '1', '101', 89.50, 1),
(1, '1', '102', 89.50, 1),
(1, '1', '201', 92.00, 1),
(1, '1', '202', 92.00, 1),
(1, '2', '101', 105.00, 1),
(1, '2', '102', 105.00, 0),
(2, '1', '101', 78.00, 1),
(2, '1', '102', 78.00, 1),
(2, '2', '201', 120.00, 1),
(3, '1', '101', 110.00, 1);

-- ---------- 维修工档案 ----------
INSERT INTO `biz_repair_worker` (`user_id`, `skills`, `workload`, `status`) VALUES
(3, '水电,电梯', 0, 1),
(4, '门窗,公共设施', 0, 1);

-- ---------- 业主房屋绑定 ----------
INSERT INTO `biz_owner_house` (`user_id`, `house_id`, `relation_type`, `is_primary`) VALUES
(5, 1, 1, 1);

-- ---------- 示例公告 ----------
INSERT INTO `biz_announcement` (`title`, `content`, `publisher_id`, `scope`, `status`, `publish_time`) VALUES
('关于小区停水的通知',
 '因管网改造，本周六 9:00-17:00 全小区停水，请提前储水。给您带来不便，敬请谅解。',
 2, 1, 1, NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================
--  自检脚本（执行完可跑一遍确认数据正确）
-- =============================================================
-- 17 张表
-- SELECT COUNT(*) AS table_count FROM information_schema.tables
--   WHERE table_schema = 'qilin';
-- 角色/用户/菜单
-- SELECT COUNT(*) FROM sys_role;        -- 期望 4
-- SELECT COUNT(*) FROM sys_user;        -- 期望 5
-- SELECT COUNT(*) FROM sys_menu;        -- 期望 26
-- SELECT r.role_code, COUNT(rm.menu_id) AS menus
--   FROM sys_role r LEFT JOIN sys_role_menu rm ON rm.role_id = r.id
--  GROUP BY r.id ORDER BY r.id;
--   期望：ADMIN=26, MANAGER=12, WORKER=4, OWNER=8
-- 房屋
-- SELECT COUNT(*) FROM biz_house;       -- 期望 10
