package com.qilin.module.auth.service;

import com.qilin.common.exception.BusinessException;
import com.qilin.common.result.ResultCode;
import com.qilin.common.security.LoginUser;
import com.qilin.common.security.LoginUserHolder;
import com.qilin.common.util.IpUtils;
import com.qilin.module.auth.dto.ChangePasswordRequest;
import com.qilin.module.auth.dto.LoginRequest;
import com.qilin.module.auth.dto.UserInfoVO;
import com.qilin.module.system.entity.SysUser;
import com.qilin.module.system.mapper.SysMenuMapper;
import com.qilin.module.system.mapper.SysRoleMapper;
import com.qilin.module.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证业务。
 *
 * <p>⚠️ 分工约定：登录认证是面试必问的一块，建议你**先按下面的注释自己从空白重写**，
 * 写完再回来 diff 这份实现。核心只有 4 步：
 * <ol>
 *   <li>按用户名查用户（查不到 → 401，提示语不要暴露“账号不存在”）</li>
 *   <li>BCrypt 校验密码</li>
 *   <li>算角色 + 权限，装进 Session（登录成功后换新 SessionId，防会话固定）</li>
 *   <li>返回用户信息（不含密码）</li>
 * </ol>
 *
 * <p>进阶（P1 再做）：登录失败锁定（连续 5 次冻 15 分钟）、图形验证码、登录日志。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 登录。
     *
     * <p>为什么“用户名或密码错误”不区分：
     * 如果提示“用户不存在”，攻击者就能拿它批量探测有效账号（用户枚举）。
     * 统一文案是安全基线。
     */
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO login(LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername().trim();
        SysUser user = userMapper.selectByUsername(username);

        if (user == null || !StringUtils.hasText(user.getPassword())
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }

        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该账号未分配角色，请联系管理员");
        }
        List<String> perms = menuMapper.selectPermsByUserId(user.getId());

        String ip = IpUtils.getClientIp(httpRequest);
        LocalDateTime now = LocalDateTime.now();

        LoginUser loginUser = LoginUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roles(roles)
                .perms(perms)
                .loginIp(ip)
                .loginTime(now)
                .build();

        // 防会话固定攻击：登录成功后丢弃旧 Session，换一个新的 SessionId。
        // 否则攻击者可以先塞给用户一个已知 SessionId，用户登录后该 Session 就变成了已登录状态。
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(LoginUser.SESSION_KEY, loginUser);

        // 记录最后登录信息（只更新这两个字段，别整行 update，避免覆盖别的并发修改）
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLastLoginAt(now);
        update.setLastLoginIp(ip);
        userMapper.updateById(update);

        log.info("用户登录成功 username={} roles={} ip={}", user.getUsername(), roles, ip);
        return UserInfoVO.from(loginUser);
    }

    /** 退出：销毁 Session 即可 */
    public void logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /** 当前登录用户（拦截器已经把 LoginUser 放进 ThreadLocal） */
    public UserInfoVO me() {
        return UserInfoVO.from(LoginUserHolder.require());
    }

    /**
     * 修改密码。
     *
     * <p>先验证原密码，再写新哈希。
     * 生产环境还应：改密后让该用户所有会话失效（强制重新登录）、记录操作日志。
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordRequest request) {
        LoginUser current = LoginUserHolder.require();

        SysUser user = userMapper.selectById(current.getUserId());
        if (user == null || !StringUtils.hasText(user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号不存在，请重新登录");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "原密码不正确");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "新密码不能与原密码相同");
        }

        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(update);

        log.info("用户修改密码 username={}", user.getUsername());
    }
}
