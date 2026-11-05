package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oms_order_setting")
public class OrderSetting {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer flashOrderOvertime;
    private Integer normalOrderOvertime;
    private Integer confirmOvertime;
    private Integer commentOvertime;
    private LocalDateTime createTime;
}
