package com.macro.mall.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class CommonPage<T> {
    private long pageNum;
    private long pageSize;
    private long totalPage;
    private long total;
    private List<T> list;

    public static <T> CommonPage<T> of(IPage<T> page) {
        CommonPage<T> result = new CommonPage<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotalPage(page.getPages());
        result.setTotal(page.getTotal());
        result.setList(page.getRecords());
        return result;
    }

    public static <T> CommonPage<T> of(List<T> list) {
        CommonPage<T> result = new CommonPage<>();
        result.setPageNum(1);
        result.setPageSize(list.size());
        result.setTotalPage(1);
        result.setTotal(list.size());
        result.setList(list);
        return result;
    }
}
