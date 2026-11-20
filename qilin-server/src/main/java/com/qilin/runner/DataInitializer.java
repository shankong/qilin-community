package com.qilin.runner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qilin.module.system.entity.SysUser;
import com.qilin.module.system.mapper.SysMenuMapper;
import com.qilin.module.system.mapper.SysRoleMapper;
import com.qilin.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 启动初始化：给“空密码”的种子账号写入 BCrypt 初始密码。
 *
 * <p>为什么需要它：sql/qilin_init.sql 里 password 故意留空，
 * 而 BCrypt 哈希只能在运行期生成（每次结果还不同），
 * 所以由应用首次启动时补齐。
 *
 * <p>幂等：只处理 password 为空 / 不是 BCrypt 格式（$2 开头）的账号，
 * 已初始化过的账号不会被重复改写，你手动改过的密码也不会被覆盖。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final PasswordEncoder passwordEncoder;

    /** 初始密码，可用 qilin.init.default-password 覆盖 */
    @Value("${qilin.init.default-password:123456}")
    private String defaultPassword;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        long userCount = userMapper.selectCount(null);
        if (userCount == 0) {
            log.error("sys_user 表为空！请先执行 sql/qilin_init.sql 初始化数据库");
            return;
        }

        List<SysUser> users = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().orderByAsc(SysUser::getId));

        int initialized = 0;
        for (SysUser user : users) {
            String password = user.getPassword();
            boolean notBcrypt = password == null || password.isBlank() || !password.startsWith("$2");
            if (notBcrypt) {
                userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, user.getId())
                        .set(SysUser::getPassword, passwordEncoder.encode(defaultPassword)));
                initialized++;
            }
        }

        if (initialized > 0) {
            log.warn("已为 {} 个空密码账号写入初始密码（配置项 qilin.init.default-password），上线前请修改", initialized);
        }
        log.info("数据库就绪：用户 {} 个 / 角色 {} 个 / 菜单 {} 个",
                userCount, roleMapper.selectCount(null), menuMapper.selectCount(null));
    }
}
