package com.qilin.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qilin.common.result.PageResult;
import com.qilin.common.result.Result;
import com.qilin.common.security.RequirePerm;
import com.qilin.module.system.entity.SysUser;
import com.qilin.module.system.mapper.SysUserMapper;
import com.qilin.module.system.vo.SysUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理接口（P0 阶段的“受保护接口”示例）。
 *
 * <p>它同时演示了三件事，写前端之前可以先拿它验证：
 * <ol>
 *   <li>Session 认证：不带 Cookie 访问 → 401</li>
 *   <li>按钮级权限：@RequirePerm("system:user:list")，业主登录后访问 → 403</li>
 *   <li>分页：MyBatis-Plus 分页插件 + 统一 PageResult</li>
 * </ol>
 */
@Tag(name = "02. 系统 - 用户管理")
@RestController
@RequestMapping("/api/v1/system/users")
@RequiredArgsConstructor
public class SystemUserController {

    private final SysUserMapper userMapper;

    @Operation(summary = "用户分页列表", description = "需要权限 system:user:list（仅超管）")
    @RequirePerm("system:user:list")
    @GetMapping
    public Result<PageResult<SysUserVO>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {

        // 防御：页码最小 1，每页最多 100 条
        Page<SysUser> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 100));

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            // 注意 and(...) 包一层，否则和后面的条件组合时会变成 a OR b AND c 的优先级错误
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword));
        }
        wrapper.orderByAsc(SysUser::getId);

        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        List<SysUserVO> records = result.getRecords().stream().map(SysUserVO::from).toList();

        return Result.ok(new PageResult<>(records, result.getTotal(),
                result.getCurrent(), result.getSize(), result.getPages()));
    }
}
