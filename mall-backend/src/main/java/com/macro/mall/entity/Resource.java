package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ums_resource")
public class Resource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String url;
    private String method;
    private String description;
    private LocalDateTime createTime;
}
