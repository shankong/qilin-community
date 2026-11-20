package com.qilin.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置。
 *
 * <p>两个插件对应项目里两个“面试点”：
 * <ul>
 *   <li>分页：没有它，selectPage 会变成全表查完再内存分页；</li>
 *   <li>乐观锁：工单表有 version 字段，两个维修工同时接单时，
 *       只有一个的 UPDATE ... WHERE version = ? 能成功，另一个影响行数为 0。</li>
 * </ul>
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 乐观锁（工单抢单、账单并发修改会用到）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 分页（注意：分页插件建议放在最后）
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setMaxLimit(500L);      // 防止前端传 pageSize=999999 把数据库拖死
        pagination.setOverflow(false);     // 页码超出范围时返回空列表，而不是回到第一页
        interceptor.addInnerInterceptor(pagination);

        return interceptor;
    }
}
