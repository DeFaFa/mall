package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_home_advertise")
public class HomeAdvertise {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer type;
    private String pic;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Integer clickCount;
    private Integer orderCount;
    private String url;
    private Integer sort;
    private String note;
}
