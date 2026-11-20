package com.qilin.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结构。
 *
 * <p>前端的表格组件一般只需要 records + total，pageNum/pageSize/pages 便于展示分页器。
 *
 * @param <T> 行数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> records;
    /** 总条数 */
    private long total;
    /** 当前页码（从 1 开始） */
    private long pageNum;
    /** 每页条数 */
    private long pageSize;
    /** 总页数 */
    private long pages;

    /** 直接把 MyBatis-Plus 的分页对象转成统一结构 */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }
}
