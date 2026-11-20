package com.qilin.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 菜单/按钮权限表 sys_menu 的实体。
 *
 * <p>注意 sys_menu 表没有 deleted 字段（菜单停用用 status），
 * 所以这里不要加 @TableLogic，否则会拼出不存在的列。
 *
 * <p>type：M 目录 / C 菜单 / F 按钮。perms 是后端鉴权和前端按钮级控制共用的权限标识。
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父级 ID，0 为顶级 */
    private Long parentId;

    private String name;

    private String path;

    private String component;

    /** 权限标识，如 biz:order:dispatch；目录/菜单可能为空串 */
    private String perms;

    /** M 目录 / C 菜单 / F 按钮 */
    private String type;

    private String icon;

    private Integer sort;

    /** 0 隐藏 1 显示 */
    private Integer visible;

    /** 0 禁用 1 启用 */
    private Integer status;
}
