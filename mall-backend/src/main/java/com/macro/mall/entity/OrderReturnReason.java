package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oms_order_return_reason")
public class OrderReturnReason {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}
