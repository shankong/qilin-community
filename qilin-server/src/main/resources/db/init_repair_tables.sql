-- ============================================
-- 栖邻项目 - 工单模块数据库初始化脚本
-- 创建时间: 2026-11-24
-- 说明: 包含工单主表、流转日志表、维修工表及测试数据
-- ============================================

-- 1. 工单主表
DROP TABLE IF EXISTS `biz_repair_order`;
CREATE TABLE `biz_repair_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '工单号，格式：RO20261124000001',
  `house_id` BIGINT NULL COMMENT '房屋ID（暂时可为空，待房屋表创建后关联）',
  `owner_id` BIGINT NOT NULL COMMENT '业主ID，关联sys_user',
  `worker_id` BIGINT NULL COMMENT '维修工ID，关联sys_user',
  `type` VARCHAR(20) NOT NULL COMMENT '工单类型：PLUMBING(水电), RENOVATION(装修), APPLIANCE(电器), OTHER(其他)',
  `status` VARCHAR(30) NOT NULL COMMENT '状态：PENDING_DISPATCH, DISPATCHED, ACCEPTED, PROCESSING, COMPLETED, EVALUATED, CANCELLED, REJECTED',
  `urgency` VARCHAR(10) NOT NULL DEFAULT 'MEDIUM' COMMENT '紧急程度：LOW(低), MEDIUM(中), HIGH(高)',
  `description` TEXT NOT NULL COMMENT '问题描述',
  `expect_time` DATETIME NULL COMMENT '期望上门时间',
  `accept_time` DATETIME NULL COMMENT '接单时间',
  `finish_time` DATETIME NULL COMMENT '完成时间',
  `score` INT NULL COMMENT '评分 1-5',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_owner_status` (`owner_id`, `status`),
  KEY `idx_worker_status` (`worker_id`, `status`),
  KEY `idx_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单主表';

-- 2. 工单流转日志表
DROP TABLE IF EXISTS `biz_repair_order_log`;
CREATE TABLE `biz_repair_order_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '工单ID',
  `from_status` VARCHAR(30) NULL COMMENT '原状态',
  `to_status` VARCHAR(30) NOT NULL COMMENT '新状态',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转日志表';

-- 3. 维修工档案表
DROP TABLE IF EXISTS `biz_repair_worker`;
CREATE TABLE `biz_repair_worker` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '关联sys_user的用户ID',
  `skills` JSON NOT NULL COMMENT '技能标签，JSON数组格式，如：["水电","装修","电器"]',
  `workload` INT NOT NULL DEFAULT 0 COMMENT '当前工作量（未完成工单数）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE(在职), INACTIVE(离职)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工档案表';

-- ============================================
-- 测试数据插入
-- ============================================

-- 插入维修工数据（假设user_id 100和101是维修工账号）
INSERT INTO `biz_repair_worker` (`user_id`, `skills`, `workload`, `status`) VALUES
(100, '["水电", "管道维修"]', 2, 'ACTIVE'),
(101, '["装修", "木工", "油漆"]', 1, 'ACTIVE');

-- 插入工单数据（假设owner_id=1是测试业主账号）
INSERT INTO `biz_repair_order`
(`order_no`, `house_id`, `owner_id`, `worker_id`, `type`, `status`, `urgency`, `description`, `expect_time`, `accept_time`, `finish_time`, `score`)
VALUES
-- 1. 待派单工单
('RO20261124000001', NULL, 1, NULL, 'PLUMBING', 'PENDING_DISPATCH', 'HIGH', '厨房水龙头漏水严重，急需维修', '2026-11-25 09:00:00', NULL, NULL, NULL),

-- 2. 待派单工单
('RO20261124000002', NULL, 1, NULL, 'APPLIANCE', 'PENDING_DISPATCH', 'MEDIUM', '客厅空调不制冷，请师傅检查', '2026-11-26 14:00:00', NULL, NULL, NULL),

-- 3. 已派单工单
('RO20261124000003', NULL, 1, 100, 'PLUMBING', 'DISPATCHED', 'MEDIUM', '卫生间马桶堵塞', '2026-11-25 10:00:00', NULL, NULL, NULL),

-- 4. 处理中工单
('RO20261123000001', NULL, 1, 100, 'RENOVATION', 'PROCESSING', 'LOW', '卧室墙面需要重新粉刷', '2026-11-23 15:00:00', '2026-11-23 16:30:00', NULL, NULL),

-- 5. 已完成工单
('RO20261122000001', NULL, 1, 101, 'OTHER', 'COMPLETED', 'MEDIUM', '门锁损坏需要更换', '2026-11-22 10:00:00', '2026-11-22 10:30:00', '2026-11-22 12:00:00', 5);

-- 插入工单流转日志
INSERT INTO `biz_repair_order_log` (`order_id`, `from_status`, `to_status`, `operator_id`, `remark`) VALUES
-- 工单1的日志（待派单）
(1, NULL, 'PENDING_DISPATCH', 1, '业主提交报修'),

-- 工单2的日志（待派单）
(2, NULL, 'PENDING_DISPATCH', 1, '业主提交报修'),

-- 工单3的日志（待派单→已派单）
(3, NULL, 'PENDING_DISPATCH', 1, '业主提交报修'),
(3, 'PENDING_DISPATCH', 'DISPATCHED', 2, '管理员派单给维修工100'),

-- 工单4的日志（待派单→已派单→已接单→处理中）
(4, NULL, 'PENDING_DISPATCH', 1, '业主提交报修'),
(4, 'PENDING_DISPATCH', 'DISPATCHED', 2, '管理员派单给维修工100'),
(4, 'DISPATCHED', 'ACCEPTED', 100, '维修工接单'),
(4, 'ACCEPTED', 'PROCESSING', 100, '开始处理'),

-- 工单5的日志（完整流程）
(5, NULL, 'PENDING_DISPATCH', 1, '业主提交报修'),
(5, 'PENDING_DISPATCH', 'DISPATCHED', 2, '管理员派单给维修工101'),
(5, 'DISPATCHED', 'ACCEPTED', 101, '维修工接单'),
(5, 'ACCEPTED', 'PROCESSING', 101, '开始处理'),
(5, 'PROCESSING', 'COMPLETED', 101, '已更换新门锁，测试正常');

-- ============================================
-- 验证查询
-- ============================================

-- 查看工单统计
SELECT status, COUNT(*) as count FROM biz_repair_order GROUP BY status;

-- 查看维修工工作量
SELECT w.id, w.user_id, w.skills, w.workload, w.status
FROM biz_repair_worker w;

-- 查看工单流转记录
SELECT o.order_no, l.from_status, l.to_status, l.operator_id, l.remark, l.created_at
FROM biz_repair_order_log l
JOIN biz_repair_order o ON l.order_id = o.id
ORDER BY l.id;
