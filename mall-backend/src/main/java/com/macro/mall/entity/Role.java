package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ums_role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer adminCount;
    private LocalDateTime createTime;
    private Integer status;
    private Integer sort;
}
